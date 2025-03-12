package io.hansan.monitor.exception;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ExceptionListener;
import io.netty.channel.ChannelHandlerContext;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author 何汉叁
 * @description Socket.IO全局异常处理配置
 * @date 2025/3/9 15:30
 */
@Configuration
public class SocketIOExceptionConfig {
    private static final Logger log = LoggerFactory.getLogger(SocketIOExceptionConfig.class);

    @Bean
    public ExceptionListener exceptionListener() {
        return new ExceptionListener() {
            @Override
            public void onEventException(Exception e, List<Object> args, SocketIOClient client) {
                handleException(e, client, "事件处理异常");
            }

            @Override
            public void onDisconnectException(Exception e, SocketIOClient client) {
                handleException(e, client, "断开连接异常");
            }

            @Override
            public void onConnectException(Exception e, SocketIOClient client) {
                handleException(e, client, "建立连接异常");
            }

            @Override
            public void onPingException(Exception e, SocketIOClient client) {
                handleException(e, client, "Ping异常");
            }

            @Override
            public void onPongException(Exception e, SocketIOClient client) {
                handleException(e, client, "Pong异常");
            }

            @Override
            public void onAuthException(Throwable e, SocketIOClient client) {
                handleException(new Exception(e), client, "认证异常");
            }

            @Override
            public boolean exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
                log.error("Socket.IO通道异常: {}", e.getMessage(), e);
                return true; // 异常已处理
            }

            private void handleException(Exception e, SocketIOClient client, String type) {
                String sessionId = client != null ? client.getSessionId().toString() : "未知";
                log.error("Socket.IO {} - 会话ID: {}, 错误信息: {}", type, sessionId, e.getMessage(), e);

                if (client != null && client.isChannelOpen()) {
                    try {
                        // 向客户端发送错误信息
                        client.sendEvent("error", "服务器处理请求时发生错误: " + e.getMessage());
                    } catch (Exception ex) {
                        log.error("向客户端发送错误信息时出错: {}", ex.getMessage(), ex);
                    }
                }
            }
        };
    }

    @Bean
    public com.corundumstudio.socketio.Configuration socketIOConfiguration(ExceptionListener exceptionListener) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        // 设置异常处理器
        config.setExceptionListener(exceptionListener);

        log.info("Socket.IO全局异常处理器已配置");
        return config;
    }
}