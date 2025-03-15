package io.hansan.monitor.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.hansan.monitor.dto.TagDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/2 21:05
    * @Description：监控配置模型类
    */
/**
 * 监控配置模型类
 * 对应数据库中的 monitor 表，用于存储监控任务的配置信息
 */
@Data
@TableName("monitor")
public class MonitorModel {
    /**
     * 监控任务ID，主键，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 监控任务名称
     */
    private String name;

    private String pathName;
    /**
     * 监控任务是否激活
     * true: 激活状态，会被定时检查
     * false: 未激活，暂停检查
     */
    private boolean active;

    /**
     * 监控任务所属用户ID
     */
    private Integer userId;

    /**
     * 检查间隔时间（秒）
     * 默认为20秒
     */
    @TableField(value = "check_interval")
    private Integer check_interval;

    /**
     * 要监控的URL地址
     * 对于HTTP监控，此处为完整URL
     * 对于TCP/PING监控，格式可能是主机名:端口
     */
    private String url;

    /**
     * 监控类型
     * 可选值: http, tcp, ping
     * 对应数据库中的枚举类型
     */
    private String type; // Enum in DB: 'http','tcp','ping'

    /**
     * 权重值，用于判断监控任务的重要性
     * 默认为2000
     */
    private Integer weight;

    /**
     * 主机名
     * 当URL中没有明确指定或需要单独设置时使用
     */
    private String hostname;

    /**
     * 端口号
     * 当URL中没有明确指定或需要单独设置时使用
     */
    private Integer port;

    /**
     * 创建时间
     * 默认为当前时间戳
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    /**
     * 关键词
     * 用于HTTP监控检查响应内容中是否包含特定关键词
     */
    private String keyword;
    private String description;

    /**
     * 最大重试次数
     * 监控失败时的重试次数，默认为0
     */
    private Integer maxretries;
    @TableField(exist = false)
    private List<TagDTO> tags;
}