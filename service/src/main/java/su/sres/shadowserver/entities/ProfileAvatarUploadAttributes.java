/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProfileAvatarUploadAttributes {

  @JsonProperty
  private String key;

  @JsonProperty
  private String credential;

  @JsonProperty
  private String acl;

  @JsonProperty
  private String algorithm;

  @JsonProperty
  private String date;

  @JsonProperty
  private String policy;

  @JsonProperty
  private String signature;

  public ProfileAvatarUploadAttributes() {}

  public ProfileAvatarUploadAttributes(String key, String credential,
                                       String acl,  String algorithm,
                                       String date, String policy,
                                       String signature)
  {
    this.key        = key;
    this.credential = credential;
    this.acl        = acl;
    this.algorithm  = algorithm;
    this.date       = date;
    this.policy     = policy;
    this.signature  = signature;
  }
  
  public String getKey() {
	    return key;
	  }

}
