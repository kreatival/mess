/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotEmpty;

public class ApnConfiguration {

    @NotEmpty
    @JsonProperty
    private String teamId;

    @NotEmpty
    @JsonProperty
    private String keyId;

    @NotEmpty
    @JsonProperty
    private String signingKey;

    @NotEmpty
    @JsonProperty
    private String bundleId;

    @JsonProperty
    private boolean sandbox = false;

    public String getTeamId() {
	return teamId;
    }

    public String getKeyId() {
	return keyId;
    }

    public String getSigningKey() {
	return signingKey;
    }

    public String getBundleId() {
	return bundleId;
    }

    public boolean isSandboxEnabled() {
	return sandbox;
    }
}
