package io.hansan.monitor.service;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 11:49
 * @Description：TODO
 */

import io.hansan.monitor.dto.Result;
import io.hansan.monitor.mapper.MaintenanceMapper;
import io.hansan.monitor.model.Maintenance;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

@Component
public class MaintenanceService {

    @Inject
    private MaintenanceMapper maintenanceMapper;

    /**
     * 根据ID查找维护记录
     */
    public Maintenance findById(Integer id) {
        return maintenanceMapper.findById(id);
    }

    /**
     * 根据用户ID查找维护记录
     */
    public List<Maintenance> findByUserId(Integer userId) {
        return maintenanceMapper.findByUserId(userId);
    }

    /**
     * 插入新的维护记录
     */
    public Result add(Maintenance maintenance) {
        int insert = maintenanceMapper.insert(maintenance);
        Result result = new Result();
        if (insert > 0) {
            result.setOk(true);
            result.setMsg("添加维护记录成功");
        } else {
            result.setOk(false);
            result.setMsg("添加维护记录失败");
        }
        return result;
    }

    /**
     * 更新维护记录
     */
    public int update(Maintenance maintenance) {
        return maintenanceMapper.update(maintenance);
    }

    /**
     * 删除维护记录
     */
    public int delete(Integer id) {
        return maintenanceMapper.delete(id);
    }

    public void addMonitorMaintenance(Integer maintenanceId, Integer monitorId) {
        maintenanceMapper.addMonitorMaintenance(maintenanceId, monitorId);
    }
}