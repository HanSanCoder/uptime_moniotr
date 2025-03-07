package io.hansan.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.HeartbeatModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HeartbeatMapper extends BaseMapper<HeartbeatModel> {
}
