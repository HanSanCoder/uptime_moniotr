package io.hansan.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.dto.UserContext;
import io.hansan.monitor.mapper.UserMapper;
import io.hansan.monitor.model.User;
import org.noear.solon.annotation.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author ：何汉叁
 * @Date ：2025/2/28 16:15
 * @Description：用户服务，提供用户增删改查功能
 */
@Component
public class UserService extends ServiceImpl<UserMapper, User> {

    /**
     * 获取所有用户
     */
    public List<User> listAll() {
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
     * 创建用户
     */
    public Result createUser(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        wrapper.eq(User::getPassword, password);
        User user = this.getOne(wrapper);
        Result result = new Result();
        if (user != null) {
            UserContext.setCurrentUserId(Math.toIntExact(user.getId()));
            result.setUsername(user.getUsername());
            result.setOk(true);
            result.setMsg("登录成功");
        } else {
            user.setUsername(username);
            user.setPassword(password);
            user.setActive(true);
            user.setCreatedAt(LocalDateTime.now());
            boolean save = this.save(user);
            result.setOk(save);
            if (save) {
                result.setOk(true);
                result.setMsg("创建用户成功");
            } else {
                result.setOk(false);
                result.setMsg("创建用户失败");
            }
        }
        return result;
    }

    public Result login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        wrapper.eq(User::getPassword, password);
        User user = this.getOne(wrapper);
        Result result = new Result();
        if (user != null) {
            UserContext.setCurrentUserId(Math.toIntExact(user.getId()));
            result.setUsername(user.getUsername());
            result.setOk(true);
            result.setMsg("登录成功");
        } else {
            result.setOk(false);
            result.setMsg("用户名或密码错误");
        }
        return result;
    }

    public Result logout() {
//        UserContext.setCurrentUserId(1001);
        Result result = new Result();
        result.setOk(true);
        result.setMsg("退出登录成功");
        return result;
    }

    public Result updatePassword(String currentPassword, String newPassword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, UserContext.getCurrentUserId());
        wrapper.eq(User::getPassword, currentPassword);
        User user = this.getOne(wrapper);
        Result result = new Result();
        if (user != null) {
            user.setPassword(newPassword);
            boolean update = this.updateById(user);
            if (update) {
                result.setOk(true);
                result.setMsg("修改密码成功");
            } else {
                result.setOk(false);
                result.setMsg("修改密码失败");
            }
        } else {
            result.setOk(false);
            result.setMsg("当前密码错误");
        }
        return result;
    }
}