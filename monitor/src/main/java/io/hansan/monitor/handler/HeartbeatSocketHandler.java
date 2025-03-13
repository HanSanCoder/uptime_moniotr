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

@Component
public class HeartbeatSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatSocketHandler.class);

    @Inject
    private HeartbeatService heartbeatService;

    /**
     * 获取监控器心跳记录
     */
    public DataListener<Object[]> getHeartbeatList() {
        return (client, data, ack) -> {
            Integer monitorId = 1;
            if (data.length >= 1) {
                monitorId = (Integer) data[0];
            }
                client.sendEvent("heartbeatList", monitorId,
                        heartbeatService.getBeats(monitorId), true);
        };
    }


    /**
     * 获取心跳数据
     */
//    public DataListener<Object[]> heartbeatListener() {
//        return (client, data, ack) -> {
//            if (data.length >= 4) {
//                Integer monitorId = (Integer) data[0];
//                Integer status = (Integer) data[1];
//                String msg = (String) data[2];
//                Double ping = (Double) data[3];
//                Boolean important = data.length >= 5 ? (Boolean) data[4] : false;
//
//                Map<String, Object> heartbeat = heartbeatService.add(monitorId, status, msg, ping, important);
//
//                // 发送至客户端
//                client.getNamespace().getBroadcastOperations()
//                        .sendEvent("heartbeat", heartbeat);
//
//                if (ack.isAckRequested()) {
//                    ack.sendAckData(heartbeat);
//                }
//            }
//        };
//    }



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
}