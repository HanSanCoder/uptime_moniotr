package io.hansan.monitor.check;


import io.hansan.monitor.configs.WebSocketConfig;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.service.HeartbeatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

@Slf4j
public class HttpMonitorChecker extends AbstractMonitorChecker {

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
                HttpRequestBase request = new HttpGet(monitor.getUrl());

                // 设置超时
                int timeout = 5000; // 默认5秒
                if (monitor.getCheck_interval() != null) {
                    timeout = monitor.getCheck_interval() * 1000;
                }

                RequestConfig config = RequestConfig.custom()
                        .setConnectTimeout(timeout)
                        .setSocketTimeout(timeout)
                        .build();
                request.setConfig(config);

                // 执行请求
                long startTime = System.currentTimeMillis();
                try (CloseableHttpClient httpClient = HttpClients.createDefault();
                     CloseableHttpResponse response = httpClient.execute(request)) {

                    long ping = System.currentTimeMillis() - startTime;
                    int statusCode = response.getStatusLine().getStatusCode();

                    // 检查状态码是否表示成功（2xx和3xx）
                    boolean isSuccess = statusCode >= 200 && statusCode < 400;
                    String message = "HTTP " + statusCode + ", 响应时间: " + ping + "ms";

                    return new HttpResult(isSuccess, message, ping);
                }
            } catch (Exception e) {
                lastException = e;
                retryCount++;

                if (retryCount <= maxRetries) {
                    handleRetry(monitor, retryCount, maxRetries);
                }
            }
        }

        log.error("HTTP检查异常，重试{}次后仍然失败", maxRetries, lastException);
        return new HttpResult(false, "Error: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    @Override
    protected String getCheckerType() {
        return "HTTP";
    }
}