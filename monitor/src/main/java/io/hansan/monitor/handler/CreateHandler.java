package io.hansan.monitor.handler;

import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.dto.MonitorDTO;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.dto.UserContext;
import io.hansan.monitor.mapper.UserMapper;
import io.hansan.monitor.model.*;
import io.hansan.monitor.service.*;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
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
    @Inject
    private NotificationService notificationService;
    @Inject
    private EmailService emailService;
    @Inject
    private UserService userService;
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
            Result result = maintenanceService.add(data);
            ack.sendAckData(result);
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
            client.sendEvent("monitorList", monitorService.listAll());
        };
    }


    public DataListener<Object[]> addMonitorMaintenance() {
        return (client, data, ack) -> {
            Integer maintenanceId = (Integer) data[0];
            List<?> monitorsList = (List<?>) data[1];
            Object[] affectedMonitors = monitorsList.toArray();

            for (Object monitorObj : affectedMonitors) {
                Map<String, Object> monitor = (Map<String, Object>) monitorObj;
                Integer monitorId = ((Number) monitor.get("id")).intValue();
                maintenanceService.addMonitorMaintenance(maintenanceId, monitorId);
                monitorService.pauseMonitor(monitorId);
                heartbeatService.updateStatue(monitorId, 3);
                List<HeartbeatModel> beats = heartbeatService.getBeats(monitorId);
                client.sendEvent("heartbeatList", monitorId, beats, true);
                client.sendEvent("importantHeartbeatList", monitorId, heartbeatService.getImportantHeartbeats(monitorId), true);
                client.sendEvent("uptime", monitorId, 720, heartbeatService.getUptimeData(monitorId).get("720"));
                client.sendEvent("uptime", monitorId, 24, heartbeatService.getUptimeData(monitorId).get("24"));
                client.sendEvent("avgPing", monitorId, heartbeatService.calculateAveragePing(monitorId));
            }
            Result result = new Result();
            result.setOk(true);
            result.setMsg("添加维护监控事件成功");
            ack.sendAckData(result);
        };
    }

    public DataListener<Object[]> setSettings() {
        return (client, data, ack) -> {
            String currentPassword = (String) data[0];
            String newPassword = (String) data[1];
            Result result = userService.updatePassword(currentPassword, newPassword);
            ack.sendAckData(result);
        };
    }

    public DataListener<NotificationModel> addNotification() {
        return (client, data, ack) -> {
            Result result = notificationService.addNotification(data);
            client.sendEvent("notificationList", notificationService.listAll());
            ack.sendAckData(result);
        };
    }

    public DataListener<NotificationModel> testNotification() {
        return (client, data, ack) -> {
            String email = data.getEmail();
            String subject = data.getSubject();
            boolean inSend = emailService.sendTestEmail(email, subject);
            Result result = new Result();
            if (inSend) {
                result.setOk(true);
                result.setMsg("发送测试邮件成功");
            } else {
                result.setOk(false);
                result.setMsg("发送测试邮件失败");
            }
            ack.sendAckData(result);
        };
    }

    public DataListener<Object[]> setup() {
        return (client, data, ack) -> {
            String username = (String) data[0];
            String password = (String) data[1];
            Result result = userService.createUser(username, password);
            ack.sendAckData(result);
        };
    }

    public DataListener<Void> logout() {
        return (client, data, ack) -> {
            Result result = userService.logout();
            ack.sendAckData(result);
        };
    }

    public DataListener<Integer> setTLSDay() {
        return (client, data, ack) -> {
            Result result = new Result();
            UserContext.setExpiryDay(data);
            result.setOk(true);
            result.setMsg("设置证书通知时间成功");
            result.setExpiryDay(UserContext.getExpiryDay());
            ack.sendAckData(result);
        };
    }

}
