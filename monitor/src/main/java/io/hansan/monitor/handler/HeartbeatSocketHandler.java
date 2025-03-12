package io.hansan.monitor.handler;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 22:36
 * @Description：TODO
 */

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import io.hansan.monitor.service.HeartbeatService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class HeartbeatSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatSocketHandler.class);

    @Inject
    private HeartbeatService heartbeatService;

    /**
     * 获取监控器心跳记录
     */
    public DataListener<Object[]> getMonitorBeatsListener() {
        return (client, data, ack) -> {
            if (data.length >= 2) {
                Integer monitorId = (Integer) data[0];
                Integer period = (Integer) data[1];

                client.sendEvent("heartbeatList", monitorId,
                        heartbeatService.getBeats(monitorId, period), false);

                if (ack.isAckRequested()) {
                    ack.sendAckData(heartbeatService.getBeats(monitorId, period));
                }
            }
        };
    }

    /**
     * 清除心跳记录
     */
    public DataListener<Integer> clearHeartbeatsListener() {
        return (client, monitorId, ack) -> {
            boolean success = heartbeatService.clearHeartbeats(monitorId);

            // 广播空心跳列表
            client.getNamespace().getBroadcastOperations()
                    .sendEvent("heartbeatList", monitorId, Collections.emptyList(), true);

            if (ack.isAckRequested()) {
                Map<String, Object> result = new HashMap<>();
                result.put("ok", success);
                ack.sendAckData(result);
            }
        };
    }

    /**
     * 广播重要心跳列表
     */
    public DataListener<Integer> importantHeartbeatsListener() {
        return (client, monitorId, ack) -> {
            client.sendEvent("importantHeartbeatList", monitorId,
                    heartbeatService.getImportantHeartbeats(monitorId, 50), false);
        };
    }

    /**
     * 获取心跳数据
     */
    public DataListener<Object[]> heartbeatListener() {
        return (client, data, ack) -> {
            if (data.length >= 4) {
                Integer monitorId = (Integer) data[0];
                Integer status = (Integer) data[1];
                String msg = (String) data[2];
                Double ping = (Double) data[3];
                Boolean important = data.length >= 5 ? (Boolean) data[4] : false;

                Map<String, Object> heartbeat = heartbeatService.add(monitorId, status, msg, ping, important);

                // 发送至客户端
                client.getNamespace().getBroadcastOperations()
                        .sendEvent("heartbeat", heartbeat);

                if (ack.isAckRequested()) {
                    ack.sendAckData(heartbeat);
                }
            }
        };
    }

    /**
     * 获取所有最新心跳数据
     */
    public DataListener<Void> getAllLastHeartbeatListener() {
        return (client, data, ack) -> {
            Map<String, Map<String, Object>> heartbeats = heartbeatService.getAllLastHeartbeat();

            client.sendEvent("heartbeatList", heartbeats);

            if (ack.isAckRequested()) {
                ack.sendAckData(heartbeats);
            }
        };
    }

    /**
     * 获取平均响应时间
     */
    public DataListener<Integer> getAvgPingListener() {
        return (client, monitorId, ack) -> {
            Double avgPing = heartbeatService.getAveragePing(monitorId);

            client.sendEvent("avgPing", monitorId, avgPing);

            if (ack.isAckRequested()) {
                ack.sendAckData(avgPing);
            }
        };
    }

    /**
     * 获取正常运行时间
     */
    public DataListener<Object[]> getUptimeListener() {
        return (client, data, ack) -> {
            if (data.length >= 2) {
                Integer monitorId = (Integer) data[0];
                Integer period = (Integer) data[1];

                Double uptime = heartbeatService.getUptime(monitorId, period);

                client.sendEvent("uptime", monitorId, period, uptime);

                if (ack.isAckRequested()) {
                    ack.sendAckData(uptime);
                }
            }
        };
    }
}