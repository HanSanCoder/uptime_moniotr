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

    @Select("SELECT * FROM monitor WHERE active = true ORDER BY id")
    List<MonitorModel> getAllActive();

    @Select("SELECT * FROM monitor WHERE type = #{type} ORDER BY id")
    List<MonitorModel> getMonitorsByType(String type);

    @Select("SELECT * FROM monitor WHERE hostname = #{hostname} AND port = #{port} AND type = 'tcp'")
    List<MonitorModel> findTcpMonitorsByHostAndPort(@Param("hostname") String hostname, @Param("port") Integer port);

    @Select("SELECT t.name, t.id as `tag_id`, t.color FROM tag t " +
            "INNER JOIN monitor_tag_relation mtr ON t.id = mtr.tag_id " +
            "WHERE mtr.monitor_id = #{monitorId}")
    @Results({
            @Result(column = "name", property = "name"),
            @Result(column = "tag_id", property = "tag_id"),
            @Result(column = "color", property = "color")
    })
    List<TagDTO> findTagsByMonitorId(@Param("monitorId") Integer monitorId);

    @Select("SELECT *, monitor.name AS pathName FROM monitor")
    List<MonitorModel> findAll();
}