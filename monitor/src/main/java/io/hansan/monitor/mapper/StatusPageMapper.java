package io.hansan.monitor.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.StatusPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StatusPageMapper extends BaseMapper<StatusPage> {

    @Select("SELECT id FROM status_page WHERE slug = #{slug}")
    Long getIdBySlug(@Param("slug") String slug);

    @Select("SELECT * FROM status_page WHERE slug = #{slug}")
    StatusPage findBySlug(@Param("slug") String slug);
}
