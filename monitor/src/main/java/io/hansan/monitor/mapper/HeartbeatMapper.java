package io.hansan.monitor.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.HeartbeatModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HeartbeatMapper extends BaseMapper<HeartbeatModel> {

    /**
     * 获取监控器的最近心跳记录(按时间倒序)
     * @param monitorId 监控器ID
     * @param limit 限制数量
     * @return 心跳记录列表
     */
    @Select("SELECT * FROM heartbeat WHERE monitor_id = #{monitorId} ORDER BY time DESC LIMIT #{limit}")
    List<HeartbeatModel> findLatestByMonitorId(@Param("monitorId") Integer monitorId, @Param("limit") Integer limit);

    /**
     * 获取特定时间段内的监控器心跳记录
     * @param monitorId 监控器ID
     * @param startTime 开始时间
     * @return 心跳记录列表
     */
    @Select("SELECT * FROM heartbeat WHERE monitor_id = #{monitorId} AND time > #{startTime} ORDER BY time ASC")
    List<HeartbeatModel> findByMonitorIdAndTimeAfter(@Param("monitorId") Integer monitorId, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取重要的心跳记录
     * @param monitorId 监控器ID
     * @param limit 限制数量
     * @return 心跳记录列表
     */
    @Select("SELECT * FROM heartbeat WHERE monitor_id = #{monitorId} AND important = 1 ORDER BY time DESC LIMIT #{limit}")
    List<HeartbeatModel> findImportantByMonitorId(@Param("monitorId") Integer monitorId, @Param("limit") Integer limit);

    /**
     * 计算指定时间段内监控器的平均响应时间
     * @param monitorId 监控器ID
     * @param startTime 开始时间
     * @param status 状态 (通常是UP=1)
     * @return 平均响应时间
     */
    @Select("SELECT AVG(ping) FROM heartbeat WHERE monitor_id = #{monitorId} AND status = #{status} AND time > #{startTime}")
    Double calculateAverageResponseTime(@Param("monitorId") Integer monitorId, @Param("status") Integer status, @Param("startTime") LocalDateTime startTime);

    /**
     * 获取所有监控器的最后一次心跳记录
     */
    @Select("SELECT h.* FROM heartbeat h " +
            "INNER JOIN (SELECT monitor_id, MAX(time) AS max_time FROM heartbeat GROUP BY monitor_id) AS latest " +
            "ON h.monitor_id = latest.monitor_id AND h.time = latest.max_time")
    List<HeartbeatModel> findAllLastHeartbeats();

    /**
     * 获取所有不同的监控器ID
     */
    @Select("SELECT DISTINCT monitor_id FROM heartbeat")
    List<Integer> findDistinctMonitorIds();

    /**
     * 获取特定状态的心跳数量
     */
    @Select("SELECT COUNT(*) FROM heartbeat WHERE monitor_id = #{monitorId} AND time > #{startTime} AND status = #{status}")
    Integer countByMonitorIdAndStatusAndTimeAfter(
            @Param("monitorId") Integer monitorId,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime
    );

    /**
     * 获取总心跳数量
     */
    @Select("SELECT COUNT(*) FROM heartbeat WHERE monitor_id = #{monitorId} AND time > #{startTime}")
    Integer countByMonitorIdAndTimeAfter(
            @Param("monitorId") Integer monitorId,
            @Param("startTime") LocalDateTime startTime
    );
}
