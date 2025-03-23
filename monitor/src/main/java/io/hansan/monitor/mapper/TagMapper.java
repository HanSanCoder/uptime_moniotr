package io.hansan.monitor.mapper;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/12 17:46
 * @Description：TODO
 */
import io.hansan.monitor.model.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Tag Mapper接口
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    @Select("SELECT DISTINCT name FROM tag ORDER BY name")
    List<String> findAllTags();
    @Insert("INSERT INTO monitor_tag_relation (tag_id, monitor_id) VALUES(#{tagId}, #{monitorId})")
    boolean addMonitorTag(Integer tagId, Integer monitorId);
    @Delete("DELETE FROM monitor_tag_relation WHERE tag_id = #{tagId} AND monitor_id = #{monitorId}")
    boolean deleteMonitorTag(Integer tagId, Integer monitorId);
    @Delete("DELETE FROM monitor_tag_relation WHERE monitor_id = #{monitorId}")
    boolean deleteMonitorTagByMonitorId(Integer monitorId);
}