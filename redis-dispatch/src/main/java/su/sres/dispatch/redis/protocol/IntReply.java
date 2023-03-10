/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.dispatch.redis.protocol;

import java.io.IOException;

public class IntReply {

  private final int value;

  public IntReply(String reply) throws IOException {
    if (reply == null || reply.length() < 2 || reply.charAt(0) != ':') {
      throw new IOException("Invalid int reply: " + reply);
    }

    try {
      this.value = Integer.parseInt(reply.substring(1));
    } catch (NumberFormatException e) {
      throw new IOException(e);
    }
  }

  public int getValue() {
    return value;
  }
}
