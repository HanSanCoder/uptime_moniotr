package io.hansan.monitor.controller;

import io.hansan.monitor.common.Result;
import io.hansan.monitor.model.UserModel;
import io.hansan.monitor.service.impl.UserServiceImpl;
import org.noear.solon.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ：何汉叁
 * @date ：2025/2/28 16:06
 * @description：用户管理接口
 */
@Controller
@Mapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    @Inject
    private UserServiceImpl userServiceImpl;

    /**
     * 创建用户
     */
    @Post
    @Mapping("/register")
    public Result<Long> createUser(@Body UserModel user) {
        try {
            // 基本参数校验
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                return Result.fail("用户名不能为空");
            }
            if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
                return Result.fail("密码不能为空");
            }

            // 检查用户名是否已存在
            UserModel existingUser = userServiceImpl.getUserByUsername(user.getUsername());
            if (existingUser != null) {
                return Result.fail("用户名已存在");
            }

            Long userId = userServiceImpl.createUser(user);
            return Result.ok("用户创建成功", userId);
        } catch (Exception e) {
            log.error("用户创建失败", e);
            return Result.fail("用户创建失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @Get
    @Mapping("/{id}")
    public Result<UserModel> getUserById(@Path Long id) {
        if (id == null || id <= 0) {
            return Result.fail("无效的用户ID");
        }

        UserModel user = userServiceImpl.getUserById(id);
        if (user != null) {
            // 不返回密码哈希
            user.setPasswordHash(null);
            return Result.ok(user);
        } else {
            return Result.fail("用户不存在");
        }
    }

    /**
     * 更新用户信息
     */
    @Put
    @Mapping("/{id}")
    public Result<Boolean> updateUser(@Path Long id, @Body UserModel user) {
        if (id == null || id <= 0) {
            return Result.fail("无效的用户ID");
        }

        // 验证用户是否存在
        UserModel existingUser = userServiceImpl.getUserById(id);
        if (existingUser == null) {
            return Result.fail("用户不存在");
        }

        user.setId(id);
        boolean success = userServiceImpl.updateUser(user);
        return success ? Result.ok("用户更新成功", true) : Result.fail("用户更新失败");
    }

    /**
     * 删除用户
     */
    @Delete
    @Mapping("/{id}")
    public Result<Boolean> deleteUser(@Path Long id) {
        if (id == null || id <= 0) {
            return Result.fail("无效的用户ID");
        }

        boolean success = userServiceImpl.deleteUser(id);
        return success ? Result.ok("用户删除成功", true) : Result.fail("用户不存在或删除失败");
    }


    /**
     * 根据用户名查询用户
     */
    @Get
    @Mapping("/search")
    public Result<UserModel> getUserByUsername(@Param("username") String username) {
        if (username == null || username.isEmpty()) {
            return Result.fail("用户名不能为空");
        }

        UserModel user = userServiceImpl.getUserByUsername(username);
        if (user != null) {
            // 不返回密码哈希
            user.setPasswordHash(null);
            return Result.ok(user);
        } else {
            return Result.fail("用户不存在");
        }
    }

    /**
     * 按邮箱查询用户
     */
    @Get
    @Mapping("/byEmail")
    public Result<?> listUsersByEmail(@Param(required = false) String email) {
        return Result.ok(userServiceImpl.listUsers(email));
    }
}