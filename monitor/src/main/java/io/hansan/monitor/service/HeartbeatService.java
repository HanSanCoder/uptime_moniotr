package io.hansan.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.model.HeartbeatModel;
import io.hansan.monitor.mapper.HeartbeatMapper;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HeartbeatService extends ServiceImpl<HeartbeatMapper, HeartbeatModel> {

    @Inject
    private MaintenanceService maintenanceService;

    @Inject
    private HeartbeatMapper heartbeatMapper;

    /**
     * Get heartbeat records for a specific monitor
     */
    public List<HeartbeatModel> getBeats(Integer monitorId) {
        LambdaQueryWrapper<HeartbeatModel> wrapper = new LambdaQueryWrapper<HeartbeatModel>()
                .eq(HeartbeatModel::getMonitorId, monitorId)
                .orderByDesc(HeartbeatModel::getTime)
                .last("LIMIT 100");
        List<HeartbeatModel> beats = this.list(wrapper);

        return beats;
    }

    /**
     * 获取重要心跳记录并包含监控器名称
     */
    public List<HeartbeatModel> getImportantHeartbeats(Integer monitorId) {
        // 获取心跳记录
        List<HeartbeatModel> beats = heartbeatMapper.selectImportantHeartbeatsWithName(monitorId);
        return beats;
    }

    /**
     * Clear all heartbeat records for a specific monitor
     */
    public boolean clearHeartbeats(Integer monitorId) {
        LambdaQueryWrapper<HeartbeatModel> wrapper = new LambdaQueryWrapper<HeartbeatModel>()
                .eq(HeartbeatModel::getMonitorId, monitorId);

        return this.remove(wrapper);
    }


    /**
     * socket.on("uptime", (monitorID, type, data)
     */
    public Map<String, Double> getUptimeData(Integer monitorId) {
        Map<String, Double> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // 计算24小时正常运行时间
        LocalDateTime start24h = now.minusHours(24);
        Double uptime24h = heartbeatMapper.calculateUptime(monitorId, start24h, now);
        result.put("24", uptime24h);

        // 计算7天正常运行时间
        LocalDateTime start7d = now.minusDays(7);
        Double uptime7d = heartbeatMapper.calculateUptime(monitorId, start7d, now);
        result.put("24x7", uptime7d); // 对应前端的一周

        // 计算30天正常运行时间
        LocalDateTime start30d = now.minusDays(30);
        Double uptime30d = heartbeatMapper.calculateUptime(monitorId, start30d, now);
        result.put("720", uptime30d); // 对应前端的30天

        return result;
    }


    private boolean add(Integer monitorId, Integer status, String msg, Double ping, Boolean important) {
        HeartbeatModel heartbeat = new HeartbeatModel();
        heartbeat.setMonitorId(monitorId);
        heartbeat.setStatus(status);
        heartbeat.setMsg(msg);
        heartbeat.setPing(ping);
        heartbeat.setImportant(important);
        heartbeat.setTime(LocalDateTime.now());
        return this.save(heartbeat);
    }

    /**
     * 计算监控的平均ping响应时间
     */
    public Result calculateAveragePing(Integer monitorId) {
        Double avgPing = heartbeatMapper.calculateAveragePing(monitorId);
        avgPing = avgPing != null ? Double.valueOf(String.format("%.1f", avgPing)) : 0.0;
        Result result = new Result();
        result.setAvgPing(avgPing);
        result.setMonitorID(monitorId);
        return result;
    }

    /**
     * 获取指定时间段内的心跳数据
     */
    public Result getCustomPeriodHeartbeats(Integer monitorId, Integer newPeriod) {

        Result result = new Result();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusHours(newPeriod);

        // 获取指定时间段内的心跳记录
        LambdaQueryWrapper<HeartbeatModel> wrapper = new LambdaQueryWrapper<HeartbeatModel>()
                .eq(HeartbeatModel::getMonitorId, monitorId)
                .ge(HeartbeatModel::getTime, startTime)
                .le(HeartbeatModel::getTime, now)
                .orderByAsc(HeartbeatModel::getTime);

        List<HeartbeatModel> heartbeats = this.list(wrapper);

        result.setOk(true);
        result.setBeats(heartbeats);
        return result;
    }
}