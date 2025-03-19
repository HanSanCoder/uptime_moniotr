package io.hansan.monitor.service;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 19:17
 * @Description：TODO
 */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.dto.MonitorDTO;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.dto.TagDTO;
import io.hansan.monitor.mapper.MaintenanceMapper;
import io.hansan.monitor.mapper.MonitorMapper;
import io.hansan.monitor.model.MonitorModel;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MonitorService extends ServiceImpl<MonitorMapper, MonitorModel> {

    @Inject
    private MonitorMapper monitorMapper;
    @Inject
    private MaintenanceMapper maintenanceMapper;
    /**
     * 获取所有监控项
     */
    public List<MonitorModel> listAll() {
        // 获取所有监控项
        List<MonitorModel> monitors = monitorMapper.findAll();

        // 查询每个监控项的标签
        for (MonitorModel monitor : monitors) {
            List<TagDTO> tags = monitorMapper.findTagsByMonitorId(monitor.getId());
            monitor.setTags(tags);
        }

        return monitors;
    }

    /**
     * 获取所有监控器ID
     */
    public List<Integer> listAllIds() {
        LambdaQueryWrapper<MonitorModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(MonitorModel::getId);
        return this.list(wrapper)
                .stream()
                .map(MonitorModel::getId)
                .collect(Collectors.toList());
    }

    /**
     * 添加新的监控项
     */
    public Result addMonitor(MonitorDTO monitor) {
        MonitorModel monitorModel = monitor.convertDtoToModel();
        List<Integer> notificationIDList = monitor.getNotificationIDList();
        Result result = new Result();
        boolean saved = this.save(monitorModel);
        if (saved) {
            Integer monitorId = monitorModel.getId();
            if (notificationIDList != null && !notificationIDList.isEmpty()) {
                monitorMapper.batchInsertMonitorNotifications(monitorId, notificationIDList);
            }
            result.setMonitorID(monitorId);
            result.setMessage("添加监控成功");
            result.setOk(true);
            return result;
        }
        result.setOk(false);
        result.setMessage("添加监控失败");
        return result;
    }

    /**
     * 删除监控项
     */
    public Result deleteMonitor(Integer monitorId) {
        Result result = new Result();
        boolean removed = this.removeById(monitorId);
        if (removed) {
            result.setOk(true);
            result.setMsg("删除监控成功");
            return result;
        }
        result.setOk(false);
        return result;
    }

    /**
     * 更新监控项
     */
    public Result updateMonitor(MonitorModel monitor) {
        Result result = new Result();
        boolean updated = this.updateById(monitor);
        if (updated) {
            result.setOk(true);
            result.setMsg("更新监控成功");
            return result;
        }
        result.setOk(false);
        result.setMsg("更新监控失败");
        return result;
    }

    // 获取用户的所有监控项
    public List<MonitorModel> getMonitorsByUserId(Integer userId) {
        List<MonitorModel> monitors = monitorMapper.getMonitorsByUserId(userId);
        // 查询每个监控项的标签
        for (MonitorModel monitor : monitors) {
            List<TagDTO> tags = monitorMapper.findTagsByMonitorId(monitor.getId());
            monitor.setTags(tags);
            monitor.setPathName(monitor.getName());
        }
        return monitors;
    }

    public Result getMonitorMaintenance(Integer maintenanceId) {
        List<Integer> monitorIds = maintenanceMapper.getMonitorMaintenance(maintenanceId);
        LambdaQueryWrapper<MonitorModel> wrapper = Wrappers.<MonitorModel>lambdaQuery()
                .in(MonitorModel::getId, monitorIds);

        List<MonitorModel> monitorList = monitorMapper.selectList(wrapper);
        Map<Integer, MonitorModel> monitors = monitorList.stream()
                .collect(Collectors.toMap(
                        MonitorModel::getId,  // Key mapper (monitor ID)
                        monitor -> monitor    // Value mapper (monitor object itself)
                ));
        Result result = new Result();
        result.setOk(true);
        result.setMonitors(monitors);
        return result;
    }

    public Result getByMonitorId(Integer monitorId) {
        MonitorModel monitor = monitorMapper.selectById(monitorId);
        Result result = new Result();
        result.setOk(true);
        result.setMonitor(monitor);
        return result;
    }

    public Result pauseMonitor(Integer monitorId) {
        MonitorModel monitor = monitorMapper.selectById(monitorId);
        monitor.setActive(false);
        boolean updated = this.updateById(monitor);
        Result result = new Result();
        if (updated) {
            result.setOk(true);
            result.setMsg("暂停监控成功");
            result.setMonitor(monitor);
            return result;
        }
        result.setOk(false);
        result.setMsg("暂停监控失败");
        return result;
    }

    public Result resumeMonitor(Integer monitorId) {
        MonitorModel monitor = monitorMapper.selectById(monitorId);
        monitor.setActive(true);
        boolean updated = this.updateById(monitor);
        Result result = new Result();
        if (updated) {
            result.setOk(true);
            result.setMsg("恢复监控成功");
            result.setMonitor(monitor);
            return result;
        }
        result.setOk(false);
        result.setMsg("恢复监控失败");
        return result;
    }
}