package io.hansan.monitor.model;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 11:15
 * @Description：TODO
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 心跳记录实体类
 * 对应数据库中的 heartbeat 表
 */
@Data
@TableName("heartbeat")
public class HeartbeatModel {
    /**
     * 心跳记录ID，主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关联监控项ID
     */
    private Integer monitorId;

    /**
     * 状态码
     * 0=正常，1-5=错误级别
     */
    private Integer status;

    /**
     * 状态信息
     */
    private String msg;

    /**
     * 响应时间（毫秒）
     */
    private Integer ping;

    /**
     * 持续时间（秒）
     * 默认为0
     */
    private Integer duration;

    /**
     * 创建时间
     * 默认为当前时间戳
     */
    private LocalDateTime createdAt;
}
