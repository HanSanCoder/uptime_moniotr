package io.hansan.monitor.controller;

import io.hansan.monitor.common.Result;
import io.hansan.monitor.model.UserModel;
import io.hansan.monitor.service.UserService;
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
    private UserService userService;

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
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return Result.fail("密码不能为空");
            }

            // 检查用户名是否已存在
            UserModel existingUser = userService.getUserByUsername(user.getUsername());
            if (existingUser != null) {
                return Result.fail("用户名已存在");
            }

            Long userId = userService.createUser(user);
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

        UserModel user = userService.getUserById(id);
        if (user != null) {
            // 不返回密码哈希
            user.setPassword(null);
            return Result.ok(user);
        } else {
            return Result.fail("用户不存在");
        }
    }

}