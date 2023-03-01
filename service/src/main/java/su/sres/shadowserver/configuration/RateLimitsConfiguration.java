/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.configuration;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RateLimitsConfiguration {

  @JsonProperty
  private RateLimitConfiguration smsDestination = new RateLimitConfiguration(2, 2);

  @JsonProperty
  private RateLimitConfiguration smsVoiceIp = new RateLimitConfiguration(1000, 1000);
  
  @JsonProperty
  private RateLimitConfiguration autoBlock = new RateLimitConfiguration(500, 500);

  @JsonProperty
  private RateLimitConfiguration verifyUserLogin = new RateLimitConfiguration(2, 2);

  @JsonProperty
  private RateLimitConfiguration verifyPin = new RateLimitConfiguration(10, 1 / (24.0 * 60.0));

  @JsonProperty
  private RateLimitConfiguration attachments = new RateLimitConfiguration(50, 50);

  @JsonProperty
  private RateLimitConfiguration contactQueries = new RateLimitConfiguration(50000, 50000);

  @JsonProperty
  private RateLimitConfiguration prekeys = new RateLimitConfiguration(3, 1.0 / 10.0);

  @JsonProperty
  private RateLimitConfiguration messages = new RateLimitConfiguration(60, 60);

  @JsonProperty
  private RateLimitConfiguration allocateDevice = new RateLimitConfiguration(2, 1.0 / 2.0);

  @JsonProperty
  private RateLimitConfiguration verifyDevice = new RateLimitConfiguration(2, 2);

  @JsonProperty
  private RateLimitConfiguration turnAllocations = new RateLimitConfiguration(60, 60);

  @JsonProperty
  private RateLimitConfiguration profile = new RateLimitConfiguration(4320, 3);

  @JsonProperty
  private RateLimitConfiguration stickerPack = new RateLimitConfiguration(50, 20 / (24.0 * 60.0));

  @JsonProperty
  private RateLimitConfiguration usernameLookup = new RateLimitConfiguration(100, 100 / (24.0 * 60.0));

  @JsonProperty
  private RateLimitConfiguration usernameSet = new RateLimitConfiguration(100, 100 / (24.0 * 60.0));

  @JsonProperty
  private RateLimitConfiguration configRequest = new RateLimitConfiguration(2, 2);

  @JsonProperty
  private RateLimitConfiguration certRequest = new RateLimitConfiguration(24, 24 / (24.0 * 60.0));

  @JsonProperty
  private RateLimitConfiguration certVerRequest = new RateLimitConfiguration(24, 24 / (24.0 * 60.0));

  @JsonProperty
  private RateLimitConfiguration directoryRequest = new RateLimitConfiguration(60, 60 / (1.0 * 60.0));

  @JsonProperty
  private RateLimitConfiguration licenseRequest = new RateLimitConfiguration(2, 2);

  public RateLimitConfiguration getAutoBlock() {
    return autoBlock;
  }

  public RateLimitConfiguration getAllocateDevice() {
    return allocateDevice;
  }

  public RateLimitConfiguration getVerifyDevice() {
    return verifyDevice;
  }

  public RateLimitConfiguration getMessages() {
    return messages;
  }

  public RateLimitConfiguration getPreKeys() {
    return prekeys;
  }

  public RateLimitConfiguration getContactQueries() {
    return contactQueries;
  }

  public RateLimitConfiguration getAttachments() {
    return attachments;
  }

  public RateLimitConfiguration getSmsDestination() {
    return smsDestination;
  }
  
  public RateLimitConfiguration getSmsVoiceIp() {
    return smsVoiceIp;
  }  

  public RateLimitConfiguration getVerifyUserLogin() {
    return verifyUserLogin;
  }

  public RateLimitConfiguration getVerifyPin() {
    return verifyPin;
  }

  public RateLimitConfiguration getTurnAllocations() {
    return turnAllocations;
  }

  public RateLimitConfiguration getProfile() {
    return profile;
  }

  public RateLimitConfiguration getStickerPack() {
    return stickerPack;
  }

  public RateLimitConfiguration getUsernameLookup() {
    return usernameLookup;
  }

  public RateLimitConfiguration getUsernameSet() {
    return usernameSet;
  }

  public RateLimitConfiguration getConfigRequest() {
    return configRequest;
  }

  public RateLimitConfiguration getCertRequest() {
    return certRequest;
  }

  public RateLimitConfiguration getCertVerRequest() {
    return certVerRequest;
  }

  public RateLimitConfiguration getDirectoryRequest() {
    return directoryRequest;
  }

  public RateLimitConfiguration getLicenseRequest() {
    return licenseRequest;
  }

  public static class RateLimitConfiguration {
    @JsonProperty
    private int bucketSize;

    @JsonProperty
    private double leakRatePerMinute;

    public RateLimitConfiguration(int bucketSize, double leakRatePerMinute) {
      this.bucketSize = bucketSize;
      this.leakRatePerMinute = leakRatePerMinute;
    }

    public RateLimitConfiguration() {
    }

    public int getBucketSize() {
      return bucketSize;
    }

    public double getLeakRatePerMinute() {
      return leakRatePerMinute;
    }
  }

  public static class CardinalityRateLimitConfiguration {
    @JsonProperty
    private int maxCardinality;

    @JsonProperty
    private Duration ttl;

    @JsonProperty
    private Duration ttlJitter;

    public CardinalityRateLimitConfiguration() {
    }

    public CardinalityRateLimitConfiguration(int maxCardinality, Duration ttl, Duration ttlJitter) {
      this.maxCardinality = maxCardinality;
      this.ttl = ttl;
      this.ttlJitter = ttlJitter;
    }

    public int getMaxCardinality() {
      return maxCardinality;
    }

    public Duration getTtl() {
      return ttl;
    }

    public Duration getTtlJitter() {
      return ttlJitter;
    }
  }
}