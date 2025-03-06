package io.hansan.monitor.service;

/**
 * @Author ：何汉叁
 * @Date ：2025/2/28 17:20
 * @Description：TODO
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.hansan.monitor.model.UserModel;
import java.util.List;

public interface IUserService {
    /**
     * 创建用户
     */
    Long createUser(UserModel user);

    /**
     * 根据ID获取用户
     */
    UserModel getUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    UserModel getUserByUsername(String username);

    /**
     * 更新用户信息
     */
    boolean updateUser(UserModel user);

    /**
     * 删除用户
     */
    boolean deleteUser(Long id);
    /**
     * 分页查询用户列表
     */
    Page<UserModel> listUsersByPage(int page, int pageSize);

    /**
     * 根据邮箱查询用户列表
     */
    List<UserModel> listUsers(String email);
}