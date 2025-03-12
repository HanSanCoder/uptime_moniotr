package io.hansan.monitor.mapper;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 11:43
 * @Description：TODO
 */

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.MaintenanceModel;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MaintenanceMapper extends BaseMapper<MaintenanceModel> {

    @Select("SELECT * FROM maintenance WHERE id = #{id}")
    MaintenanceModel findById(Integer id);

    @Select("SELECT * FROM maintenance WHERE user_id = #{userId}")
    List<MaintenanceModel> findByUserId(Integer userId);

    @Insert("INSERT INTO maintenance (title, description, strategy, interval_day, active, start_date, end_date, " +
            "start_time, end_time, weekdays, days_of_month, cron, duration, timezone, user_id) " +
            "VALUES (#{title}, #{description}, #{strategy}, #{intervalDay}, #{active}, #{startDate}, #{endDate}, " +
            "#{startTime}, #{endTime}, #{weekdays}, #{daysOfMonth}, #{cron}, #{duration}, #{timezone}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MaintenanceModel maintenance);

    @Update("UPDATE maintenance SET title = #{title}, description = #{description}, strategy = #{strategy}, " +
            "interval_day = #{intervalDay}, active = #{active}, start_date = #{startDate}, end_date = #{endDate}, " +
            "start_time = #{startTime}, end_time = #{endTime}, weekdays = #{weekdays}, days_of_month = #{daysOfMonth}, " +
            "cron = #{cron}, duration = #{duration}, timezone = #{timezone} WHERE id = #{id}")
    int update(MaintenanceModel maintenance);

    @Delete("DELETE FROM maintenance WHERE id = #{id}")
    int delete(Integer id);
}