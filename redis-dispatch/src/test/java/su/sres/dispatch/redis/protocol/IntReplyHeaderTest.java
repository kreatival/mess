/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.dispatch.redis.protocol;


import org.junit.Test;

import su.sres.dispatch.redis.protocol.IntReply;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IntReplyHeaderTest {

  @Test(expected = IOException.class)
  public void testNull() throws IOException {
    new IntReply(null);
  }

  @Test(expected = IOException.class)
  public void testEmpty() throws IOException {
    new IntReply("");
  }

  @Test(expected = IOException.class)
  public void testBadNumber() throws IOException {
    new IntReply(":A");
  }

  @Test(expected = IOException.class)
  public void testBadFormat() throws IOException {
    new IntReply("*");
  }

  @Test
  public void testValid() throws IOException {
    assertEquals(23, new IntReply(":23").getValue());
  }
}
