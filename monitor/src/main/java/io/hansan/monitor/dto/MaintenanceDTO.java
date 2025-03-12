package io.hansan.monitor.dto;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 11:05
 * @Description：TODO
 */
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class MaintenanceDTO {
    private Integer id;
    private String title;
    private String description;
    private String strategy;
    private Integer intervalDay;
    private Boolean active;
    private LocalDateTime[] dateRange = new LocalDateTime[2];
    private String[] timeRange = new String[2];
    private List<Integer> weekdays = new ArrayList<>();
    private List<String> daysOfMonth = new ArrayList<>();
    private List<TimeslotDTO> timeslotList = new ArrayList<>();
    private String cron;
    private Integer duration;
    private Integer durationMinutes;
    private String timezone;
    private String timezoneOption;
    private String timezoneOffset;
    private String status;

    @Data
    public static class TimeslotDTO {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}