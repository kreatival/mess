/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.metrics;

import org.junit.Test;
import su.sres.shadowserver.redis.AbstractRedisClusterTest;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PushLatencyManagerTest extends AbstractRedisClusterTest {

    @Test
    public void testGetLatency() throws ExecutionException, InterruptedException {
	final PushLatencyManager pushLatencyManager = new PushLatencyManager(getRedisCluster());
	final UUID accountUuid = UUID.randomUUID();
	final long deviceId = 1;
	final long expectedLatency = 1234;
	final long pushSentTimestamp = System.currentTimeMillis();
	final long clearQueueTimestamp = pushSentTimestamp + expectedLatency;

	assertNull(pushLatencyManager.getLatencyAndClearTimestamp(accountUuid, deviceId, System.currentTimeMillis()).get());

	{
	    pushLatencyManager.recordPushSent(accountUuid, deviceId, pushSentTimestamp);

	    assertEquals(expectedLatency, (long) pushLatencyManager.getLatencyAndClearTimestamp(accountUuid, deviceId, clearQueueTimestamp).get());
	    assertNull(pushLatencyManager.getLatencyAndClearTimestamp(accountUuid, deviceId, System.currentTimeMillis()).get());
	}
    }
}
