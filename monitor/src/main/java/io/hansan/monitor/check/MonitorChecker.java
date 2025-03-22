package io.hansan.monitor.check;

import io.hansan.monitor.model.MonitorModel;

public interface MonitorChecker {
    HttpResult check(MonitorModel monitor);
}
