package io.hansan.monitor.handler;

import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.model.HeartbeatModel;
import io.hansan.monitor.service.*;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/15 23:42
 * @Description：TODO
 */
@Component
public class DeleteHandler {
    @Inject
    private MonitorService monitorService;
    @Inject
    private HeartbeatService heartbeatService;
    @Inject
    private MaintenanceService maintenanceService;
    @Inject
    private TagService tagService;
    @Inject
    private NotificationService notificationService;
    public DataListener<Integer> deleteTag() {
        return (client, data, ack) -> {
            Result result = tagService.deleteTag(data);
            ack.sendAckData(result);
        };
    }

    public DataListener<Object[]> deleteMonitorTag() {
        return (client, data, ack) -> {
            Integer tagId = (Integer) data[0];
            Integer monitorId = (Integer) data[1];
            Result result = tagService.deleteMonitorTag(tagId, monitorId);
            ack.sendAckData(result);
        };
    }

    public DataListener<Integer> deleteMonitor() {
        return (client, data, ack) -> {
            Result result = monitorService.deleteMonitor(data);
            ack.sendAckData(result);
        };
    }

    public DataListener<Integer> clearHeartbeats() {
        return (client, data, ack) -> {
            Result result = heartbeatService.removeByMonitorId(data);
            List<HeartbeatModel> beats = heartbeatService.getBeats(data);
            client.sendEvent("heartbeatList", data, beats, true);
            client.sendEvent("importantHeartbeatList", data, heartbeatService.getImportantHeartbeats(data), true);
            ack.sendAckData(result);
        };
    }

    public DataListener<Integer> clearEvents() {
        return (client, data, ack) -> {
            Result result = heartbeatService.removeEventsByMonitorId(data);
            List<HeartbeatModel> beats = heartbeatService.getBeats(data);
            client.sendEvent("heartbeatList", data, beats, true);
            client.sendEvent("importantHeartbeatList", data, heartbeatService.getImportantHeartbeats(data), true);
            ack.sendAckData(result);
        };
    }

    public DataListener<Integer> deleteMaintenance() {
        return (client, data, ack) -> {
            List<Integer> monitorIds = maintenanceService.findMonitorIdByMaintenanceId(data);
            for (Integer monitorId : monitorIds) {
                heartbeatService.updateStatue(monitorId, 1);
                List<HeartbeatModel> beats = heartbeatService.getBeats(monitorId);
                client.sendEvent("heartbeatList", monitorId, beats, true);
            }
            Result result = maintenanceService.deleteMaintenance(data);
            ack.sendAckData(result);
        };
    }

    public DataListener<Integer> deleteNotification() {
        return (client, data, ack) -> {
            Result result = notificationService.deleteNotification(data);
            client.sendEvent("notificationList", notificationService.listAll());
            ack.sendAckData(result);
        };
    }
}
