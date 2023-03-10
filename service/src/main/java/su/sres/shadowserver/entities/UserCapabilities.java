/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserCapabilities {

    @JsonProperty
    private boolean gv2;

    @JsonProperty("gv1-migration")
    private boolean gv1Migration;

    public UserCapabilities() {
    }

    public UserCapabilities(boolean gv2, boolean gv1Migration) {
	this.gv2 = gv2;
	this.gv1Migration = gv1Migration;
    }

    public boolean isGv2() {
	return gv2;
    }

    public boolean isGv1Migration() {
	return gv1Migration;
    }
}