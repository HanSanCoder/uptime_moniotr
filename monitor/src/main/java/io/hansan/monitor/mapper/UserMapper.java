package io.hansan.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.hansan.monitor.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.noear.solon.annotation.Component;

/**
 * @author ：何汉叁
 * @date ：2025/2/28 16:14
 * @description：TODO
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
