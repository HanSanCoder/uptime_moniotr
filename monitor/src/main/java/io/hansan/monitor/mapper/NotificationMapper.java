package io.hansan.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.NotificationModel;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<NotificationModel> {

    @Select("SELECT n.email FROM notification n " +
            "JOIN monitor_notification mn ON n.id = mn.notification_id " +
            "WHERE mn.monitor_id = #{monitorId}")
    List<String> getNotificationByMonitorId(Integer monitorId);

    @Delete("DELETE FROM monitor_notification WHERE notification_id = #{id}")
    void deleteMonitorNotification(Integer id);
}
