package io.hansan.monitor.model;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 18:09
 * @Description：TODO
 */

import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class Incident {
    private Long id;
    private Long statusPageId;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private Boolean pin = false;
    private Boolean active = true;

    /**
     * 返回准备好解析为JSON的对象
     * @return Map对象
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("id", id);
        json.put("statusPageId", statusPageId);
        json.put("title", title);
        json.put("content", content);
        json.put("createdDate", createdDate);
        json.put("pin", pin);
        json.put("active", active);
        return json;
    }

    /**
     * 返回准备好解析为JSON的公开对象
     * @return Map对象
     */
    public Map<String, Object> toPublicJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("id", id);
        json.put("title", title);
        json.put("content", content);
        json.put("createdDate", createdDate);
        return json;
    }
}