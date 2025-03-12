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
public class MaintenanceTimeslot {
    private Long id;
    private Long maintenanceId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean generatedNext = false;

    /**
     * 返回准备好解析为JSON的对象
     * @return Map对象
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("id", id);
        json.put("maintenanceId", maintenanceId);
        json.put("startDate", startDate);
        json.put("endDate", endDate);
        json.put("generatedNext", generatedNext);
        return json;
    }
}
