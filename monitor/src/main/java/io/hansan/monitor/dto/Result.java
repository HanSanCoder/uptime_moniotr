package io.hansan.monitor.dto;

import io.hansan.monitor.model.HeartbeatModel;
import io.hansan.monitor.model.Maintenance;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.model.Tag;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/13 21:32
 * @Description：TODO
 */
@Data
public class Result {
    private Integer monitorID;
    private String msg;
    private String message;
    private boolean ok;
    private Long tagId;
    private Tag tag;
    private Map<Integer, MonitorModel> monitors;
    private String data;
    private MonitorModel monitor;
    private double avgPing;
    private List<Maintenance> maintenances;
    private Maintenance maintenance;
    private List<HeartbeatModel> beats;
    private Long maintenanceID;
    private String username;
    private Integer expiryDay;
}
