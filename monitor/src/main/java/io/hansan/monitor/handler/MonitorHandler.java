package io.hansan.monitor.handler;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/7 10:08
 * @Description：TODO
 */
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.service.MonitorService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 通用监控处理器类，处理多种监控相关事件
 */
@Component
public class MonitorHandler {
    private static final Logger log = LoggerFactory.getLogger(MonitorHandler.class);

    @Inject
    private MonitorService monitorService;

    // 处理获取监控详情事件
    public DataListener<Integer> getMonitorDetailListener() {
        return (client, monitorId, ack) -> {
            log.debug("获取监控详情事件 - 会话ID: {}, 监控ID: {}", client.getSessionId(), monitorId);
            try {
                client.sendEvent("monitorList", monitorService.getById(monitorId));
            } catch (Exception e) {
                log.error("向客户端发送监控详情时出错: " + client.getSessionId(), e);
            }
        };
    }

    // 处理创建监控事件
    public DataListener<MonitorModel> createMonitorListener() {
        return (client, monitor, ack) -> {
            log.debug("创建监控事件 - 会话ID: {}, 监控名称: {}", client.getSessionId(), monitor.getName());
            try {
                boolean save = monitorService.save(monitor);
                client.sendEvent("monitorCreated", save);
            } catch (Exception e) {
                log.error("创建监控时出错: " + client.getSessionId(), e);
                client.sendEvent("error", "创建监控失败：" + e.getMessage());
            }
        };
    }

    // 处理更新监控状态事件
    public DataListener<Map<String, Object>> updateMonitorStatusListener() {
        return (client, data, ack) -> {
            try {
                Integer monitorId = (Integer) data.get("id");
                Boolean active = (Boolean) data.get("active");

                log.debug("更新监控状态事件 - 会话ID: {}, 监控ID: {}, 状态: {}",
                         client.getSessionId(), monitorId, active);

                client.sendEvent("statusUpdated", true);
            } catch (Exception e) {
                log.error("更新监控状态时出错: " + client.getSessionId(), e);
                client.sendEvent("error", "更新监控状态失败：" + e.getMessage());
            }
        };
    }
}