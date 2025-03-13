package io.hansan.monitor.handler;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/7 10:08
 * @Description：TODO
 */
import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.service.MonitorService;
import io.hansan.monitor.service.TagService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 通用监控处理器类，处理多种监控相关事件
 */
@Component
public class InitMonitorHandler {
    private static final Logger log = LoggerFactory.getLogger(InitMonitorHandler.class);

    @Inject
    private MonitorService monitorService;
    @Inject
    private TagService tagService;
    // 处理获取监控详情事件
    public DataListener<Integer> getMonitorDetailListener() {
        return (client, userId, ack) -> {
            client.sendEvent("getMonitor", monitorService.getMonitorsByUserId(userId));
        };
    }


    // 处理获取标签列表事件
    public DataListener<Void> getTags() {
        return (client, data, ack) -> {
            ack.sendAckData(tagService.listAll());
        };
    }

}
