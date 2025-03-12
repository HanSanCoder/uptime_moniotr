package io.hansan.monitor.model;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 18:10
 * @Description：TODO
 */
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StatusPageCname {
    private Long id;
    private Long statusPageId;
    private String domain;
    private LocalDateTime createdAt;

}