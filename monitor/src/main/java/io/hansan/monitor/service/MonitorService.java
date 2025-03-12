package io.hansan.monitor.service;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 19:17
 * @Description：TODO
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.mapper.MonitorMapper;
import io.hansan.monitor.model.MonitorModel;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import javax.management.monitor.Monitor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MonitorService extends ServiceImpl<MonitorMapper, MonitorModel> {

    @Inject
    private MonitorMapper monitorMapper;

    /**
     * 获取所有监控项
     */
    public List<MonitorModel> listAll() {
        return this.list();
    }

    /**
     * 获取监控项详情
     */
//    public MonitorModel getDetails(Integer monitorId) {
//        // 获取基本信息
//        MonitorModel monitor = this.getById(monitorId);
//        if (monitor != null) {
//            // 获取最近的心跳记录
//            monitor.setLatestHeartbeat(monitorMapper.getLatestHeartbeat(monitorId));
//            // 获取最近24小时的状态统计
//            monitor.setStatusStats(monitorMapper.getStatusStats(monitorId));
//        }
//        return monitor;
//    }

    /**
     * 更新监控项状态
     */
//    public void updateStatus(Integer monitorId, Integer status, String message) {
//        monitorMapper.updateStatus(monitorId, status, message);
//    }

    /**
     * 添加新的监控项
     */
    public boolean addMonitor(MonitorModel monitor) {
        return this.save(monitor);
    }

    /**
     * 删除监控项
     */
    public boolean deleteMonitor(Integer monitorId) {
        return this.removeById(monitorId);
    }

    /**
     * 更新监控项
     */
    public boolean updateMonitor(MonitorModel monitor) {
        return this.updateById(monitor);
    }

    // 获取用户的所有监控项
    public Map<Integer, MonitorModel> getMonitorsByUserId(Integer userId) {
        Map<Integer, MonitorModel> result = new HashMap<>();
        LambdaQueryWrapper<MonitorModel> queryWrapper = Wrappers.<MonitorModel>lambdaQuery()
                .eq(MonitorModel::getUserId, userId)
                .orderByDesc(MonitorModel::getWeight)
                .orderByAsc(MonitorModel::getName);

        List<MonitorModel> monitorList = monitorMapper.selectList(queryWrapper);

        for (MonitorModel monitor : monitorList) {
            result.put(monitor.getId(), monitor);
        }
        return result;
    }
}