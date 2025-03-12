package io.hansan.monitor.service;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 11:49
 * @Description：TODO
 */

import io.hansan.monitor.mapper.MaintenanceMapper;
import io.hansan.monitor.model.MaintenanceModel;
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
    public MaintenanceModel findById(Integer id) {
        return maintenanceMapper.findById(id);
    }

    /**
     * 根据用户ID查找维护记录
     */
    public List<MaintenanceModel> findByUserId(Integer userId) {
        return maintenanceMapper.findByUserId(userId);
    }

    /**
     * 插入新的维护记录
     */
    public int insert(MaintenanceModel maintenance) {
        return maintenanceMapper.insert(maintenance);
    }

    /**
     * 更新维护记录
     */
    public int update(MaintenanceModel maintenance) {
        return maintenanceMapper.update(maintenance);
    }

    /**
     * 删除维护记录
     */
    public int delete(Integer id) {
        return maintenanceMapper.delete(id);
    }
}