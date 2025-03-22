
package io.hansan.monitor.check;

import lombok.Data;

@Data
public class HttpResult {
    private final boolean up;
    private final String message;
    private final long ping;

    public HttpResult(boolean up, String message) {
        this(up, message, 0);
    }

    public HttpResult(boolean up, String message, long ping) {
        this.up = up;
        this.message = message;
        this.ping = ping;
    }

    public boolean isUp() {
        return up;
    }

    public String getMessage() {
        return message;
    }

    public long getPing() {
        return ping;
    }
}