/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.dispatch.redis.protocol;

import java.io.IOException;

public class StringReplyHeader {

  private final int stringLength;

  public StringReplyHeader(String header) throws IOException {
    if (header == null || header.length() < 2 || header.charAt(0) != '$') {
      throw new IOException("Invalid string reply header: " + header);
    }

    try {
      this.stringLength = Integer.parseInt(header.substring(1));
    } catch (NumberFormatException e) {
      throw new IOException(e);
    }
  }

  public int getStringLength() {
    return stringLength;
  }
}
