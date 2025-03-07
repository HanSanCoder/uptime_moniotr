package io.hansan.monitor.handler;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 22:47
 * @Description：TODO
 */
import com.corundumstudio.socketio.SocketIOServer;
import lombok.Setter;
import org.noear.solon.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 广播更新到连接的客户端的辅助类。
 */
@Component
public class BroadcastHelper {
    private static final Logger log = LoggerFactory.getLogger(BroadcastHelper.class);

    @Setter
    private SocketIOServer server;

    public void broadcastUpdate(Integer monitorId, Object heartbeat) {
        if (server != null) {
            log.debug("Broadcasting update - MonitorId: {}", monitorId);
            server.getBroadcastOperations().sendEvent("monitorUpdate",
                    new MonitorUpdate(monitorId, heartbeat));
        }
    }

    private static class MonitorUpdate {
        private Integer monitorId;
        private Object heartbeat;

        public MonitorUpdate(Integer monitorId, Object heartbeat) {
            this.monitorId = monitorId;
            this.heartbeat = heartbeat;
        }

        public Integer getMonitorId() {
            return monitorId;
        }

        public Object getHeartbeat() {
            return heartbeat;
        }
    }
}
