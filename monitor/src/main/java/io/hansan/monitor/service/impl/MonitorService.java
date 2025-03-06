package io.hansan.monitor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.hansan.monitor.mapper.MonitorMapper;
import io.hansan.monitor.model.MonitorModel;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;
import java.util.List;

@Component
public class MonitorService {

    @Inject
    private MonitorMapper monitorMapper;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 用于存储检查结果的内部类
     */
    public static class CheckResult {
        private final boolean success;
        private final long responseTime;
        private final int statusCode;

        public CheckResult(boolean success, long responseTime, int statusCode) {
            this.success = success;
            this.responseTime = responseTime;
            this.statusCode = statusCode;
        }

        public boolean isSuccess() {
            return success;
        }

        public long getResponseTime() {
            return responseTime;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    /**
     * 获取所有活跃的监控配置
     */
    public List<MonitorModel> getAllActiveMonitors() {
        return monitorMapper.getAllActive();
    }

    /**
     * 根据类型获取监控配置
     */
    public List<MonitorModel> getMonitorsByType(String type) {
        return monitorMapper.getMonitorsByType(type);
    }

    /**
     * 执行对指定监控的检查
     */
    public CheckResult checkMonitor(MonitorModel monitor) {
        switch (monitor.getType().toLowerCase()) {
            case "http":
                return checkHttp(monitor);
            case "tcp":
                return checkTcp(monitor);
            case "ping":
                return checkPing(monitor);
            default:
                return new CheckResult(false, -1, 400);
        }
    }

    /**
     * HTTP连接检查
     */
    private CheckResult checkHttp(MonitorModel monitor) {
        try {
            long start = System.currentTimeMillis();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(monitor.getUrl()))
                    .timeout(Duration.ofMillis(monitor.getCheckInterval() * 1000))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            long responseTime = System.currentTimeMillis() - start;
            boolean success = response.statusCode() < 400;

            // 如果设置了关键字，检查响应中是否包含该关键字
            if (success && monitor.getKeyword() != null && !monitor.getKeyword().isEmpty()) {
                success = response.body().contains(monitor.getKeyword());
            }

            return new CheckResult(success, responseTime, response.statusCode());
        } catch (Exception e) {
            return new CheckResult(false, -1, 500);
        }
    }

    /**
     * TCP连接检查
     */
    private CheckResult checkTcp(MonitorModel monitor) {
        String host = monitor.getHostname() != null ? monitor.getHostname() : parseHost(monitor.getUrl());
        int port = monitor.getPort() != null ? monitor.getPort() : parsePort(monitor.getUrl());
        int timeout = monitor.getCheckInterval() * 1000;

        try (Socket socket = new Socket()) {
            long start = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(host, port), timeout);
            long responseTime = System.currentTimeMillis() - start;
            return new CheckResult(true, responseTime, 200);
        } catch (IOException e) {
            return new CheckResult(false, -1, 500);
        }
    }

    /**
     * Ping检查实现
     */
    private CheckResult checkPing(MonitorModel monitor) {
        String host = monitor.getHostname() != null ? monitor.getHostname() : parseHost(monitor.getUrl());

        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 " + host);
            int exitCode = process.waitFor();
            boolean success = (exitCode == 0);
            return new CheckResult(success, -1, success ? 200 : 500);
        } catch (Exception e) {
            return new CheckResult(false, -1, 500);
        }
    }

    /**
     * 保存检查结果
     */
    public void saveCheckResult(MonitorModel monitor, CheckResult result) {
        monitorMapper.saveCheckResult(
            monitor.getId(),
            result.isSuccess(),
            result.getResponseTime(),
            result.getStatusCode()
        );
    }

    // 辅助方法，从URL中解析主机名
    private String parseHost(String url) {
        if (url == null) return "";
        if (url.contains("://")) {
            url = url.split("://")[1];
        }
        if (url.contains(":")) {
            return url.split(":")[0];
        }
        if (url.contains("/")) {
            return url.split("/")[0];
        }
        return url;
    }

    // 辅助方法，从URL中解析端口号
    private int parsePort(String url) {
        if (url == null) return 80;
        if (url.contains("://")) {
            String protocol = url.split("://")[0].toLowerCase();
            url = url.split("://")[1];
            if (!url.contains(":") && protocol.equals("https")) {
                return 443; // HTTPS默认端口
            }
        }
        if (url.contains(":")) {
            String portStr = url.split(":")[1];
            if (portStr.contains("/")) {
                portStr = portStr.split("/")[0];
            }
            try {
                return Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                return 80; // 默认HTTP端口
            }
        }
        return 80; // 默认HTTP端口
    }
}