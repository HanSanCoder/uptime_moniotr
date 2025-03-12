package io.hansan.monitor.model;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 18:11
 * @Description：TODO
 */

import lombok.Data;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MonitorGroup {
    private Long id;
    private Long statusPageId;
    private String name;
    private Integer weight = 0;
    private Boolean isPublic = false;
    private List<MonitorModel> monitors;

    /**
     * 返回准备好解析为JSON的对象
     * @return Map对象
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("id", id);
        json.put("statusPageId", statusPageId);
        json.put("name", name);
        json.put("weight", weight);
        json.put("public", isPublic);
        return json;
    }

    /**
     * 返回准备好解析为JSON的公开对象
     * @return Map对象
     */
    public Map<String, Object> toPublicJSON() {
        Map<String, Object> json = toJSON();
        return json;
    }
}