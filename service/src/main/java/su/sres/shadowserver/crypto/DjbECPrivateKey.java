/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.crypto;

public class DjbECPrivateKey implements ECPrivateKey {

  private final byte[] privateKey;

  DjbECPrivateKey(byte[] privateKey) {
    this.privateKey = privateKey;
  }

  @Override
  public byte[] serialize() {
    return privateKey;
  }

  @Override
  public int getType() {
    return Curve.DJB_TYPE;
  }

  public byte[] getPrivateKey() {
    return privateKey;
  }
}