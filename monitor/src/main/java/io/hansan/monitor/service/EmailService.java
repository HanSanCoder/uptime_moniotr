package io.hansan.monitor.service;

     import io.hansan.monitor.model.MonitorModel;
     import lombok.extern.slf4j.Slf4j;
     import org.noear.solon.annotation.Component;
     import org.noear.solon.annotation.Inject;

     import javax.mail.*;
     import javax.mail.internet.InternetAddress;
     import javax.mail.internet.MimeMessage;
     import java.io.UnsupportedEncodingException;
     import java.text.SimpleDateFormat;
     import java.util.Calendar;
     import java.util.Date;
     import java.util.List;
     import java.util.Properties;
     import java.util.concurrent.ExecutorService;
     import java.util.concurrent.Executors;

     @Slf4j
     @Component
     public class EmailService {

         @Inject
         private NotificationService notificationService;
         @Inject
            private MonitorService monitorService;

         @Inject("${email.smtp.host}")
         private String smtpHost;

         @Inject("${email.smtp.port}")
         private int smtpPort;

         @Inject("${email.username}")
         private String username;

         @Inject("${email.password}")
         private String password;

         @Inject("${email.smtp.tls:false}")
         private boolean useTLS;

         private final ExecutorService emailExecutor = Executors.newSingleThreadExecutor();

         /**
          * 发送服务下线通知
          */
         public void sendDownNotification(MonitorModel monitor) {
             List<String> emails = notificationService.getNotificationByMonitorId(monitor.getId());
             String subject = generateDownSubject(monitor);
             String content = generateDownContent(monitor);
             for (String email : emails) {
                 sendEmailAsync(email, subject, content);
             }
             monitor.setNotified(true);
             monitorService.updateById(monitor);
         }

         /**
          * 发送服务恢复通知
          */
         public void sendUpNotification(MonitorModel monitor, long downDuration) {
             List<String> emails = notificationService.getNotificationByMonitorId(monitor.getId());
             String subject = generateUpSubject(monitor);
             String content = generateUpContent(monitor, downDuration);
             for (String email : emails) {
                 sendEmailAsync(email, subject, content);
             }
             monitor.setNotified(true);
             monitorService.updateById(monitor);
         }
        /**
         * 发送TLS证书即将过期通知
         *
         * @param monitor 监控模型
         * @param expirationDate 证书到期日期
         * @param daysUntilExpiration 距离到期的天数
         */
        public void sendCertificateExpirationNotification(MonitorModel monitor, String expirationDate, long daysUntilExpiration) {
            List<String> emails = notificationService.getNotificationByMonitorId(monitor.getId());
            String subject = "[证书警告] " + monitor.getName() + " 的TLS证书将在" + daysUntilExpiration + "天后过期";

            // 根据剩余天数确定警告级别的颜色
            String warningColor = "#FFA500"; // 默认橙色
            String bgColor = "#FFF7E6";      // 浅橙色背景
            String borderColor = "#FFE0B2";  // 橙色边框

            if (daysUntilExpiration <= 7) {
                warningColor = "#D32F2F";    // 红色，紧急
                bgColor = "#FFEBEE";         // 浅红色背景
                borderColor = "#FFCDD2";     // 红色边框
            } else if (daysUntilExpiration > 14) {
                warningColor = "#F9A825";    // 黄色，较长时间
                bgColor = "#FFFDE7";         // 浅黄色背景
                borderColor = "#FFF9C4";     // 黄色边框
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(new Date());

            StringBuilder builder = new StringBuilder();
            builder.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>");
            builder.append("<div style='text-align: center; margin-bottom: 20px;'>");
            builder.append(String.format("<h2 style='color: %s; margin: 0;'>⚠️ TLS证书到期警告</h2>", warningColor));
            builder.append("<p style='color: #666; margin: 5px 0 0;'>安全风险提示</p>");
            builder.append("</div>");

            builder.append("<div style='margin: 20px 0;'>");
            builder.append("<p>尊敬的用户，您监控的以下服务TLS证书即将到期:</p>");
            builder.append("</div>");

            // 证书信息卡片
            builder.append(String.format("<div style='background-color: %s; border: 1px solid %s; border-radius: 8px; padding: 15px; margin: 15px 0;'>", bgColor, borderColor));
            builder.append("<p style='margin: 8px 0;'><strong>服务名称:</strong> ").append(monitor.getName()).append("</p>");
            builder.append("<p style='margin: 8px 0;'><strong>监控地址:</strong> ").append(monitor.getUrl()).append("</p>");
            builder.append("<p style='margin: 8px 0;'><strong>当前时间:</strong> ").append(currentTime).append("</p>");
            builder.append(String.format("<p style='margin: 8px 0;'><strong>证书到期日期:</strong> <span style='color: %s; font-weight: bold;'>%s</span></p>", warningColor, expirationDate));
            builder.append(String.format("<p style='margin: 8px 0;'><strong>距离到期时间:</strong> <span style='color: %s; font-weight: bold;'>%d天</span></p>", warningColor, daysUntilExpiration));
            builder.append("</div>");

            // 提醒级别
            builder.append("<div style='margin: 20px 0;'>");
            if (daysUntilExpiration <= 7) {
                builder.append(String.format("<p style='color: %s; font-weight: bold; font-size: 16px;'>⚠️ 紧急情况: 证书即将在一周内过期，请立即更新！</p>", warningColor));
            } else if (daysUntilExpiration <= 14) {
                builder.append(String.format("<p style='color: %s; font-weight: bold; font-size: 16px;'>⚠️ 注意: 证书将在两周内过期，请尽快计划更新。</p>", warningColor));
            } else {
                builder.append(String.format("<p style='color: %s; font-weight: bold; font-size: 16px;'>⚠️ 提醒: 证书将在一个月内过期，请安排证书更新工作。</p>", warningColor));
            }
            builder.append("</div>");

            // 建议操作
            builder.append("<div style='background-color: #E8F5E9; border: 1px solid #C8E6C9; border-radius: 8px; padding: 15px; margin: 15px 0;'>");
            builder.append("<h3 style='color: #2E7D32; margin-top: 0;'>建议操作:</h3>");
            builder.append("<ul style='margin: 10px 0; padding-left: 20px;'>");
            builder.append("<li>联系您的证书提供商申请更新</li>");
            builder.append("<li>准备好新证书的部署计划</li>");
            builder.append("<li>在非高峰时段进行证书替换</li>");
            builder.append("<li>更新后进行全面的功能测试</li>");
            builder.append("</ul>");
            builder.append("</div>");

            // 为什么重要
            builder.append("<div style='margin: 20px 0;'>");
            builder.append("<h3 style='color: #333; margin-bottom: 10px;'>为什么这很重要?</h3>");
            builder.append("<p>过期的TLS证书会导致:</p>");
            builder.append("<ul>");
            builder.append("<li>用户浏览器显示安全警告，影响用户信任</li>");
            builder.append("<li>API调用失败，影响依赖该服务的系统</li>");
            builder.append("<li>敏感数据传输的安全风险</li>");
            builder.append("<li>潜在的服务中断</li>");
            builder.append("</ul>");
            builder.append("</div>");

            // 页脚
            builder.append("<div style='border-top: 1px solid #eee; margin-top: 30px; padding-top: 15px;'>");
            builder.append("<p style='color: #777; font-size: 12px; margin: 5px 0;'>此邮件由监控系统自动发送，请勿直接回复。</p>");
            builder.append("<p style='color: #777; font-size: 12px; margin: 5px 0;'>© " + Calendar.getInstance().get(Calendar.YEAR) + " 监控系统</p>");
            builder.append("</div>");

            builder.append("</div>");

            // 发送邮件给所有关联的通知邮箱
            for (String email : emails) {
                sendEmailAsync(email, subject, builder.toString());
            }

            log.info("已发送证书到期通知: {}，剩余{}天过期", monitor.getName(), daysUntilExpiration);
        }

        /**
         * 发送测试邮件
         *
         * @param email 接收测试邮件的邮箱地址
         * @return 发送结果
         */
        public boolean sendTestEmail(String email,String subject) {
            subject = "[监控系统] 测试邮件" + subject;

            StringBuilder content = new StringBuilder();
            content.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>");
            content.append("<h2 style='color: #5cb85c;'>监控系统测试邮件</h2>");
            content.append("<p>您好，这是一封来自监控系统的测试邮件。</p>");
            content.append("<div style='background-color: #f9f9f9; padding: 15px; border-radius: 4px; margin: 10px 0;'>");
            content.append("<p><strong>发送时间:</strong> ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("</p>");
            content.append("<p><strong>邮件服务器:</strong> ").append(smtpHost).append(":").append(smtpPort).append("</p>");
            content.append("<p><strong>TLS 状态:</strong> ").append(useTLS ? "启用" : "禁用").append("</p>");
            content.append("</div>");
            content.append("<p>如果您收到此邮件，表明您的邮件系统配置正常。</p>");
            content.append("<p style='color: #777; font-size: 12px; margin-top: 30px;'>此邮件由监控系统自动发送，请勿直接回复。</p>");
            content.append("</div>");

            try {
                boolean result = sendEmail(email, subject, content.toString());
                if (result) {
                    log.info("测试邮件发送成功");
                } else {
                    log.warn("测试邮件发送失败");
                }
                return result;
            } catch (Exception e) {
                log.error("测试邮件发送异常", e);
                return false;
            }
        }
         /**
          * 生成服务下线邮件主题
          */
         private String generateDownSubject(MonitorModel monitor) {
             return "[监控警报] " + monitor.getName() + " 服务已下线";
         }

         /**
          * 生成服务恢复邮件主题
          */
         private String generateUpSubject(MonitorModel monitor) {
             return "[监控通知] " + monitor.getName() + " 服务已恢复";
         }

         /**
          * 生成服务下线邮件内容
          */
         private String generateDownContent(MonitorModel monitor) {
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             String currentTime = sdf.format(new Date());

             StringBuilder builder = new StringBuilder();
             builder.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>");
             builder.append("<h2 style='color: #d9534f;'>监控服务告警</h2>");
             builder.append("<p>您监控的服务已检测到异常:</p>");
             builder.append("<div style='background-color: #f9f9f9; padding: 15px; border-radius: 4px; margin: 10px 0;'>");
             builder.append("<p><strong>服务名称:</strong> ").append(monitor.getName()).append("</p>");
             builder.append("<p><strong>监控地址:</strong> ").append(monitor.getUrl()).append("</p>");
             builder.append("<p><strong>告警时间:</strong> ").append(currentTime).append("</p>");
             builder.append("<p><strong>当前状态:</strong> <span style='color: #d9534f;'>已下线</span></p>");
             builder.append("</div>");
             builder.append("<p>请及时检查服务状态，确保服务正常运行。</p>");
             builder.append("<p style='color: #777; font-size: 12px; margin-top: 30px;'>此邮件由监控系统自动发送，请勿直接回复。</p>");
             builder.append("</div>");

             return builder.toString();
         }

         /**
          * 生成服务恢复邮件内容
          */
         private String generateUpContent(MonitorModel monitor, long downDurationSeconds) {
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
             String currentTime = sdf.format(new Date());

             String duration = formatDuration(downDurationSeconds);

             StringBuilder builder = new StringBuilder();
             builder.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;'>");
             builder.append("<h2 style='color: #5cb85c;'>监控服务恢复通知</h2>");
             builder.append("<p>您监控的服务已恢复正常:</p>");
             builder.append("<div style='background-color: #f9f9f9; padding: 15px; border-radius: 4px; margin: 10px 0;'>");
             builder.append("<p><strong>服务名称:</strong> ").append(monitor.getName()).append("</p>");
             builder.append("<p><strong>监控地址:</strong> ").append(monitor.getUrl()).append("</p>");
             builder.append("<p><strong>恢复时间:</strong> ").append(currentTime).append("</p>");
             builder.append("<p><strong>当前状态:</strong> <span style='color: #5cb85c;'>已恢复</span></p>");
             builder.append("<p><strong>停机时长:</strong> ").append(duration).append("</p>");
             builder.append("</div>");
             builder.append("<p>感谢您使用本监控系统。</p>");
             builder.append("<p style='color: #777; font-size: 12px; margin-top: 30px;'>此邮件由监控系统自动发送，请勿直接回复。</p>");
             builder.append("</div>");

             return builder.toString();
         }

         /**
          * 格式化持续时间
          */
         private String formatDuration(long seconds) {
             if (seconds < 60) {
                 return seconds + " 秒";
             } else if (seconds < 3600) {
                 return (seconds / 60) + " 分钟 " + (seconds % 60) + " 秒";
             } else if (seconds < 86400) {
                 return (seconds / 3600) + " 小时 " + ((seconds % 3600) / 60) + " 分钟";
             } else {
                 return (seconds / 86400) + " 天 " + ((seconds % 86400) / 3600) + " 小时";
             }
         }

         /**
          * 异步发送邮件
          */
         public void sendEmailAsync(String to, String subject, String content) {
             emailExecutor.submit(() -> {
                 try {
                     sendEmail(to, subject, content);
                 } catch (Exception e) {
                     log.error("发送邮件失败: {}", e.getMessage(), e);
                 }
             });
         }

         /**
          * 发送邮件
          */
         public boolean sendEmail(String toEmail, String subject, String content) {
             Properties props = new Properties();
             props.put("mail.smtp.auth", "true");
             props.put("mail.smtp.starttls.enable", String.valueOf(useTLS));
             props.put("mail.smtp.host", smtpHost);
             props.put("mail.smtp.port", smtpPort);

             Session session = Session.getInstance(props, new Authenticator() {
                 @Override
                 protected PasswordAuthentication getPasswordAuthentication() {
                     return new PasswordAuthentication(username, password);
                 }
             });

             try {
                 Message message = new MimeMessage(session);
                 message.setFrom(new InternetAddress(username, "Uptime Monitor", "UTF-8"));
                 message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                 message.setSubject(subject);
                 message.setContent(content, "text/html;charset=UTF-8");

                 Transport.send(message);
                 log.info("邮件发送成功: {}", toEmail);
                 return true;
             } catch (MessagingException e) {
                 log.error("邮件发送失败", e);
                 return false;
             } catch (UnsupportedEncodingException e) {
                 throw new RuntimeException(e);
             }
         }
     }