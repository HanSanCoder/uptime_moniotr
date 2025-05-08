package io.hansan.monitor.dto;

/**
 * 用户上下文，存储当前用户信息
 */
public class UserContext {
    // 默认用户ID为1
    private static final ThreadLocal<Integer> currentUserId = ThreadLocal.withInitial(() -> 1);
    // 默认证书到期警告天数为30天
    private static final ThreadLocal<Integer> expiryDay = ThreadLocal.withInitial(() -> 30);

    private static final ThreadLocal<Long> daysUntilExpiration = ThreadLocal.withInitial(() -> 22L);
    private static final ThreadLocal<String> expirationDate = ThreadLocal.withInitial(() -> "2025-06-02");

    /**
     * 获取证书到期剩余天数
     */
    public static Long getDaysUntilExpiration() {
        return daysUntilExpiration.get();
    }

    /**
     * 设置证书到期剩余天数
     */
    public static void setDaysUntilExpiration(Long days) {
        if (days != null && days > 0) {
            daysUntilExpiration.set(days);
        }
    }

    /**
     * 获取证书过期日期
     */
    public static String getExpirationDate() {
        return expirationDate.get();
    }

    /**
     * 设置证书过期日期
     */
    public static void setExpirationDate(String date) {
        if (date != null && !date.isEmpty()) {
            expirationDate.set(date);
        }
    }


    /**
     * 获取当前用户ID
     */
    public static Integer getCurrentUserId() {
        return currentUserId.get();
    }

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Integer userId) {
        if (userId != null) {
            currentUserId.set(userId);
        }
    }

    /**
     * 获取证书到期警告天数
     */
    public static Integer getExpiryDay() {
        return expiryDay.get();
    }

    /**
     * 设置证书到期警告天数
     */
    public static void setExpiryDay(Integer days) {
        if (days != null && days > 0) {
            expiryDay.set(days);
        }
    }

    /**
     * 清除当前用户ID（退出登录时调用）
     */
    public static void clear() {
        currentUserId.remove();
        expiryDay.remove();
    }
}