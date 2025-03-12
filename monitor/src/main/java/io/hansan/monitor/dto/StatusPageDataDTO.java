package io.hansan.monitor.dto;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 21:40
 * @Description：TODO
 */
import lombok.Data;
import java.util.List;

@Data
public class StatusPageDataDTO {
    private StatusPagePublicDTO config;
    private Object incident;
    private List<Object> publicGroupList;
    private List<Object> maintenanceList;
}