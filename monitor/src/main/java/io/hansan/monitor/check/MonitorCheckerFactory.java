package io.hansan.monitor.check;


import io.hansan.monitor.configs.WebSocketConfig;
import io.hansan.monitor.service.HeartbeatService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.HashMap;
import java.util.Map;

@Component
public class MonitorCheckerFactory {
    private final Map<String, MonitorChecker> checkers = new HashMap<>();

    public MonitorCheckerFactory(HeartbeatService heartbeatService, WebSocketConfig webSocketServer) {
        checkers.put("http", new HttpMonitorChecker(heartbeatService, webSocketServer));
        checkers.put("ping", new PingMonitorChecker(heartbeatService, webSocketServer));
        checkers.put("tcp", new TcpMonitorChecker(heartbeatService, webSocketServer));
    }

    public MonitorChecker getChecker(String type) {
        MonitorChecker checker = checkers.get(type.toLowerCase());
        if (checker == null) {
            throw new IllegalArgumentException("不支持的监控类型: " + type);
        }
        return checker;
    }
}