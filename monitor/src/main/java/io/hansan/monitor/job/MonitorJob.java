package io.hansan.monitor.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.hansan.monitor.configs.WebSocketConfig;
import io.hansan.monitor.mapper.HeartbeatMapper;
import io.hansan.monitor.mapper.MonitorMapper;
import io.hansan.monitor.model.HeartbeatModel;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.service.HeartbeatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.annotation.Scheduled;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class MonitorJob {

    @Inject
    private MonitorMapper monitorMapper;

    @Inject
    private HeartbeatService heartbeatService;
    @Inject
    private HeartbeatMapper heartbeatMapper;

    @Inject("${monitor.retention-days:30}")
    private Integer retentionDays;

    // 创建线程池用于并行执行监控任务
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // WebSocket 服务，用于广播状态变化
    @Inject
    private WebSocketConfig webSocketServer;

    /**
     * 定期检查监控项状态
     */
    @Scheduled(fixedRate = 6000000)
    public void checkMonitors() {
        try {
            List<MonitorModel> activeMonitors = monitorMapper.findActiveMonitorsDueForCheck();
            // 针对每个需要检查的监控项，提交到线程池执行
            for (MonitorModel monitor : activeMonitors) {
                executorService.submit(() -> performCheck(monitor));
            }
        } catch (Exception e) {
            log.error("监控检查任务异常", e);
        }
    }

    /**
     * 清理老旧的心跳数据
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldHeartbeats() {
        try {
            log.info("开始清理老旧心跳数据...");

            // 计算截止时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -retentionDays);
            Date cutoffTime = calendar.getTime();

            // 删除老旧数据
            int deleted = heartbeatMapper.delete(
                    new LambdaQueryWrapper<HeartbeatModel>()
                            .lt(HeartbeatModel::getTime, cutoffTime)
            );

            log.info("清理完成，共删除 {} 条老旧心跳数据", deleted);
        } catch (Exception e) {
            log.error("清理老旧心跳数据异常", e);
        }
    }

    /**
     * 执行具体的监控检查
     */
    private void performCheck(MonitorModel monitor) {
        long startTime = System.currentTimeMillis();
        int status;
        String message = null;
        float ping = 0;

        try {
            // 根据监控类型执行不同的检查
            switch (monitor.getType()) {
                case "http":
                    HttpResult httpResult = checkHttp(monitor);
                    status = httpResult.isUp() ? 1 : 0;
                    message = httpResult.getMessage();
                    ping = httpResult.getPing();
                    break;

                case "ping":
                    HttpResult pingResult = checkPing(monitor);
                    status = pingResult.isUp() ? 1 : 0;
                    message = pingResult.getMessage();
                    ping = pingResult.getPing();
                    break;

                case "tcp":
                    HttpResult tcpResult = checkTcp(monitor);
                    status = tcpResult.isUp() ? 1 : 0;
                    message = tcpResult.getMessage();
                    ping = tcpResult.getPing();
                    break;

                default:
                    log.warn("未知的监控类型: {}", monitor.getType());
                    return;
            }
        } catch (Exception e) {
            status = 0;
            message = "Error: " + e.getMessage();
            log.error("监控检查异常: {}", monitor.getName(), e);
        }

        // 检查完成后的响应时间
        float responseTime = (System.currentTimeMillis() - startTime) / 1000.0f;

        // 处理检查结果
        processCheckResult(monitor, status, ping, responseTime, message);
    }

    /**
     * 处理检查结果
     */
    private void processCheckResult(MonitorModel monitor, int status, float ping, float responseTime, String message) {
        try {
            // 获取之前的状态
            Integer previousStatus = monitor.getMaxretries() > 0 ? monitor.getMaxretries() : 0;

            // 创建心跳记录
            HeartbeatModel heartbeat = new HeartbeatModel();
            heartbeat.setMonitorId(monitor.getId());
            heartbeat.setStatus(status);
            heartbeat.setDuration((int) responseTime);
            heartbeat.setMsg(message);
            heartbeat.setPing((double) ping);
            heartbeat.setTime(java.time.LocalDateTime.now());
            heartbeat.setCreatedAt(java.time.LocalDateTime.now());

            // 保存心跳记录
            heartbeatMapper.insert(heartbeat);

            // 更新监控状态
            // 注意：根据您的数据库结构，monitor表里似乎没有存储状态的字段，所以这里我们暂不设置状态
            // 如果需要可以考虑添加字段或使用maxretries存储当前重试次数

            // 如果状态是"down"，增加重试计数；否则重置为0
            if (status == 0) {
                monitor.setMaxretries(previousStatus + 1);
            } else {
                monitor.setMaxretries(0);
            }

            // 保存监控状态
            monitorMapper.updateById(monitor);

            // 检查状态是否发生变化
            boolean statusChanged = previousStatus != null && (previousStatus == 0 ? status != 0 : status == 0);

            // 广播心跳数据
            if (webSocketServer != null && webSocketServer.getSocketIOServer() != null) {
                List<HeartbeatModel> beats = heartbeatService.getBeats(monitor.getId());
                webSocketServer.getSocketIOServer().getBroadcastOperations().sendEvent("heartbeatList", monitor.getId(), beats, true);
                webSocketServer.getSocketIOServer().getBroadcastOperations().sendEvent("importantHeartbeatList", monitor.getId(),
                        heartbeatService.getImportantHeartbeats(monitor.getId()), true);
                webSocketServer.getSocketIOServer().getBroadcastOperations().sendEvent("uptime", monitor.getId(), 720,
                        heartbeatService.getUptimeData(monitor.getId()).get("720"));
                webSocketServer.getSocketIOServer().getBroadcastOperations().sendEvent("uptime", monitor.getId(), 24,
                        heartbeatService.getUptimeData(monitor.getId()).get("24"));
                webSocketServer.getSocketIOServer().getBroadcastOperations().sendEvent("avgPing", monitor.getId(),
                        heartbeatService.calculateAveragePing(monitor.getId()));
            }

            // 如果状态发生变化，广播状态变化
            if (statusChanged && webSocketServer != null && webSocketServer.getSocketIOServer() != null) {
                webSocketServer.getSocketIOServer().getBroadcastOperations().sendEvent("statusChange", monitor);
                log.info("监控状态变化: {} 从 {} 变为 {}", monitor.getName(), previousStatus == 0 ? "up" : "down", status == 0 ? "down" : "up");
            }
        } catch (Exception e) {
            log.error("处理检查结果异常", e);
        }
    }

    /**
     * 检查HTTP服务
     */
    private HttpResult checkHttp(MonitorModel monitor) {
        int retryCount = 0;
        int maxRetries = monitor.getMaxretries() != null ? monitor.getMaxretries() : 3; // 默认最大重试3次
        Exception lastException = null;

        while (retryCount <= maxRetries) {
            try {
                // 创建HTTP请求
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
                    log.info("HTTP检查失败，正在进行第{}次重试，共{}次", retryCount, maxRetries);
                    try {
                        // 重试前等待一段时间
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // 所有重试都失败
        log.error("HTTP检查异常，重试{}次后仍然失败", maxRetries, lastException);
        return new HttpResult(false, "Error: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    /**
     * 检查Ping
     */
    private HttpResult checkPing(MonitorModel monitor) {
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
                    log.info("Ping检查失败，正在进行第{}次重试，共{}次", retryCount, maxRetries);
                    try {
                        Thread.sleep(1000); // 重试前等待1秒
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
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
                    log.info("Ping检查异常，正在进行第{}次重试，共{}次", retryCount, maxRetries);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        // 所有重试都失败
        log.error("Ping检查异常，重试{}次后仍然失败", maxRetries, lastException);
        return new HttpResult(false, "Ping异常: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    /**
     * 检查TCP连接
     */
    private HttpResult checkTcp(MonitorModel monitor) {
        int retryCount = 0;
        int maxRetries = monitor.getMaxretries() != null ? monitor.getMaxretries() : 3; // 默认最大重试3次
        Exception lastException = null;

        while (retryCount <= maxRetries) {
            String message;
            long responseTime = 0;
            try (Socket socket = new Socket()) {
                // 设置超时
                int timeout = 5000; // 默认5秒
                if (monitor.getCheck_interval() != null) {
                    timeout = monitor.getCheck_interval() * 1000;
                }

                log.debug("开始TCP检查: {}, 主机名: {}, 端口: {}, 超时: {}ms",
                        monitor.getName(), monitor.getHostname(), monitor.getPort(), timeout);

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
                    log.info("TCP检查失败，正在进行第{}次重试，共{}次", retryCount, maxRetries);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // 所有重试都失败
        log.error("TCP检查异常，重试{}次后仍然失败", maxRetries, lastException);
        return new HttpResult(false, "TCP连接失败: " + (lastException != null ? lastException.getMessage() : "未知错误"));
    }

    /**
     * HTTP检查结果
     */
    private static class HttpResult {
        private final boolean up;
        private final String message;
        private final long ping;

        public HttpResult(boolean up, String message) {
            this(up, message, 0);
        }

        public HttpResult(boolean up, String message, long ping) {
            this.up = up;
            this.message = message;
            this.ping = ping;
        }

        public boolean isUp() {
            return up;
        }

        public String getMessage() {
            return message;
        }

        public long getPing() {
            return ping;
        }
    }
}