/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.storage;

import java.util.List;

public class AbusiveHostRule {

  private final String       host;
  private final boolean      blocked;
  private final List<String> regions;

  public AbusiveHostRule(String host, boolean blocked, List<String> regions) {
    this.host    = host;
    this.blocked = blocked;
    this.regions = regions;
  }

  public List<String> getRegions() {
    return regions;
  }

  public boolean isBlocked() {
    return blocked;
  }

  public String getHost() {
    return host;
  }
}