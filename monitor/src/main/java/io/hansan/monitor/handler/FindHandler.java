package io.hansan.monitor.handler;

import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.dto.MonitorDTO;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.model.Tag;
import io.hansan.monitor.service.HeartbeatService;
import io.hansan.monitor.service.MaintenanceService;
import io.hansan.monitor.service.MonitorService;
import io.hansan.monitor.service.TagService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

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

    public DataListener<Integer> getMonitor() {
        return (client, data, ack) -> {
            Result result = monitorService.getByMonitorId(data);
            ack.sendAckData(result);
        };
    }

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

    public DataListener<Void> getMonitorList() {
        return (client, data, ack) -> {
            List<MonitorModel> monitorList = monitorService.getMonitorsByUserId(1);
            ack.sendAckData(monitorList);
            client.sendEvent("monitorList", monitorService.getMonitorsByUserId(1));
        };
    }

    public DataListener<MonitorModel> editMonitor() {
        return (client, data, ack) -> {
            Result result = monitorService.updateMonitor(data);
            ack.sendAckData(result);
        };
    }

    public DataListener<Integer> pauseMonitor() {
        return (client, data, ack) -> {
            Result result = monitorService.pauseMonitor(data);
            List<MonitorModel> monitorList = monitorService.getMonitorsByUserId(1);
            client.sendEvent("monitorList", monitorList);
            ack.sendAckData(result);
        };
    }

    public DataListener<Integer> resumeMonitor() {
        return (client, data, ack) -> {
            Result result = monitorService.resumeMonitor(data);
            List<MonitorModel> monitorList = monitorService.getMonitorsByUserId(1);
            client.sendEvent("monitorList", monitorList);
            ack.sendAckData(result);
        };
    }

    public DataListener<Object[]> getMonitorBeats() {
        return (client, data, ack) -> {
            Integer monitorId = Integer.valueOf(String.valueOf(data[0]));
            Integer newPeriod = Integer.valueOf(String.valueOf(data[1]));
            Result monitor = heartbeatService.getCustomPeriodHeartbeats(monitorId, newPeriod);
            ack.sendAckData(monitor);
        };
    }
}

