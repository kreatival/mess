/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.gcm.server;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static su.sres.gcm.server.util.JsonHelpers.jsonFixture;

public class MessageTest {

  @Test
  public void testMinimal() throws IOException {
    Message message = Message.newBuilder()
                             .withDestination("1")
                             .build();

    assertEquals(message.serialize(), jsonFixture("fixtures/message-minimal.json"));
  }

  @Test
  public void testComplete() throws IOException {
    Message message = Message.newBuilder()
                             .withDestination("1")
                             .withCollapseKey("collapse")
                             .withDelayWhileIdle(true)
                             .withTtl(10)
                             .withPriority("high")
                             .build();

    assertEquals(message.serialize(), jsonFixture("fixtures/message-complete.json"));
  }

  @Test
  public void testWithData() throws IOException {
    Message message = Message.newBuilder()
                             .withDestination("2")
                             .withDataPart("key1", "value1")
                             .withDataPart("key2", "value2")
                             .build();

    assertEquals(message.serialize(), jsonFixture("fixtures/message-data.json"));
  }

}