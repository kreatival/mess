/*
 * Copyright 2013-2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.gcm.server.internal;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GcmResponseListEntity {

  @JsonProperty
  private List<GcmResponseEntity> results;

  public List<GcmResponseEntity> getResults() {
    return results;
  }
}