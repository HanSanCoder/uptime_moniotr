
package io.hansan.monitor.check;

import lombok.Data;

@Data
public class HttpResult {
    private final boolean up;
    private final String message;
    private final long ping;
    private boolean important = false;

    public HttpResult(boolean up, String message) {
        this(up, message, 0);
    }

    public HttpResult(boolean up, String message, long ping) {
        this.up = up;
        this.message = message;
        this.ping = ping;
    }

    public HttpResult(boolean up, String message, long ping, boolean important) {
        this.up = up;
        this.message = message;
        this.ping = ping;
        this.important = important;
    }
}