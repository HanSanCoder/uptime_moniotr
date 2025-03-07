package io.hansan.monitor.handler;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 22:44
 * @Description：TODO
 */
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.service.MonitorService;
import io.hansan.monitor.service.NotificationService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author 何汉叁
 * @description 刷新处理器类，用于处理客户端刷新事件
 * @date 2025/3/6 22:52
 */
@Component
public class RefreshHandler implements DataListener<String> {
    private static final Logger log = LoggerFactory.getLogger(RefreshHandler.class);

    @Inject
    private MonitorService monitorService;

    @Inject
    private NotificationService notificationService;

    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
        log.debug("Refresh event received from client: {}", client.getSessionId());
        try {
            log.debug("Sending initial data to client: {}", client.getSessionId());
            client.sendEvent("monitorList", monitorService.listAll());
            client.sendEvent("notificationList", notificationService.list());
            log.debug("Initial data sent successfully to client: {}", client.getSessionId());
        } catch (Exception e) {
            log.error("Error sending initial data to client: " + client.getSessionId(), e);
        }
    }
}