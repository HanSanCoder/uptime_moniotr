package io.hansan.monitor.model;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/6 18:02
 * @Description：TODO
 */
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("notification")
public class NotificationModel {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String type;
    private String config;
    private Boolean active;
    private String createdAt;
    private String updatedAt;
}
