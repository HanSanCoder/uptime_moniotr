package io.hansan.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.MonitorModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
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

    }