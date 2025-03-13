package io.hansan.monitor.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/12 20:41
 * @Description：TODO
 */
@Data
public class TagDTO {
    private Integer tag_id;
    private String name;
    private String color;
}
