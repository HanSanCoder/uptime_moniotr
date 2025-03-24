package io.hansan.monitor.check;


import io.hansan.monitor.configs.WebSocketConfig;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.service.HeartbeatService;
import lombok.extern.slf4j.Slf4j;

import java.net.Socket;

@Slf4j
public class TcpMonitorChecker extends AbstractMonitorChecker {

    public TcpMonitorChecker(HeartbeatService heartbeatService, WebSocketConfig webSocketServer) {
        super(heartbeatService, webSocketServer);
    }

    @Override
    public HttpResult check(MonitorModel monitor) {
        int retryCount = 0;
        int maxRetries = monitor.getMaxretries() != null ? monitor.getMaxretries() : 3; // 默认最大重试3次
        Exception lastException = null;

        while (retryCount <= maxRetries) {
            String message;
            long responseTime = 0;
            try (Socket socket = new Socket()) {
                // 设置超时
                int timeout = monitor.getTimeout() != null ? monitor.getTimeout().intValue() * 1000: 5000; // 默认5秒
                long startTime = System.currentTimeMillis();
                socket.connect(
                        new java.net.InetSocketAddress(monitor.getHostname(), monitor.getPort()),
                        timeout
                );
                responseTime = System.currentTimeMillis() - startTime;

                message = "TCP连接成功，端口: " + monitor.getPort() + "，响应时间: " + responseTime + "ms";
                return new HttpResult(true, message, responseTime);
            } catch (Exception e) {
                lastException = e;
                retryCount++;

                if (retryCount <= maxRetries) {
                    handleRetry(monitor, retryCount, maxRetries);
                }
            }
        }
        log.error("TCP检查异常，重试{}次后仍然失败", maxRetries, lastException);
        return new HttpResult(false, "TCP连接失败: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    @Override
    protected String getCheckerType() {
        return "TCP";
    }
}