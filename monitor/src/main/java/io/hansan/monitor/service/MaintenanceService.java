package io.hansan.monitor.service;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 11:49
 * @Description：TODO
 */

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.mapper.MaintenanceMapper;
import io.hansan.monitor.model.Maintenance;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

@Component
public class MaintenanceService extends ServiceImpl<MaintenanceMapper, Maintenance> {

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
        boolean save = this.save(maintenance);
        Result result = new Result();
        if (save) {
            result.setMaintenanceID(maintenance.getId());
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

    public Result pauseMaintenance(Integer maintenanceId) {
        Maintenance maintenance = this.getById(maintenanceId);
        maintenance.setActive(false);
        this.updateById(maintenance);
        Result result = new Result();
        result.setOk(true);
        result.setMsg("暂停维护记录成功");
        return result;
    }

    public Result resumeMaintenance(Integer maintenanceId) {
        Maintenance maintenance = this.getById(maintenanceId);
        maintenance.setActive(true);
        this.updateById(maintenance);
        Result result = new Result();
        result.setOk(true);
        result.setMsg("恢复维护记录成功");
        return result;
    }

    public Result deleteMaintenance(Integer maintenanceId) {
        boolean remove = this.removeById(maintenanceId);
        Result result = new Result();
        if (remove) {
            result.setOk(true);
            result.setMsg("删除维护记录成功");
        } else {
            result.setOk(false);
            result.setMsg("删除维护记录失败");
        }
        return result;
    }

    public List<Integer> findMonitorIdByMaintenanceId(Integer maintenanceId) {
        return maintenanceMapper.findMonitorIdByMaintenanceId(maintenanceId);
    }

    public Result editMaintenance(Maintenance data) {
        boolean update = this.updateById(data);
        maintenanceMapper.deleteMonitorMaintenance(data.getId());
        Result result = new Result();
        if (update) {
            result.setOk(true);
            result.setMaintenanceID(data.getId());
            result.setMsg("编辑维护记录成功");
        } else {
            result.setOk(false);
            result.setMsg("编辑维护记录失败");
        }
        return result;
    }
}