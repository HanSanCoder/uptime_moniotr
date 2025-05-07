package io.hansan.monitor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.hansan.monitor.dto.Result;
import io.hansan.monitor.dto.UserContext;
import io.hansan.monitor.mapper.HeartbeatMapper;
import io.hansan.monitor.mapper.NotificationMapper;
import io.hansan.monitor.model.HeartbeatModel;
import io.hansan.monitor.model.MonitorModel;
import io.hansan.monitor.model.NotificationModel;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
@Slf4j
@Component
public class NotificationService extends ServiceImpl<NotificationMapper, NotificationModel> {

    @Inject
    private NotificationMapper notificationMapper;
    @Inject
    private EmailService emailService;
    @Inject
    private HeartbeatMapper heartbeatMapper;
    /**
     * 获取所有通知配置
     */
    public List<NotificationModel> list() {
        return super.list();
    }

    /**
     * 添加新的通知配置
     */
    public Result addNotification(NotificationModel notification) {
        notification.setUserId(UserContext.getCurrentUserId());
        notificationMapper.deleteById(notification.getId());
        boolean success = this.save(notification);
        Result result = new Result();
        if (success) {
            result.setOk(true);
            result.setMsg("添加通知选项成功");
        } else {
            result.setOk(false);
            result.setMsg("添加通知选项失败");
        }
        return result;
    }

    /**
     * 删除通知配置
     */
    public Result deleteNotification(Integer id) {
        notificationMapper.deleteMonitorNotification(id);
        notificationMapper.deleteById(id);
        Result result = new Result();
        result.setOk(true);
        result.setMsg("删除通知成功");
        return result;
    }

    /**
     * 更新通知配置
     */
    public boolean updateNotification(NotificationModel notification) {
        return this.updateById(notification);
    }

    public List<String> getNotificationByMonitorId(Integer MonitorId) {
        return notificationMapper.getNotificationByMonitorId(MonitorId);
    }

    public List<NotificationModel> listAll() {
        LambdaQueryWrapper<NotificationModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationModel::getUserId, UserContext.getCurrentUserId());
        return notificationMapper.selectList(wrapper);
    }

    /**
     * 处理服务状态变化的通知逻辑
     */
    // 根据通知规则处理
    public void handleStatusChangeNotification(MonitorModel monitor) {

        // 检查最近的3次心跳
        List<HeartbeatModel> recentHeartbeats = heartbeatMapper.selectList(
                new LambdaQueryWrapper<HeartbeatModel>()
                        .eq(HeartbeatModel::getMonitorId, monitor.getId())
                        .orderByDesc(HeartbeatModel::getTime)
                        .last("LIMIT 3")
        );

        // 检查是否有3个心跳且全部为在线状态
        boolean allUp = recentHeartbeats.size() == 3 &&
                recentHeartbeats.stream().allMatch(h -> h.getStatus() == 1);

        // 检查是否有3个心跳且全部为离线状态（status为0或2均视为离线）
        boolean allDown = recentHeartbeats.size() == 3 &&
                recentHeartbeats.stream().allMatch(h -> h.getStatus() == 0 || h.getStatus() == 2);

        if (allDown && !monitor.isNotified()) {
            emailService.sendDownNotification(monitor);
        } else if (allUp && monitor.isNotified()) {
            // 计算停机时间
            long downDuration = 0;
            List<HeartbeatModel> downHeartbeats = heartbeatMapper.selectList(
                    new LambdaQueryWrapper<HeartbeatModel>()
                            .eq(HeartbeatModel::getMonitorId, monitor.getId())
                            .eq(HeartbeatModel::getStatus, 0)
                            .orderByAsc(HeartbeatModel::getTime)
            );
            if (downHeartbeats != null && !downHeartbeats.isEmpty()) {
                // 记录连续的停机时段
                HeartbeatModel segmentStart = downHeartbeats.get(0);
                HeartbeatModel previous = segmentStart;

                for (int i = 1; i < downHeartbeats.size(); i++) {
                    HeartbeatModel current = downHeartbeats.get(i);
                    // 定义连续性阈值
                    long continuityCutoff = monitor.getCheck_interval() != null
                        ? monitor.getCheck_interval() * 2L
                        : 1200L; // 默认值为1200秒（20分钟）

                    // 计算时间差，注意处理空值情况
                    long timeDiff = 0;
                    if (previous.getTime() != null && current.getTime() != null) {
                        timeDiff = java.time.Duration.between(previous.getTime(), current.getTime()).getSeconds();
                    }

                    if (timeDiff > continuityCutoff) {
                        // 发现间隔，计算当前段并累加
                        downDuration += calculateSegmentDuration(segmentStart, previous, monitor.getCheck_interval());

                        // 记录新段的开始
                        segmentStart = current;
                    }
                    previous = current;
                }

                // 处理最后一段时间
                downDuration += calculateSegmentDuration(segmentStart, previous, monitor.getCheck_interval());
            }
            // 如果没有记录到停机心跳，则可能需要使用默认值或其他方式处理
            if (downDuration == 0 && monitor.isNotified()) {
                // 没有停机记录，但系统认为服务已经恢复
                // 使用一个默认最小停机时间，避免发送没有停机时间的通知
                downDuration = monitor.getCheck_interval() != null ? monitor.getCheck_interval() : 600L;
                log.warn("服务 {} 恢复，但没有找到停机记录，使用默认停机时间: {}秒", monitor.getName(), downDuration);
            }
            emailService.sendUpNotification(monitor, downDuration);
        }
    }

    /**
     * 计算单个停机段的持续时间
     * 处理可能的空值情况
     */
    private long calculateSegmentDuration(HeartbeatModel start, HeartbeatModel end, Integer checkInterval) {
        long duration = 0;
        if (start.getTime() != null && end.getTime() != null) {
            duration = java.time.Duration.between(start.getTime(), end.getTime()).getSeconds();
        }
        // 添加检查间隔，确保有默认值
        long interval = checkInterval != null ? checkInterval : 600L;
        return duration + interval;
    }
}