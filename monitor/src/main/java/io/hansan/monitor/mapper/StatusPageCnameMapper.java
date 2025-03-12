package io.hansan.monitor.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.StatusPageCname;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatusPageCnameMapper extends BaseMapper<StatusPageCname> {

    @Select("SELECT domain FROM status_page_cname WHERE status_page_id = #{statusPageId}")
    List<String> getDomainList(@Param("statusPageId") Long statusPageId);

    @Select("SELECT domain, slug FROM status_page, status_page_cname WHERE status_page.id = status_page_cname.status_page_id")
    List<Map<String, String>> getAllDomainMappings();
}