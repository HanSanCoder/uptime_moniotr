package io.hansan.monitor.dto;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 21:40
 * @Description：TODO
 */
import lombok.Data;
import java.util.List;

@Data
public class StatusPageDTO {
    private Long id;
    private String slug;
    private String title;
    private String description;
    private String icon;
    private String theme;
    private Boolean published;
    private Boolean showTags;
    private List<String> domainNameList;
    private String customCss;
    private String footerText;
    private Boolean showPoweredBy;
    private String googleAnalyticsId;
    private Boolean showCertificateExpiry;
}