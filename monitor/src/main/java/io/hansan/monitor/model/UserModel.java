package io.hansan.monitor.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ：何汉叁
 * @date ：2025/2/28 16:11
 * @description：TODO
 */
@Data
@TableName("users")
public class UserModel {
    /**
     * 用户的唯一标识符
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户的哈希密码
     */
    private String passwordHash;

    /**
     * 用户的电子邮件地址
     */
    private String email;

    /**
     * 用户创建的时间戳
     */
    private LocalDateTime createdAt;

    /**
     * 用户最后更新的时间戳
     */
    private LocalDateTime updatedAt;
}
