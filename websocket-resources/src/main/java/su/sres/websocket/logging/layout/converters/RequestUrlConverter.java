/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.websocket.logging.layout.converters;

import su.sres.websocket.logging.WebsocketEvent;

public class RequestUrlConverter extends WebSocketEventConverter {
  @Override
  public String convert(WebsocketEvent event) {
    return
        event.getMethod()                  +
        WebSocketEventConverter.SPACE_CHAR +
        event.getPath()                    +
        WebSocketEventConverter.SPACE_CHAR +
        event.getProtocol();
  }
}
