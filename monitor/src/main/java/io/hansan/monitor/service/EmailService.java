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