package io.hansan.monitor.model;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/11 18:06
 * @Description：TODO
 */
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class StatusPage {
    private Long id;
    private String slug;
    private String title;
    private String description;
    private String icon;
    private String theme;
    private Boolean published;
    private Boolean showTags;
    private String customCSS;
    private String footerText;
    private Boolean showPoweredBy;
    private String googleAnalyticsTagId;
    private Boolean showCertificateExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 获取图标路径
     * @return 图标路径
     */
    public String getIcon() {
        if (icon == null || icon.isEmpty()) {
            return "/icon.svg";
        }
        return icon;
    }

    /**
     * 返回准备好解析为JSON的对象
     * @return Map对象
     */
    public Map<String, Object> toJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("id", id);
        json.put("slug", slug);
        json.put("title", title);
        json.put("description", description);
        json.put("icon", getIcon());
        json.put("theme", theme);
        json.put("published", published);
        json.put("showTags", showTags);
        json.put("customCSS", customCSS);
        json.put("footerText", footerText);
        json.put("showPoweredBy", showPoweredBy);
        json.put("googleAnalyticsTagId", googleAnalyticsTagId);
        json.put("showCertificateExpiry", showCertificateExpiry);
        return json;
    }

    /**
     * 返回准备好解析为JSON的对象（公开版本）
     * @return Map对象
     */
    public Map<String, Object> toPublicJSON() {
        Map<String, Object> json = new HashMap<>();
        json.put("slug", slug);
        json.put("title", title);
        json.put("description", description);
        json.put("icon", getIcon());
        json.put("theme", theme);
        json.put("published", published);
        json.put("showTags", showTags);
        json.put("customCSS", customCSS);
        json.put("footerText", footerText);
        json.put("showPoweredBy", showPoweredBy);
        json.put("googleAnalyticsTagId", googleAnalyticsTagId);
        json.put("showCertificateExpiry", showCertificateExpiry);
        return json;
    }
}