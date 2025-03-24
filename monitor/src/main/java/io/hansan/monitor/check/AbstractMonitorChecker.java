package io.hansan.monitor.check;


import io.hansan.monitor.configs.WebSocketConfig;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.service.HeartbeatService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractMonitorChecker implements MonitorChecker {
    protected final HeartbeatService heartbeatService;
    protected final WebSocketConfig webSocketServer;

    protected AbstractMonitorChecker(HeartbeatService heartbeatService, WebSocketConfig webSocketServer) {
        this.heartbeatService = heartbeatService;
        this.webSocketServer = webSocketServer;
    }

    protected void handleRetry(MonitorModel monitor, int retryCount, int maxRetries) {
        log.info("{}检查失败，正在进行第{}次重试，共{}次", getCheckerType(), retryCount, maxRetries);
        heartbeatService.addPendingHeartbeat(monitor);
        broadcastHeartbeats(monitor);
        try {
            // 重试前等待一段时间
            Integer retryInterval = (monitor.getRetryInterval() != null ? monitor.getRetryInterval() : 15) * 1000;
            Thread.sleep(retryInterval);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    protected void broadcastHeartbeats(MonitorModel monitor) {
        if (webSocketServer != null && webSocketServer.getSocketIOServer() != null) {
            List<io.hansan.monitor.model.HeartbeatModel> beats = heartbeatService.getBeats(monitor.getId());
            webSocketServer.getSocketIOServer().getBroadcastOperations().sendEvent("heartbeatList", monitor.getId(), beats, true);
        }
    }

    protected abstract String getCheckerType();
}