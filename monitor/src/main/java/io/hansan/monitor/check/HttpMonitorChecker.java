package io.hansan.monitor.check;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hansan.monitor.configs.WebSocketConfig;
import io.hansan.monitor.dto.UserContext;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.service.EmailService;
import io.hansan.monitor.service.HeartbeatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.noear.solon.annotation.Inject;

import javax.net.ssl.*;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HttpMonitorChecker extends AbstractMonitorChecker {
    @Inject
    private EmailService emailService;

    private static final ConcurrentHashMap<String, Boolean> notifiedCertHosts = new ConcurrentHashMap<>();

    public HttpMonitorChecker(HeartbeatService heartbeatService, WebSocketConfig webSocketServer) {
        super(heartbeatService, webSocketServer);
    }

    @Override
    public HttpResult check(MonitorModel monitor) {
        int retryCount = 0;
        int maxRetries = monitor.getMaxretries() != null ? monitor.getMaxretries() : 0;
        Exception lastException = null;

        while (retryCount <= maxRetries) {
            try {
                // 创建并配置HTTP请求
                HttpRequestBase request = createRequest(monitor);

                // 设置超时和重定向
                int timeout = monitor.getTimeout() != null ? monitor.getTimeout().intValue() * 1000: 5000; // 默认5秒
                int maxRedirects = monitor.getMaxredirects() != null ? monitor.getMaxredirects() : 3; // 默认最多重定向3次

                RequestConfig config = RequestConfig.custom()
                        .setConnectTimeout(timeout)
                        .setSocketTimeout(timeout)
                        .setRedirectsEnabled(true)
                        .setMaxRedirects(maxRedirects)
                        .build();
                request.setConfig(config);

                // 执行请求
                long startTime = System.currentTimeMillis();
                try (CloseableHttpClient httpClient = HttpClients.createDefault();
                     CloseableHttpResponse response = httpClient.execute(request)) {

                    long ping = System.currentTimeMillis() - startTime;
                    int statusCode = response.getStatusLine().getStatusCode();

                    // 检查状态码是否表示成功
                    String statusCodeRange = monitor.getAcceptedStatuscodes();
                    Integer minAccepted = 200;
                    Integer maxAccepted = 399;
                    if (statusCodeRange != null && statusCodeRange.contains("-")) {
                        String[] parts = statusCodeRange.split("-");
                        try {
                            minAccepted = Integer.parseInt(parts[0].trim());
                            maxAccepted = Integer.parseInt(parts[1].trim());
                        } catch (NumberFormatException e) {
                            log.warn("有效状态码获取失败: {}", statusCodeRange);
                        }
                    }
                    boolean isSuccess = statusCode >= minAccepted && statusCode <= maxAccepted;
                    String message = "HTTP " + statusCode + ", 响应时间: " + ping + "ms";
                    boolean important = false;
                    // 如果是HTTPS请求，检查TLS证书有效期
                    String url = monitor.getUrl();
                    if (url != null && url.toLowerCase().startsWith("https://")) {
                        try {
                            String host = new URL(url).getHost();
                            CertificateInfo certInfo = checkCertificateExpiration(host);
                            if (certInfo != null) {
                                // 使用原子操作
                                String hostKey = host + "-" + certInfo.expirationDate;
                                if (notifiedCertHosts.putIfAbsent(hostKey, Boolean.TRUE) == null) {
                                    // putIfAbsent返回null表示之前没有这个键，所以这是第一次执行
                                    important = true;
                                    message = String.format("警告: 证书将在%d天后过期, 证书有效期至: %s", certInfo.daysUntilExpiration, certInfo.expirationDate);
                                    emailService.sendCertificateExpirationNotification(monitor, certInfo.expirationDate, certInfo.daysUntilExpiration);
                                } else {
                                    message = String.format(" (证书将在%d天后过期)", certInfo.daysUntilExpiration);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("检查TLS证书有效期失败: {}", e.getMessage());
                        }
                    }

                    return new HttpResult(isSuccess, message, ping,important);
                }
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                if (retryCount <= maxRetries) {
                    handleRetry(monitor, retryCount, maxRetries);
                }
            }
        }
        String errorMessage = "HTTP请求失败，重试" + maxRetries + "次后仍然失败: " +
                              (lastException != null ? lastException.getMessage() : "未知错误");
        log.error(errorMessage);
        return new HttpResult(false, errorMessage);
    }

    /**
     * 检查TLS证书的过期时间
     * @param host 主机名
     * @return 证书信息，包含过期日期和剩余天数
     */
    private CertificateInfo checkCertificateExpiration(String host) {
        try {
            // 创建一个信任所有证书的TrustManager
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };

            // 创建SSLContext并配置
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustAllCerts, new java.security.SecureRandom());

            // 使用HTTPS URL连接方式获取证书
            URL url = new URL("https://" + host);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(context.getSocketFactory());
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();

            // 获取证书信息
            Certificate[] certificates = connection.getServerCertificates();
            connection.disconnect();

            if (certificates.length > 0 && certificates[0] instanceof X509Certificate) {
                X509Certificate cert = (X509Certificate) certificates[0];
                Date expirationDate = cert.getNotAfter();

                long diffTime = expirationDate.getTime() - System.currentTimeMillis();
                long diffDays = diffTime / (24 * 60 * 60 * 1000);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                return new CertificateInfo(format.format(expirationDate), diffDays);
            }
        } catch (Exception e) {
            // 详细记录错误信息以便调试
            log.error("获取证书信息失败: {} - {}", e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                log.error("根本原因: {}", e.getCause().getMessage());
            }
        }
        return null;
    }

    /**
     * 证书信息类
     */
    private static class CertificateInfo {
        String expirationDate;
        long daysUntilExpiration;

        CertificateInfo(String expirationDate, long daysUntilExpiration) {
            this.expirationDate = expirationDate;
            this.daysUntilExpiration = daysUntilExpiration;
        }
    }

    @Override
    protected String getCheckerType() {
        return "HTTP";
    }

    /**
     * 创建HTTP请求对象
     * @param monitor 监控配置
     * @return 配置好的HTTP请求对象
     * @throws Exception 如果请求创建失败
     */
    private HttpRequestBase createRequest(MonitorModel monitor) throws Exception {
        // 设置请求方法（默认GET）
        String method = monitor.getMethod() != null ? monitor.getMethod().toUpperCase() : "GET";
        HttpRequestBase request;

        switch (method) {
            case "POST":
                HttpPost postRequest = new HttpPost(monitor.getUrl());
                addRequestBody(postRequest, monitor.getBody());
                request = postRequest;
                break;
            case "PUT":
                HttpPut putRequest = new HttpPut(monitor.getUrl());
                addRequestBody(putRequest, monitor.getBody());
                request = putRequest;
                break;
            case "DELETE":
                request = new HttpDelete(monitor.getUrl());
                break;
            case "HEAD":
                request = new HttpHead(monitor.getUrl());
                break;
            default:
                request = new HttpGet(monitor.getUrl());
        }

        // 添加请求头
        addRequestHeaders(request, monitor.getHeaders());

        return request;
    }

    /**
     * 为请求添加请求体
     * @param request 支持请求体的HTTP请求
     * @param body 请求体内容
     * @throws UnsupportedEncodingException 如果编码不支持
     */
    private void addRequestBody(HttpEntityEnclosingRequest request, String body) throws UnsupportedEncodingException {
        if (body != null && !body.isEmpty()) {
            request.setEntity(new StringEntity(body, "UTF-8"));
        }
    }

    /**
     * 为请求添加请求头
     * @param request HTTP请求
     * @param headersJson 请求头JSON字符串
     */
    private void addRequestHeaders(HttpRequestBase request, String headersJson) {
        if (headersJson != null && !headersJson.isEmpty()) {
            try {
                // 解析JSON格式的请求头
                ObjectMapper mapper = new ObjectMapper();
                JsonNode headersNode = mapper.readTree(headersJson);
                if (headersNode.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = headersNode.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        request.setHeader(entry.getKey(), entry.getValue().asText());
                    }
                }
            } catch (Exception e) {
                log.warn("解析请求头失败: {}", headersJson, e);
            }
        }
    }
}