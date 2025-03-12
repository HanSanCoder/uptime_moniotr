package io.hansan.monitor.dto;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 21:39
 * @Description：TODO
 */
import lombok.Data;

@Data
public class StatusPagePublicDTO {
    private String slug;
    private String title;
    private String description;
    private String icon;
    private String theme;
    private Boolean published;
    private Boolean showTags;
    private String customCss;
    private String footerText;
    private Boolean showPoweredBy;
    private String googleAnalyticsId;
    private Boolean showCertificateExpiry;
}