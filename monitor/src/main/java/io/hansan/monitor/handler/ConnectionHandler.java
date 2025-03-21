package io.hansan.monitor.handler;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 22:39
 * @Description：TODO
 */
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import io.hansan.monitor.model.HeartbeatModel;
import io.hansan.monitor.service.*;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author 何汉叁
 * @description 连接处理器类，用于处理客户端连接事件
 * @date 2025/3/6 22:51
 */
@Component
public class ConnectionHandler implements ConnectListener {
    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);
    @Inject
    private MonitorService monitorService;
    @Inject
    private NotificationService notificationService;
    @Inject
    private HeartbeatService heartbeatService;
    @Inject
    private MaintenanceService maintenanceService;
    @Override
    public void onConnect(SocketIOClient client) {
        log.info("客户端连接 - 会话ID: {}, 远程地址: {}",
                client.getSessionId(), client.getRemoteAddress());
        // 监控数据
        client.sendEvent("monitorList", monitorService.listAll());
        // 通知数据
        client.sendEvent("notificationList", notificationService.listAll());
        // 发送心跳数据
        List<Integer> monitorIds = monitorService.listAllIds();
        for (Integer monitorId : monitorIds) {
            List<HeartbeatModel> beats = heartbeatService.getBeats(monitorId);
            client.sendEvent("heartbeatList", monitorId, beats, true);
            client.sendEvent("importantHeartbeatList", monitorId, heartbeatService.getImportantHeartbeats(monitorId), true);
            client.sendEvent("uptime", monitorId, 720, heartbeatService.getUptimeData(monitorId).get("720"));
            client.sendEvent("uptime", monitorId, 24, heartbeatService.getUptimeData(monitorId).get("24"));
            client.sendEvent("avgPing", monitorId, heartbeatService.calculateAveragePing(monitorId));
        }
//        client.sendEvent("heartbeat", heartbeatSocketHandler.heartbeatListener());
        // 发送维护列表
        client.sendEvent("maintenanceList", maintenanceService.findByUserId(1));

    }
}
