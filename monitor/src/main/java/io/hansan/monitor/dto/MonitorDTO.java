package io.hansan.monitor.dto;

/**
 * @Author ：何汉叁
 * @Date ：2025/3/13 20:31
 * @Description：TODO
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.model.NotificationModel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 监控项数据传输对象 - 用于接收前端添加/编辑监控项的请求数据
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonitorDTO {

    // 基本信息
    private Integer id;                    // 监控项ID（仅编辑时使用）
    private String type;                   // 监控项类型: http, ping, port, push, keyword等
    private String name;                   // 监控项名称
    private String description;            // 描述
    private Boolean active;                // 是否激活
    private Integer weight;                // 排序权重

    // 监控间隔和重试策略
    private Integer checkInterval;              // 检查间隔（秒）
    private Integer retryInterval;         // 重试间隔（秒）
    private Integer resendInterval;        // 重新发送通知间隔（次数）
    private Integer maxretries;            // 最大重试次数
    private Double timeout;                // 请求超时时间（秒）

    // 监控行为配置
    private Boolean ignoreTls;             // 是否忽略TLS错误
    private Boolean upsideDown;            // 倒置模式（成功视为失败，失败视为成功）
    private Boolean expiryNotification;    // 证书到期通知

    // HTTP/HTTPS 特定字段
    private String url;                    // URL
    private String method;                 // HTTP方法（GET, POST等）
    private String body;                   // HTTP请求体
    private String headers;                // HTTP请求头
    private Integer maxredirects;          // 最大重定向数
    @JsonDeserialize(using = AcceptedStatuscodesDeserializer.class)
    private List<String> acceptedStatuscodes; // 接受的HTTP状态码
    private String httpBodyEncoding;       // HTTP请求体编码（json/xml）

    // 身份验证
    private String authMethod;             // 认证方法（basic, oauth2-cc, ntlm, mtls等）
    private String basic_auth_user;        // 基本认证用户名
    private String basic_auth_pass;        // 基本认证密码
    private String authDomain;             // NTLM域
    private String authWorkstation;        // NTLM工作站

    // OAuth2
    private String oauth_auth_method;      // OAuth2认证方法
    private String oauth_token_url;        // OAuth2令牌URL
    private String oauth_client_id;        // OAuth2客户端ID
    private String oauth_client_secret;    // OAuth2客户端密钥
    private String oauth_scopes;           // OAuth2范围

    // TLS/mTLS
    private String tlsCert;                // TLS证书
    private String tlsKey;                 // TLS密钥
    private String tlsCa;                  // CA证书

    // PING/TCP/DNS等通用字段
    private String hostname;               // 主机名
    private Integer port;                  // 端口号
    private Integer packetSize;            // PING包大小

    // DNS特定字段
    private String dns_resolve_server;     // DNS解析服务器
    private String dns_resolve_type;       // DNS解析类型（A, AAAA, MX等）
    private String dns_last_result;        // 上次DNS解析结果

    // Docker特定字段
    private String docker_container;       // Docker容器名称/ID
    private Integer docker_host;           // Docker主机ID

    // 关键字/JSON查询特定字段
    private String keyword;                // 关键字
    private Boolean invertKeyword;         // 反转关键字匹配
    private String jsonPath;               // JSON路径
    private String expectedValue;          // 期望值

    // MQTT特定字段
    private String mqttUsername;           // MQTT用户名
    private String mqttPassword;           // MQTT密码
    private String mqttTopic;              // MQTT主题
    private String mqttSuccessMessage;     // MQTT成功消息

    // Push特定字段
    private String pushToken;              // 推送令牌

    // RADIUS特定字段
    private String radiusUsername;         // RADIUS用户名
    private String radiusPassword;         // RADIUS密码
    private String radiusSecret;           // RADIUS密钥
    private String radiusCalledStationId;  // RADIUS被叫站点ID
    private String radiusCallingStationId; // RADIUS主叫站点ID

    // 数据库特定字段
    private String databaseConnectionString; // 数据库连接字符串
    private String databaseQuery;          // 数据库查询

    // gRPC特定字段
    private String grpcUrl;                // gRPC URL
    private String grpcServiceName;        // gRPC服务名称
    private String grpcMethod;             // gRPC方法
    private String grpcProtobuf;           // gRPC Protobuf定义
    private String grpcBody;               // gRPC请求体
    private String grpcMetadata;           // gRPC元数据
    private Boolean grpcEnableTls;         // 是否启用TLS


    // 代理设置
    private Integer proxyId;               // 代理ID

    // 关联数据
    private List<Integer> notificationIDList;  // 通知ID列表
    @JsonProperty("notificationIDList")
    private void setNotificationIDList(Map<String, Boolean> map) {
        notificationIDList = map.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(entry -> Integer.parseInt(entry.getKey()))
                .collect(Collectors.toList());
    }

    private List<TagDTO> tags;             // 标签列表

    // pathName属性 - 用于表示监控项的完整路径
    private String pathName;               // 完整路径名称（包含父级路径）
    // After
    public MonitorModel convertDtoToModel() {
        MonitorModel model = new MonitorModel();

        // 设置基本属性
        model.setId(this.getId());
        model.setName(this.getName());
        model.setPathName(this.getPathName());
        model.setType(this.getType());
        model.setWeight(this.getWeight());

        // 设置URL和网络相关属性
        model.setUrl(this.getUrl());
        model.setHostname(this.getHostname());
        model.setPort(this.getPort());
        model.setActive(true);
        // 设置检查间隔
        model.setRetryInterval(this.retryInterval);
        model.setCheck_interval(this.checkInterval);
        model.setDescription(this.description);
        // 设置重试策略
        model.setMaxretries(this.getMaxretries());
        model.setMaxredirects(this.maxredirects);
        model.setPacketSize(this.packetSize);
        model.setMethod(this.method);
        model.setAcceptedStatuscodes(acceptedStatuscodes != null && !acceptedStatuscodes.isEmpty()
            ? String.join(",", acceptedStatuscodes)
            : null);
        model.setTimeout(this.timeout);
        // 设置关键词
        model.setKeyword(this.getKeyword());

        // 设置当前时间为创建时间
        model.setCreatedDate(LocalDateTime.now());

        // 转换标签列表
        if (this.getTags() != null) {
            model.setTags(this.getTags());
        }

        return model;
    }
}
