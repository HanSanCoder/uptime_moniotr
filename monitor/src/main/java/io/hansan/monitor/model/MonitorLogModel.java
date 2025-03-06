package io.hansan.monitor.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/2 21:31
 * @Description：TODO
 */
@TableName("monitor_logs")
public class MonitorLogModel {
    /**
     * 监控日志的唯一标识符
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 与此日志关联的监控器的标识符
     */
    private Long monitorId;

    /**
     * 记录时监控器的状态
     */
    private String status;

    /**
     * 监控器的响应时间
     */
    private String responseTime;

    /**
     * 日志创建的时间戳
     */
    private LocalDateTime timestamp;
}
