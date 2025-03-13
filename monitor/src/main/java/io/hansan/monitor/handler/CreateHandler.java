package io.hansan.monitor.handler;

import com.corundumstudio.socketio.listener.DataListener;
import org.noear.solon.annotation.Component;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/13 13:59
 * @Description：TODO
 */
@Component
public class CreateHandler {

    public DataListener<Object[]> addMonitor() {
        return (client, data, ack) -> {

        };
    }
}
