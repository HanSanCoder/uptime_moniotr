package io.hansan.monitor.handler;

import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.dto.MonitorDTO;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.model.Maintenance;
import io.hansan.monitor.model.Tag;
import io.hansan.monitor.service.HeartbeatService;
import io.hansan.monitor.service.MaintenanceService;
import io.hansan.monitor.service.MonitorService;
import io.hansan.monitor.service.TagService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.Map;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/13 13:59
 * @Description：TODO
 */
@Component
public class CreateHandler {
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
    public DataListener<MonitorDTO> addMonitor() {
        return (client, data, ack) -> {
            Result monitor = monitorService.addMonitor(data);
            ack.sendAckData(monitor);
        };
    }

    /**
     * 处理添加维护事件
     */
    public DataListener<Maintenance> addMaintenance() {
        return (client, data, ack) -> {
            maintenanceService.add(data);
            ack.sendAckData("添加维护事件成功");
        };
    }

    public DataListener<Tag> addTag() {
        return (client, data, ack) -> {
            Result result = tagService.addTag(data);
            ack.sendAckData(result);
        };
    }

    public DataListener<Object[]> addMonitorTag() {
        return (client, data, ack) -> {
            Integer tagId = (Integer) data[0];
            Integer monitorId = (Integer) data[1];
            Result result = tagService.addMonitorTag(tagId, monitorId);
            ack.sendAckData(result);
        };
    }


    public DataListener<Object[]> addMonitorMaintenance() {
        return (client, data, ack) -> {
            Integer maintenanceId = (Integer) data[0];
            Object[] affectedMonitors = (Object[]) data[1];

            for (Object monitorObj : affectedMonitors) {
                Map<String, Object> monitor = (Map<String, Object>) monitorObj;
                Integer monitorId = ((Number) monitor.get("id")).intValue();
                maintenanceService.addMonitorMaintenance(maintenanceId, monitorId);
            }
            ack.sendAckData("添加维护监控事件成功");
        };
    }

    public DataListener<Integer> setSettings() {
        return (client, data, ack) -> {
            ack.sendAckData("设置功能稍后完善");
        };
    }
}
