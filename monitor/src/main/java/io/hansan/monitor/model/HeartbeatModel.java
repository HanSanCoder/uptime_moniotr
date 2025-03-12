package io.hansan.monitor.model;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 11:15
 * @Description：TODO
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    private Boolean important;
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
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    /**
     * 返回准备好解析为JSON的对象（公开版本）
     * 仅显示对公众必要的数据
     * @return Map对象
     */
    public Map<String, Object> toPublicJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("status", status);
        json.put("time", time);
        json.put("msg", "");        // 对公众隐藏
        json.put("ping", ping);
        return json;
    }

    /**
     * 返回准备好解析为JSON的对象
     * @return Map对象
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("monitorID", monitorId);
        json.put("status", status);
        json.put("time", time);
        json.put("msg", msg);
        json.put("ping", ping);
        json.put("important", important);
        json.put("duration", duration);
        return json;
    }
}
