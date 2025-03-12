package io.hansan.monitor.configs;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.handler.*;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketConfig implements LifecycleBean {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Inject
    private ConnectionHandler connectionHandler;

    @Inject
    private DisconnectionHandler disconnectionHandler;

    @Inject
    private InitMonitorHandler initMonitorHandler;
    @Inject
    private BroadcastHelper broadcastHelper;

    private SocketIOServer server;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(3001);
        config.setAllowCustomRequests(true);
        return new SocketIOServer(config);
    }

    public void broadcastUpdate(Integer monitorId, Object heartbeat) {
        broadcastHelper.broadcastUpdate(monitorId, heartbeat);
    }

    @Override
    public void start() throws Throwable {
        server = socketIOServer();

        server.addConnectListener(connectionHandler);
        server.addDisconnectListener(disconnectionHandler);
        server.addEventListener("refresh", String.class, getRefreshListener());
        server.addEventListener("getMonitor", Integer.class, initMonitorHandler.getMonitorDetailListener());
        server.start();
    }

    @Override
    public void stop() throws Throwable {
            server.stop();
            log.info("已成功停止 Socket.IO 服务器");
    }

    // 处理刷新事件
    public DataListener<String> getRefreshListener() {
        return (client, data, ack) -> {
            String userId = client.get("userID");
            String currentSocketId = client.getSessionId().toString();

            server.getAllClients().forEach(socket -> {
                String socketUserId = socket.get("userID");
                if (userId != null && userId.equals(socketUserId) &&
                        !socket.getSessionId().toString().equals(currentSocketId)) {
                    socket.sendEvent("refresh");
                    socket.disconnect();
                }
            });
        };
    }
}