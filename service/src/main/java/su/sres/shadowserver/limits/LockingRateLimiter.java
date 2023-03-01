/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.limits;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

import io.lettuce.core.SetArgs;

import static com.codahale.metrics.MetricRegistry.name;

import java.time.Duration;

import su.sres.shadowserver.controllers.RateLimitExceededException;
import su.sres.shadowserver.redis.FaultTolerantRedisCluster;
import su.sres.shadowserver.util.Constants;

public class LockingRateLimiter extends RateLimiter {

    private final Meter meter;

    public LockingRateLimiter(FaultTolerantRedisCluster cacheCluster, String name, int bucketSize, double leakRatePerMinute) {
	super(cacheCluster, name, bucketSize, leakRatePerMinute);

	MetricRegistry metricRegistry = SharedMetricRegistries.getOrCreate(Constants.METRICS_NAME);
	this.meter = metricRegistry.meter(name(getClass(), name, "locked"));
    }

    @Override
    public void validate(String key, int amount) throws RateLimitExceededException {
	if (!acquireLock(key)) {
	    meter.mark();
	    throw new RateLimitExceededException("Locked", Duration.ZERO);
	}

	try {
	    super.validate(key, amount);
	} finally {
	    releaseLock(key);
	}
    }

    @Override
    public void validate(String key) throws RateLimitExceededException {
	validate(key, 1);
    }

    private void releaseLock(String key) {
	cacheCluster.useCluster(connection -> connection.sync().del(getLockName(key)));
    }

    private boolean acquireLock(String key) {
	return cacheCluster.withCluster(connection -> connection.sync().set(getLockName(key), "L", SetArgs.Builder.nx().ex(10))) != null;
    }

    private String getLockName(String key) {
	return "leaky_lock::" + name + "::" + key;
    }

}
