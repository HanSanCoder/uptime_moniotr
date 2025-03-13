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

}