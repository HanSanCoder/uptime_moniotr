package io.hansan.monitor.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.mapper.NotificationMapper;
import io.hansan.monitor.model.NotificationModel;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

@Component
public class NotificationService extends ServiceImpl<NotificationMapper, NotificationModel> {

    @Inject
    private NotificationMapper notificationMapper;

    /**
     * 获取所有通知配置
     */
    public List<NotificationModel> list() {
        return super.list();
    }

    /**
     * 添加新的通知配置
     */
    public boolean addNotification(NotificationModel notification) {
        return this.save(notification);
    }

    /**
     * 删除通知配置
     */
    public boolean deleteNotification(Integer id) {
        return this.removeById(id);
    }

    /**
     * 更新通知配置
     */
    public boolean updateNotification(NotificationModel notification) {
        return this.updateById(notification);
    }


    /**
     * 发送邮件通知
     */
    public void sendEmailNotification(NotificationModel notification, String message) {
        // 实现邮件发送逻辑
    }

    /**
     * 发送Webhook通知
     */
    public void sendWebhookNotification(NotificationModel notification, String message) {
        // 实现Webhook通知逻辑
    }

    public List<NotificationModel> listAll() {
        return notificationMapper.selectList(null);
    }
}