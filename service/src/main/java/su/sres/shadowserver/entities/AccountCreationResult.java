/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class AccountCreationResult {

  @JsonProperty
  private UUID uuid;
  
  @JsonProperty
  private boolean storageCapable;

  public AccountCreationResult() {}

  public AccountCreationResult(UUID uuid, boolean storageCapable) {
	    this.uuid           = uuid;
	    this.storageCapable = storageCapable;
  }

  public UUID getUuid() {
    return uuid;
  }
  
  public boolean isStorageCapable() {
	    return storageCapable;
  }
}