package io.hansan.monitor.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.HeartbeatModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Mapper
public interface HeartbeatMapper extends BaseMapper<HeartbeatModel> {

    /**
     * 获取监控器的最近心跳记录(按时间倒序)
     *
     * @param monitorId 监控器ID
     * @param limit     限制数量
     * @return 心跳记录列表
     */
    @Select("SELECT * FROM heartbeat WHERE monitor_id = #{monitorId} ORDER BY created_at DESC LIMIT #{limit}")
    List<HeartbeatModel> findLatestByMonitorId(@Param("monitorId") Integer monitorId, @Param("limit") Integer limit);

    @Select("SELECT * FROM heartbeat WHERE monitor_id = #{monitorId} ORDER BY created_at DESC LIMIT #{limit}")
    List<HeartbeatModel> findRecentHeartbeats(@Param("monitorId") Integer monitorId, @Param("limit") Integer limit);

    /**
     * 获取重要的心跳记录
     *
     * @param monitorId 监控器ID
     * @param limit     限制数量
     * @return 心跳记录列表
     */
    @Select("SELECT * FROM heartbeat WHERE monitor_id = #{monitorId} AND important = 1 ORDER BY created_at DESC LIMIT #{limit}")
    List<HeartbeatModel> findImportantByMonitorId(@Param("monitorId") Integer monitorId, @Param("limit") Integer limit);

    @Select("SELECT h.*, m.name as name " +
            "FROM heartbeat h " +
            "LEFT JOIN monitor m ON h.monitor_id = m.id " +
            "WHERE h.monitor_id = #{monitorId} " +
            "AND h.important = true " +
            "ORDER BY h.created_at DESC " +
            "LIMIT 50")
    List<HeartbeatModel> selectImportantHeartbeatsWithName(@Param("monitorId") Integer monitorId);

    /**
     * 获取所有不同的监控器ID
     */
    @Select("SELECT DISTINCT monitor_id FROM heartbeat")
    List<Integer> findDistinctMonitorIds();

    /**
     * 获取特定状态的心跳数量
     */
    @Select("SELECT COUNT(*) FROM heartbeat WHERE monitor_id = #{monitorId} AND created_at > #{startTime} AND status = #{status}")
    Integer countByMonitorIdAndStatusAndTimeAfter(
            @Param("monitorId") Integer monitorId,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime
    );

    @Select("SELECT " +
            "CASE " +
            "    WHEN COUNT(*) = 0 THEN 0 " +
            "    ELSE CAST(SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(*) " +
            "END AS uptime " +
            "FROM heartbeat " +
            "WHERE monitor_id = #{monitorId} " +
            "AND created_at BETWEEN #{startTime} AND #{endTime}")
    Double calculateUptime(@Param("monitorId") Integer monitorId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

    @Select("SELECT AVG(ping) " +
            "FROM heartbeat " +
            "WHERE monitor_id = #{monitorId} " +
            "AND status != 0 ")
    Double calculateAveragePing(@Param("monitorId") Integer monitorId);
}
