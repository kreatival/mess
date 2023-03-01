/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.configuration;


import com.fasterxml.jackson.annotation.JsonProperty;

import su.sres.shadowserver.federation.FederatedPeer;

import java.util.List;

public class FederationConfiguration {

  @JsonProperty
  private List<FederatedPeer> peers;

  @JsonProperty
  private String name;

  public List<FederatedPeer> getPeers() {
    return peers;
  }

  public String getName() {
    return name;
  }
}
