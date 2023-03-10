/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.websocket.session;

import org.glassfish.jersey.server.ContainerRequest;
import su.sres.websocket.WebSocketSecurityContext;

import javax.ws.rs.core.SecurityContext;

public class WebSocketSessionContainerRequestValueFactory {
  private final ContainerRequest request;

  public WebSocketSessionContainerRequestValueFactory(ContainerRequest request) {
    this.request = request;
  }

  public WebSocketSessionContext provide() {
    SecurityContext securityContext = request.getSecurityContext();

    if (!(securityContext instanceof WebSocketSecurityContext)) {
      throw new IllegalStateException("Security context isn't for websocket!");
    }

    WebSocketSessionContext sessionContext = ((WebSocketSecurityContext)securityContext).getSessionContext();

    if (sessionContext == null) {
      throw new IllegalStateException("No session context found for websocket!");
    }

    return sessionContext;
  }

}
