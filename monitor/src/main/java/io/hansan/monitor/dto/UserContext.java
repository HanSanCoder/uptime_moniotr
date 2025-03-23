package io.hansan.monitor.dto;

/**
 * 用户上下文，存储当前用户信息
 */
public class UserContext {
    // 默认用户ID为1
    private static final ThreadLocal<Integer> currentUserId = ThreadLocal.withInitial(() -> 1);

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
     * 清除当前用户ID（退出登录时调用）
     */
    public static void clear() {
        currentUserId.remove();
    }
}