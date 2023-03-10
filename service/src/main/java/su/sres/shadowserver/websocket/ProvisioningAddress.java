/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.websocket;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import su.sres.shadowserver.util.Base64;

public class ProvisioningAddress extends WebsocketAddress {

  public ProvisioningAddress(String address, int id) throws InvalidWebsocketAddressException {
    super(address, id);
  }

  public ProvisioningAddress(String serialized) throws InvalidWebsocketAddressException {
    super(serialized);
  }

  public String getAddress() {
    return getNumber();
  }

  public static ProvisioningAddress generate() {
    try {
      byte[] random = new byte[16];
      new SecureRandom().nextBytes(random);

      return new ProvisioningAddress(Base64.encodeBytesWithoutPadding(random)
                                           .replace('+', '-').replace('/', '_'), 0);
    } catch (InvalidWebsocketAddressException e) {
      throw new AssertionError(e);
    }
  }
}
