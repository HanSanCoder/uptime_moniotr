package io.hansan.monitor.service;

        import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
        import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
        import io.hansan.monitor.model.HeartbeatModel;
        import io.hansan.monitor.mapper.HeartbeatMapper;
        import lombok.extern.slf4j.Slf4j;
        import org.noear.solon.annotation.Component;
        import org.noear.solon.annotation.Inject;

        import java.time.LocalDateTime;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.stream.Collectors;

        @Slf4j
        @Component
        public class HeartbeatService extends ServiceImpl<HeartbeatMapper, HeartbeatModel> {

            @Inject
            private MaintenanceService maintenanceService;

            @Inject
            private HeartbeatMapper heartbeatMapper;
            /**
             * Add a heartbeat record and return the heartbeat object for client sending
             */
            public Map<String, Object> add(Integer monitorId, Integer status, String msg, Double ping, Boolean important) {
                // Check if under maintenance mode

                // Create heartbeat record
                HeartbeatModel heartbeat = new HeartbeatModel();
                heartbeat.setMonitorId(monitorId);
                heartbeat.setStatus(status);
                heartbeat.setMsg(msg);
                heartbeat.setPing(ping != null ? ping.intValue() : null);
                heartbeat.setImportant(important != null && important);
                heartbeat.setTime(LocalDateTime.now());

                // Save to database
                this.save(heartbeat);

                // Return format for Socket server sending
                return toBeatObject(heartbeat);
            }

            /**
             * Get heartbeat records for a specific monitor
             */
            public List<Map<String, Object>> getBeats(Integer monitorId, Integer period) {
                LocalDateTime timeOffset = LocalDateTime.now();

                if (period > 0) {
                    timeOffset = timeOffset.minusHours(period);
                } else {
                    timeOffset = timeOffset.minusYears(100); // Return all if period is 0
                }

                LambdaQueryWrapper<HeartbeatModel> wrapper = new LambdaQueryWrapper<HeartbeatModel>()
                        .eq(HeartbeatModel::getMonitorId, monitorId)
                        .gt(HeartbeatModel::getTime, timeOffset)
                        .orderByAsc(HeartbeatModel::getTime);

                List<HeartbeatModel> beats = this.list(wrapper);

                return beats.stream()
                        .map(this::toBeatObject)
                        .collect(Collectors.toList());
            }

            /**
             * Get important heartbeat records
             */
            public List<Map<String, Object>> getImportantHeartbeats(Integer monitorId, Integer limit) {
                if (limit == null || limit <= 0) {
                    limit = 50;
                }

                LambdaQueryWrapper<HeartbeatModel> wrapper = new LambdaQueryWrapper<HeartbeatModel>()
                        .eq(HeartbeatModel::getMonitorId, monitorId)
                        .eq(HeartbeatModel::getImportant, true)
                        .orderByDesc(HeartbeatModel::getTime)
                        .last("LIMIT " + limit);

                List<HeartbeatModel> beats = this.list(wrapper);

                return beats.stream()
                        .map(this::toBeatObject)
                        .collect(Collectors.toList());
            }

            /**
             * Clear all heartbeat records for a specific monitor
             */
            public boolean clearHeartbeats(Integer monitorId) {
                LambdaQueryWrapper<HeartbeatModel> wrapper = new LambdaQueryWrapper<HeartbeatModel>()
                        .eq(HeartbeatModel::getMonitorId, monitorId);

                return this.remove(wrapper);
            }

            /**
             * Get the latest heartbeat records for all monitors
             */
            public Map<String, Map<String, Object>> getAllLastHeartbeat() {
                // Use native mapper method to get the last heartbeat for each monitor
                // You need to implement this method in HeartbeatMapper interface
                List<HeartbeatModel> lastHeartbeats = this.heartbeatMapper.findAllLastHeartbeats();

                Map<String, Map<String, Object>> result = new HashMap<>();
                for (HeartbeatModel beat : lastHeartbeats) {
                    result.put(beat.getMonitorId().toString(), toBeatObject(beat));
                }

                return result;
            }

            public List<HeartbeatModel> listAll() {
                return heartbeatMapper.findAllLastHeartbeats();
            }
            /**
             * Calculate average response time
             */
            public Double getAveragePing(Integer monitorId) {
                LocalDateTime timeOffset = LocalDateTime.now().minusHours(24);

                LambdaQueryWrapper<HeartbeatModel> wrapper = new LambdaQueryWrapper<HeartbeatModel>()
                        .eq(HeartbeatModel::getMonitorId, monitorId)
                        .eq(HeartbeatModel::getStatus, 1) // UP
                        .gt(HeartbeatModel::getTime, timeOffset)
                        .isNotNull(HeartbeatModel::getPing);

                // Get all heartbeats
                List<HeartbeatModel> beats = this.list(wrapper);

                if (beats.isEmpty()) {
                    return 0.0;
                }

                // Calculate average manually
                double sum = beats.stream()
                        .mapToInt(HeartbeatModel::getPing)
                        .average()
                        .orElse(0.0);

                return sum;
            }

            /**
             * Calculate uptime
             */
            public Double getUptime(Integer monitorId, Integer period) {
                LocalDateTime timeOffset = LocalDateTime.now().minusHours(period);

                // Total heartbeats
                LambdaQueryWrapper<HeartbeatModel> totalWrapper = new LambdaQueryWrapper<HeartbeatModel>()
                        .eq(HeartbeatModel::getMonitorId, monitorId)
                        .gt(HeartbeatModel::getTime, timeOffset);

                long total = this.count(totalWrapper);

                if (total == 0) {
                    return 0.0;
                }

                // UP status heartbeats
                LambdaQueryWrapper<HeartbeatModel> upWrapper = new LambdaQueryWrapper<HeartbeatModel>()
                        .eq(HeartbeatModel::getMonitorId, monitorId)
                        .eq(HeartbeatModel::getStatus, 1) // UP
                        .gt(HeartbeatModel::getTime, timeOffset);

                long up = this.count(upWrapper);

                return (double) up / total * 100;
            }

            /**
             * Convert heartbeat record to client sending format
             */
            private Map<String, Object> toBeatObject(HeartbeatModel beat) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", beat.getId());
                result.put("monitorID", beat.getMonitorId());
                result.put("status", beat.getStatus());
                result.put("time", beat.getTime().toString());
                result.put("msg", beat.getMsg());
                result.put("ping", beat.getPing());
                result.put("important", beat.getImportant() != null && beat.getImportant());

                return result;
            }

            /**
             * Provide heartbeat list for WebSocket service
             */
            public Map<String, Object> getHeartbeatListData(Integer monitorId, Integer period) {
                List<Map<String, Object>> beats = getBeats(monitorId, period);

                Map<String, Object> response = new HashMap<>();
                response.put("monitorId", monitorId);
                response.put("beats", beats);

                return response;
            }

            /**
             * Get monitor statistics
             */
            public Map<String, Object> getMonitorStats(Integer monitorId) {
                Map<String, Object> stats = new HashMap<>();

                // Average response time
                Double avgPing = getAveragePing(monitorId);
                stats.put("avgPing", avgPing);

                // 24-hour uptime
                Double uptime24h = getUptime(monitorId, 24);
                stats.put("uptime24h", uptime24h);

                // 30-day uptime
                Double uptime30d = getUptime(monitorId, 24 * 30);
                stats.put("uptime30d", uptime30d);

                return stats;
            }
        }