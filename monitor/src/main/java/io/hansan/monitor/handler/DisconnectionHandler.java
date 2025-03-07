package io.hansan.monitor.handler;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 22:42
 * @Description：TODO
 */
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import org.noear.solon.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DisconnectionHandler implements DisconnectListener {
    private static final Logger log = LoggerFactory.getLogger(DisconnectionHandler.class);
    @Override
    public void onDisconnect(SocketIOClient client) {
        log.info("客户端断开连接 - 会话ID: {}, 远程地址: {}",
                client.getSessionId(), client.getRemoteAddress());
    }
}