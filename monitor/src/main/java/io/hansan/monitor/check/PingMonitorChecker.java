
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
                int timeout = monitor.getTimeout() != null ? monitor.getTimeout().intValue() * 1000: 5000; // 默认5秒

                // 设置数据包大小（如果指定）
                Integer packetSize = monitor.getPacketSize();
                boolean isReachable;

                if (packetSize != null && packetSize > 0) {
                    // 使用自定义的Ping命令执行带数据包大小的Ping
                    isReachable = executeCustomPing(address.getHostAddress(), timeout, packetSize);
                } else {
                    // 使用Java内置方法执行标准Ping
                    isReachable = address.isReachable(timeout);
                }
                responseTime = System.currentTimeMillis() - startTime;

                if (isReachable) {
                    message = "Ping成功，响应时间: " + responseTime + "ms";
                    if (packetSize != null && packetSize > 0) {
                        message += "，包大小: " + packetSize + " 字节";
                    }
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
        log.error("Ping检查异常，重试{}次后仍然失败", maxRetries, lastException);
        return new HttpResult(false, "Ping异常: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    /**
     * 执行自定义Ping命令，支持设置数据包大小
     * @param host 主机地址
     * @param timeout 超时时间（毫秒）
     * @param packetSize 数据包大小（字节）
     * @return 是否可达
     */
    private boolean executeCustomPing(String host, int timeout, int packetSize) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            if (os.contains("win")) {
                // Windows系统
                processBuilder = new ProcessBuilder("ping", "-n", "1", "-w", String.valueOf(timeout), "-l", String.valueOf(packetSize), host);
            } else {
                // Linux/Unix/MacOS系统
                processBuilder = new ProcessBuilder("ping", "-c", "1", "-W", String.valueOf(timeout / 1000), "-s", String.valueOf(packetSize), host);
            }

            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.error("执行自定义Ping命令失败", e);
            return false;
        }
    }

    @Override
    protected String getCheckerType() {
        return "Ping";
    }
}