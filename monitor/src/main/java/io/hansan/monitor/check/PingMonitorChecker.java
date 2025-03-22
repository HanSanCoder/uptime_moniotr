
package io.hansan.monitor.check;

import io.hansan.monitor.configs.WebSocketConfig;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.service.HeartbeatService;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

@Slf4j
public class PingMonitorChecker extends AbstractMonitorChecker {

    public PingMonitorChecker(HeartbeatService heartbeatService, WebSocketConfig webSocketServer) {
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
            try {
                InetAddress address = InetAddress.getByName(monitor.getHostname());
                long startTime = System.currentTimeMillis();

                // 设置超时
                int timeout = 5000; // 默认5秒
                if (monitor.getCheck_interval() != null) {
                    timeout = monitor.getCheck_interval() * 1000;
                }

                boolean isReachable = address.isReachable(timeout);
                responseTime = System.currentTimeMillis() - startTime;

                if (isReachable) {
                    message = "Ping成功，响应时间: " + responseTime + "ms";
                    return new HttpResult(true, message, responseTime);
                } else if (retryCount < maxRetries) {
                    // 如果Ping失败但还有重试次数，则继续重试
                    retryCount++;
                    handleRetry(monitor, retryCount, maxRetries);
                    continue;
                } else {
                    // 所有重试都失败
                    message = "Ping失败，目标主机无响应，已重试" + maxRetries + "次";
                    return new HttpResult(false, message, responseTime);
                }
            } catch (Exception e) {
                lastException = e;
                retryCount++;

                if (retryCount <= maxRetries) {
                    handleRetry(monitor, retryCount, maxRetries);
                }
            }
        }
        // 所有重试都失败
        log.error("Ping检查异常，重试{}次后仍然失败", maxRetries, lastException);
        return new HttpResult(false, "Ping异常: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    @Override
    protected String getCheckerType() {
        return "Ping";
    }
}