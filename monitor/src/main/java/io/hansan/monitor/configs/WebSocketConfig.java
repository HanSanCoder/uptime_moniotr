package io.hansan.monitor.configs;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import io.hansan.monitor.handler.*;
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
    private RefreshHandler refreshHandler;

    @Inject
    private MonitorHandler monitorHnadler;

    @Inject
    private BroadcastHelper broadcastHelper;

    private SocketIOServer server;

    private SocketIOServer createSocketIOServer() {
        log.info("Initializing Socket.IO server...");
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(3001);
        config.setAllowCustomRequests(true);
        server = new SocketIOServer(config);

        //注册监听事件
        server.addConnectListener(connectionHandler);
        server.addDisconnectListener(disconnectionHandler);
        server.addEventListener("refresh", String.class, refreshHandler);
        server.addEventListener("getMonitor", Integer.class, monitorHnadler.getMonitorDetailListener());

        // Set server to broadcast helper
//        broadcastHelper.setServer(server);

        log.info("Socket.IO 服务器已成功初始化");
        return server;
    }

    public void broadcastUpdate(Integer monitorId, Object heartbeat) {
        broadcastHelper.broadcastUpdate(monitorId, heartbeat);
    }

    @Override
    public void start() throws Throwable {
        if (server == null) {
            server = createSocketIOServer();
        }
        server.start();
        log.info("Socket.IO 服务器在端口 3001 上成功启动");
    }

    @Override
    public void stop() throws Throwable {
        if (server != null) {
            server.stop();
            log.info("已成功停止 Socket.IO 服务器");
        }
    }
}