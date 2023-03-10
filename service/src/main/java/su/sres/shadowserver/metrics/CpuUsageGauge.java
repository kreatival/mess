/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.metrics;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Gauge;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class CpuUsageGauge extends CachedGauge<Integer> {

    private final OperatingSystemMXBean operatingSystemMXBean;

    public CpuUsageGauge(final long timeout, final TimeUnit timeoutUnit) {
	    super(timeout, timeoutUnit);
	this.operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    @Override
    protected Integer loadValue() {
	return (int) Math.ceil(operatingSystemMXBean.getSystemCpuLoad() * 100);
    }
}
