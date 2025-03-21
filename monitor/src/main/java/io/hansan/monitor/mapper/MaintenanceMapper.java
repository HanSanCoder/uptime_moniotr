package io.hansan.monitor.mapper;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 11:43
 * @Description：TODO
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.Maintenance;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MaintenanceMapper extends BaseMapper<Maintenance> {

    @Select("SELECT * FROM maintenance WHERE id = #{id}")
    Maintenance findById(Integer id);

    @Select("SELECT * FROM maintenance WHERE user_id = #{userId}")
    List<Maintenance> findByUserId(Integer userId);


    @Update("UPDATE maintenance SET title = #{title}, description = #{description}, strategy = #{strategy}, " +
            "interval_day = #{intervalDay}, active = #{active}, start_date = #{startDate}, end_date = #{endDate}, " +
            "start_time = #{startTime}, end_time = #{endTime}, weekdays = #{weekdays}, days_of_month = #{daysOfMonth}, " +
            "cron = #{cron}, duration = #{duration}, timezone = #{timezone} WHERE id = #{id}")
    int update(Maintenance maintenance);

    @Delete("DELETE FROM maintenance WHERE id = #{id}")
    int delete(Integer id);

    @Select("SELECT monitor_id FROM monitor_maintenance WHERE maintenance_id = #{maintenanceId}")
    List<Integer> getMonitorMaintenance(Integer maintenanceId);

    @Insert("INSERT INTO monitor_maintenance (maintenance_id, monitor_id) VALUES (#{maintenanceId}, #{monitorId})")
    void addMonitorMaintenance(Integer maintenanceId, Integer monitorId);

    @Select("SELECT monitor_id FROM monitor_maintenance WHERE maintenance_id = #{maintenanceId}")
    List<Integer> findMonitorIdByMaintenanceId(Integer maintenanceId);

    @Delete("DELETE FROM monitor_maintenance WHERE maintenance_id = #{maintenanceId}")
    void deleteMonitorMaintenance(Long maintenanceId);
}