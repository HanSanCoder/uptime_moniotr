package io.hansan.monitor.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.hansan.monitor.check.HttpResult;
import io.hansan.monitor.check.MonitorChecker;
import io.hansan.monitor.check.MonitorCheckerFactory;
import io.hansan.monitor.configs.WebSocketConfig;
import io.hansan.monitor.mapper.HeartbeatMapper;
import io.hansan.monitor.mapper.MonitorMapper;
import io.hansan.monitor.model.HeartbeatModel;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.service.HeartbeatService;
import io.hansan.monitor.service.MonitorService;
import io.hansan.monitor.service.NotificationService;
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
    private MonitorService monitorService;
    @Inject
    private HeartbeatService heartbeatService;
    @Inject
    private HeartbeatMapper heartbeatMapper;
    @Inject
    private NotificationService notificationService;
    @Inject
    private MonitorCheckerFactory checkerFactory;

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
    @Scheduled(fixedRate = 600000)
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
        boolean important = false;

        try {
            // 使用策略模式获取对应的检查器
            MonitorChecker checker = checkerFactory.getChecker(monitor.getType());
            HttpResult result = checker.check(monitor);
            status = result.isUp() ? 1 : 0;
            message = result.getMessage();
            ping = result.getPing();
            important = result.isImportant();
        } catch (Exception e) {
            status = 0;
            message = "Error: " + e.getMessage();
            log.error("监控检查异常: {}", monitor.getName(), e);
        }

        float responseTime = (System.currentTimeMillis() - startTime) / 1000.0f;

        // 处理检查结果
        processCheckResult(monitor, status, ping, responseTime, message, important);
    }

    /**
     * 处理检查结果
     */
    private void processCheckResult(MonitorModel monitor, int status, float ping, float responseTime, String message, boolean important) {
        try {
            // 获取之前的状态
            Integer previousStatus = heartbeatService.getLatestStatus(monitor.getId(), monitor.getMaxretries() + 1);
            HeartbeatModel heartbeat = new HeartbeatModel();
            heartbeat.setMonitorId(monitor.getId());
            heartbeat.setStatus(status);
            heartbeat.setImportant(important);
            heartbeat.setDuration((int) responseTime);
            heartbeat.setMsg(message);
            heartbeat.setPing((double) ping);
            heartbeat.setTime(java.time.LocalDateTime.now());
            heartbeat.setCreatedAt(java.time.LocalDateTime.now());
            heartbeatMapper.insert(heartbeat);
            // 检查状态是否发生变化
            boolean statusChanged = previousStatus != null && previousStatus != status;
            // 如果状态发生变化，广播状态变化
            if (statusChanged) {
                notificationService.handleStatusChangeNotification(monitor);
                heartbeat.setImportant(true);
                heartbeat.setMsg(String.format("监控状态变化: %s 从 %s 变为 %s", monitor.getName(), previousStatus == 0 ? "down" : "up", status == 0 ? "down" : "up"));
                heartbeatService.updateById(heartbeat);
                log.info("监控状态变化: {} 从 {} 变为 {}", monitor.getName(), previousStatus == 0 ? "down" : "up", status == 0 ? "down" : "up");
            }
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
        } catch (Exception e) {
            log.error("处理检查结果异常", e);
        }
    }

}