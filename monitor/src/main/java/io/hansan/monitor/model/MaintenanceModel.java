package io.hansan.monitor.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MaintenanceModel {
    private Long id;
    private String title;
    private String description;
    private Long userId;
    private Boolean active = true;
    private String strategy = "single";


    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    private LocalTime startTime;
    private LocalTime endTime;
    private String weekdays = "[]";
    private String daysOfMonth = "[]";
    private Integer intervalDay;

    /**
     * 返回准备好解析为JSON的对象
     * @return Map对象
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("id", id);
        json.put("title", title);
        json.put("description", description);
        json.put("active", active);
        json.put("strategy", strategy);
        json.put("startDate", startDate);
        json.put("endDate", endDate);
        json.put("startTime", startTime);
        json.put("endTime", endTime);
        json.put("weekdays", weekdays);
        json.put("daysOfMonth", daysOfMonth);
        json.put("intervalDay", intervalDay);
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
        json.put("description", description);
        json.put("startDate", startDate);
        json.put("endDate", endDate);
        return json;
    }
}