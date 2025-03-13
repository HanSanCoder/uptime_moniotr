package io.hansan.monitor.service;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 19:17
 * @Description：TODO
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.dto.TagDTO;
import io.hansan.monitor.mapper.MonitorMapper;
import io.hansan.monitor.model.MonitorModel;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import javax.management.monitor.Monitor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Component
public class MonitorService extends ServiceImpl<MonitorMapper, MonitorModel> {

    @Inject
    private MonitorMapper monitorMapper;

    /**
     * 获取所有监控项
     */
    public List<MonitorModel> listAll() {
        // 获取所有监控项
        List<MonitorModel> monitors = monitorMapper.findAll();

        // 查询每个监控项的标签
        for (MonitorModel monitor : monitors) {
            List<TagDTO> tags = monitorMapper.findTagsByMonitorId(monitor.getId());
            log.info("tags: {}", tags);
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