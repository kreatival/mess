/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.websocket.logging.layout.converters;

import su.sres.websocket.logging.WebsocketEvent;

import ch.qos.logback.core.CoreConstants;

public class LineSeparatorConverter extends WebSocketEventConverter {
  public LineSeparatorConverter() {
  }

  public String convert(WebsocketEvent event) {
    return CoreConstants.LINE_SEPARATOR;
  }
}
