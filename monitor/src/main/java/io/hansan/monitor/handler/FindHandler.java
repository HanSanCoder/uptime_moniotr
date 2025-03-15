package io.hansan.monitor.handler;

import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.dto.MonitorDTO;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.model.Tag;
import io.hansan.monitor.service.HeartbeatService;
import io.hansan.monitor.service.MaintenanceService;
import io.hansan.monitor.service.MonitorService;
import io.hansan.monitor.service.TagService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/15 20:17
 * @Description：TODO
 */
@Component
public class FindHandler {
    @Inject
    private MonitorService monitorService;
    @Inject
    private HeartbeatService heartbeatService;
    @Inject
    private MaintenanceService maintenanceService;
    @Inject
    private TagService tagService;
    /**
     * 处理添加监控事件
     */
    public DataListener<Integer> getMonitorMaintenance() {
        return (client, data, ack) -> {
            Result monitor = monitorService.getMonitorMaintenance(data);
            ack.sendAckData(monitor);
        };
    }

    public DataListener<Integer> getSettings() {
        return (client, data, ack) -> {
            Result settings = new Result();
            settings.setData("获取设置功能稍后完善");
            ack.sendAckData(settings);
        };
    }

    public DataListener<Tag> editTag() {
        return (client, data, ack) -> {
            Result result = tagService.editTag(data);
            ack.sendAckData(result);
        };
    }
}
