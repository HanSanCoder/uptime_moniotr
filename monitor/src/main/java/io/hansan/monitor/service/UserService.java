package io.hansan.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.mapper.UserMapper;
import io.hansan.monitor.model.UserModel;
import org.noear.solon.annotation.Component;
import java.util.List;

/**
 * @Author ：何汉叁
 * @Date ：2025/2/28 16:15
 * @Description：用户服务，提供用户增删改查功能
 */
@Component
public class UserService extends ServiceImpl<UserMapper, UserModel> {

    /**
     * 获取所有用户
     */
    public List<UserModel> listAll() {
        return super.list();
    }

    /**
     * 分页获取用户
     * <p>
     * <p>
     * /**
     * 删除用户
     */
    public boolean deleteUser(Integer id) {
        return this.removeById(id);
    }

    /**
     * 更新用户
     */
    public boolean updateUser(UserModel user) {
        return this.updateById(user);
    }

    /**
     * 根据用户名获取用户
     */
    public UserModel getUserByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<UserModel>().eq(UserModel::getUsername, username));
    }

    /**
     * 创建用户
     */
    public Long createUser(UserModel user) {
        this.save(user);
        return user.getId();
    }

    /**
     * 根据ID获取用户
     */
    public UserModel getUserById(Long id) {
        return this.getById(id);
    }

}