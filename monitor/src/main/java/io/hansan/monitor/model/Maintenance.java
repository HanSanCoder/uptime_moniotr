package io.hansan.monitor.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Maintenance {

    private Long id;
    private String title;
    private String description;
    private Long userId;
    private Boolean active = true;
    private String strategy = "manual";  // 修改默认值为 "manual" 而不是 "single"

    // 现有的日期时间字段
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;

    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private String weekdays = "[]";
    private String daysOfMonth = "[]";
    private Integer intervalDay;

    // 新增字段 - 兼容前端需要
    private String timezone = "UTC";
    private String timezoneOffset = "+00:00";
    private List<TimeSlot> timeslotList = new ArrayList<>();
    private String cron;  // 用于定时维护的CRON表达式

    /**
     * 获取时间槽列表
     * 如果列表为空但有开始和结束日期，则自动创建一个时间槽
     */
    public List<TimeSlot> getTimeslotList() {
        // 已有时间槽，直接返回
        if (timeslotList != null && !timeslotList.isEmpty()) {
            return timeslotList;
        }

        // 没有时间槽但有开始和结束日期，创建一个时间槽
        if (startDate != null && endDate != null) {
            List<TimeSlot> slots = new ArrayList<>();
            TimeSlot slot = new TimeSlot();
            slot.setStartDate(startDate);
            slot.setEndDate(endDate);
            slots.add(slot);
            return slots;
        }

        // 默认返回空列表
        return new ArrayList<>();
    }

    /**
     * 设置时间槽列表
     * 如果列表非空，同时更新开始和结束日期
     */
    public void setTimeslotList(List<TimeSlot> timeslotList) {
        this.timeslotList = timeslotList;

        // 如果有时间槽，同步更新开始和结束日期
        if (timeslotList != null && !timeslotList.isEmpty()) {
            TimeSlot firstSlot = timeslotList.get(0);
            this.startDate = firstSlot.getStartDate();
            this.endDate = firstSlot.getEndDate();
        }
    }

    /**
     * 维护时间槽内部类
     */
    @Data
    public static class TimeSlot {
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        private LocalDateTime startDate;

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        private LocalDateTime endDate;
    }
}