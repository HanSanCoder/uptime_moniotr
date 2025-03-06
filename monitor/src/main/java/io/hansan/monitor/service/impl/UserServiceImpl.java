package io.hansan.monitor.service.impl;

     import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
     import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
     import io.hansan.monitor.mapper.UserMapper;
     import io.hansan.monitor.model.UserModel;
     import io.hansan.monitor.service.IUserService;
     import org.apache.ibatis.solon.annotation.Db;
     import org.noear.solon.annotation.Component;
     import org.noear.solon.annotation.Inject;

     import java.time.LocalDateTime;
     import java.util.List;

     /**
      * @Author ：何汉叁
      * @Date ：2025/2/28 16:15
      * @Description：用户服务，提供用户增删改查功能
      */
     @Component
     public class UserServiceImpl implements IUserService {

         @Db
         private UserMapper userMapper;

         /**
          * 创建用户
          *
          * @param user 用户信息
          * @return 创建的用户ID
          */
         @Override
         public Long createUser(UserModel user) {
             // 设置创建和更新时间
             java.time.LocalDateTime now = java.time.LocalDateTime.now();
             user.setCreatedAt(now);
             user.setUpdatedAt(now);

             // 保存用户并返回ID
             userMapper.insert(user);
             return user.getId();
         }

         /**
          * 根据ID获取用户
          *
          * @param id 用户ID
          * @return 用户信息
          */
         @Override
         public UserModel getUserById(Long id) {
             return userMapper.selectById(id);
         }

         /**
          * 根据用户名获取用户
          *
          * @param username 用户名
          * @return 用户信息
          */
         @Override
         public UserModel getUserByUsername(String username) {
             LambdaQueryWrapper<UserModel> queryWrapper = new LambdaQueryWrapper<>();
             queryWrapper.eq(UserModel::getUsername, username);
             return userMapper.selectOne(queryWrapper);
         }

         /**
          * 更新用户信息
          *
          * @param user 用户信息
          * @return 是否更新成功
          */
         @Override
         public boolean updateUser(UserModel user) {
             // 设置更新时间
             user.setUpdatedAt(LocalDateTime.now());
             return userMapper.updateById(user) > 0;
         }

         /**
          * 删除用户
          *
          * @param id 用户ID
          * @return 是否删除成功
          */
         @Override
         public boolean deleteUser(Long id) {
             return userMapper.deleteById(id) > 0;
         }

         /**
          * 分页查询用户
          *
          * @param pageNum  页码
          * @param pageSize 每页记录数
          * @return 分页用户列表
          */
         public Page<UserModel> listUsersByPage(int pageNum, int pageSize) {
             Page<UserModel> page = new Page<>(pageNum, pageSize);
             return userMapper.selectPage(page, null);
         }

         /**
          * 根据条件查询用户列表
          *
          * @param email 邮箱（可选）
          * @return 用户列表
          */
         @Override
         public List<UserModel> listUsers(String email) {
             LambdaQueryWrapper<UserModel> queryWrapper = new LambdaQueryWrapper<>();
             if (email != null && !email.isEmpty()) {
                 queryWrapper.like(UserModel::getEmail, email);
             }
             return userMapper.selectList(queryWrapper);
         }
     }