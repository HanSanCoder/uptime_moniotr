package io.hansan.monitor.handler;

import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.dto.MonitorDTO;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.dto.UserContext;
import io.hansan.monitor.model.HeartbeatModel;
import io.hansan.monitor.model.Maintenance;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.model.Tag;
import io.hansan.monitor.service.*;
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
    @Inject
    private UserService userService;
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

    public DataListener<MonitorDTO> editMonitor() {
        return (client, data, ack) -> {
            Result result = monitorService.updateMonitor(data);
            client.sendEvent("monitorList", monitorService.getMonitorsByUserId(1));
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

    public DataListener<Integer> getMaintenance() {
        return (client, data, ack) -> {
            Maintenance maintenance = maintenanceService.findById(data);
            Result result = new Result();
            result.setMsg("获取维护记录成功");
            result.setMaintenance(maintenance);
            ack.sendAckData(result);
        };
    }

    public DataListener<Void> getMaintenanceList() {
        return (client, data, ack) -> {
            List<Maintenance> maintenanceList = maintenanceService.findByUserId(1);
            client.sendEvent("maintenanceList", maintenanceList);
        };
    }

    public DataListener<Integer> pauseMaintenance() {
        return (client, data, ack) -> {
            List<Integer> monitorIds = maintenanceService.findMonitorIdByMaintenanceId(data);
            for (Integer monitorId : monitorIds) {
                monitorService.pauseMonitor(monitorId);
                heartbeatService.updateStatue(monitorId, 1);
                List<HeartbeatModel> beats = heartbeatService.getBeats(monitorId);
                client.sendEvent("heartbeatList", monitorId, beats, true);
                client.sendEvent("importantHeartbeatList", monitorId, heartbeatService.getImportantHeartbeats(monitorId), true);
                client.sendEvent("uptime", monitorId, 720, heartbeatService.getUptimeData(monitorId).get("720"));
                client.sendEvent("uptime", monitorId, 24, heartbeatService.getUptimeData(monitorId).get("24"));
                client.sendEvent("avgPing", monitorId, heartbeatService.calculateAveragePing(monitorId));
            }
            Result result = maintenanceService.pauseMaintenance(data);
            ack.sendAckData(result);
        };
    }

    public DataListener<Integer> resumeMaintenance() {
        return (client, data, ack) -> {
            List<Integer> monitorIds = maintenanceService.findMonitorIdByMaintenanceId(data);
            for (Integer monitorId : monitorIds) {
                monitorService.resumeMonitor(monitorId);
                heartbeatService.updateStatue(monitorId, 3);
                List<HeartbeatModel> beats = heartbeatService.getBeats(monitorId);
                client.sendEvent("heartbeatList", monitorId, beats, true);
                client.sendEvent("importantHeartbeatList", monitorId, heartbeatService.getImportantHeartbeats(monitorId), true);
                client.sendEvent("uptime", monitorId, 720, heartbeatService.getUptimeData(monitorId).get("720"));
                client.sendEvent("uptime", monitorId, 24, heartbeatService.getUptimeData(monitorId).get("24"));
                client.sendEvent("avgPing", monitorId, heartbeatService.calculateAveragePing(monitorId));
            }
            Result result = maintenanceService.resumeMaintenance(data);
            ack.sendAckData(result);
        };
    }

    public DataListener<Maintenance> editMaintenance() {
        return (client, data, ack) -> {
            Result monitor = maintenanceService.editMaintenance(data);
            ack.sendAckData(monitor);
        };
    }

    public DataListener<Object[]> login() {
        return (client, data, ack) -> {
            String username = (String) data[0];
            String password = (String) data[1];
            Result result = userService.login(username, password);
            ack.sendAckData(result);
        };
    }

    public DataListener<Void> getTLSDay() {
        return (client, data, ack) -> {
            ack.sendAckData(UserContext.getExpiryDay());
        };
    }
}

