package io.hansan.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.dto.TagDTO;
import io.hansan.monitor.model.MonitorModel;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface MonitorMapper extends BaseMapper<MonitorModel> {

    List<MonitorModel> getMonitorsByUserId(Integer userId);

    @Select("SELECT t.name, t.id as `tag_id`, t.color FROM tag t " +
            "INNER JOIN monitor_tag_relation mtr ON t.id = mtr.tag_id " +
            "WHERE mtr.monitor_id = #{monitorId}")
    @Results({
            @Result(column = "name", property = "name"),
            @Result(column = "tag_id", property = "tag_id"),
            @Result(column = "color", property = "color")
    })
    List<TagDTO> findTagsByMonitorId(@Param("monitorId") Integer monitorId);

    @Select("SELECT *, monitor.name AS pathName FROM monitor WHERE user_id = #{userId}")
    List<MonitorModel> findAll(Integer userId);

    @Select("SELECT *, monitor.name AS pathName FROM monitor WHERE active = 1 AND (created_date IS NULL OR DATE_ADD(created_date, INTERVAL check_interval SECOND) <= NOW())")
    List<MonitorModel> findActiveMonitorsDueForCheck();
    /**
     * 批量插入monitor_notification关联表数据
     * @param monitorId 监控ID
     * @param notificationIds 通知配置ID列表
     * @return 插入的行数
     */
    int batchInsertMonitorNotifications(@Param("monitorId") Integer monitorId,
                                       @Param("notificationIds") List<Integer> notificationIds);

    @Select("SELECT notification_id FROM monitor_notification WHERE monitor_id = #{id}")
    List<Integer> findNotificationIdsByMonitorId(Integer id);
}