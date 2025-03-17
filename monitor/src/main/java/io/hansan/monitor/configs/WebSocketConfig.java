package io.hansan.monitor.configs;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.dto.MonitorDTO;
import io.hansan.monitor.handler.*;
import io.hansan.monitor.model.Maintenance;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.model.Tag;
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
    private FindHandler findHandler;
    @Inject
    private CreateHandler createHandler;
    @Inject
    private DeleteHandler deleteHandler;
    private SocketIOServer server;

    public SocketIOServer getSocketIOServer() {
        return server;
    }

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(3001);
        config.setAllowCustomRequests(true);
        return new SocketIOServer(config);
    }

    @Override
    public void start() throws Throwable {
        server = socketIOServer();

        server.addConnectListener(connectionHandler);
        server.addDisconnectListener(disconnectionHandler);
        server.addEventListener("refresh", String.class, getRefreshListener());
        server.addEventListener("getMonitorList", Void.class, findHandler.getMonitorList());
        server.addEventListener("getTags", Void.class, findHandler.getTags());
        server.addEventListener("add", MonitorDTO.class, createHandler.addMonitor());
        server.addEventListener("addMaintenance", Maintenance.class, createHandler.addMaintenance());
        server.addEventListener("addTag", Tag.class, createHandler.addTag());
        server.addEventListener("deleteTag", Integer.class, deleteHandler.deleteTag());
        server.addEventListener("addMonitorTag", Object[].class, createHandler.addMonitorTag());
        server.addEventListener("deleteMonitorTag", Object[].class, deleteHandler.deleteMonitorTag());
        server.addEventListener("getMonitorMaintenance", Integer.class, findHandler.getMonitorMaintenance());
        server.addEventListener("addMonitorMaintenance", Object[].class, createHandler.addMonitorMaintenance());
        server.addEventListener("setSettings", Integer.class, createHandler.setSettings());
        server.addEventListener("getSettings", Integer.class, findHandler.getSettings());
        server.addEventListener("editTag", Tag.class, findHandler.editTag());
        server.addEventListener("deleteMonitor", Integer.class, deleteHandler.deleteMonitor());
        server.addEventListener("editMonitor", MonitorModel.class, findHandler.editMonitor());
        server.addEventListener("getMonitor", Integer.class, findHandler.getMonitor());

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