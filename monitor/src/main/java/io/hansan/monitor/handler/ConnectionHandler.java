package io.hansan.monitor.handler;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 22:39
 * @Description：TODO
 */
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import io.hansan.monitor.service.HeartbeatService;
import io.hansan.monitor.service.MonitorService;
import io.hansan.monitor.service.NotificationService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private HeartbeatSocketHandler heartbeatSocketHandler;
    @Inject
    private HeartbeatService heartbeatService;
    @Override
    public void onConnect(SocketIOClient client) {
        log.info("客户端连接 - 会话ID: {}, 远程地址: {}",
                client.getSessionId(), client.getRemoteAddress());
        // 发送初始化数据
        client.sendEvent("monitorList", monitorService.listAll());
        client.sendEvent("notificationList", notificationService.listAll());
//        client.sendEvent("heartbeatList", heartbeatService.listAll());
        client.sendEvent("heartbeat", heartbeatSocketHandler.heartbeatListener());
    }
}
