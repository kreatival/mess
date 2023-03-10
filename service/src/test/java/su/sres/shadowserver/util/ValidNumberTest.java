/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.util;

import org.junit.Test;
import su.sres.shadowserver.util.Util;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class ValidNumberTest {

  @Test
  public void testValidE164() {
    assertTrue(Util.isValidNumber("+14151231234"));
    assertTrue(Util.isValidNumber("+71234567890"));
    assertTrue(Util.isValidNumber("+447535742222"));
    assertTrue(Util.isValidNumber("+4915174108888"));
  }

  @Test
  public void testInvalidE164() {
    assertFalse(Util.isValidNumber("+141512312341"));
    assertFalse(Util.isValidNumber("+712345678901"));
    assertFalse(Util.isValidNumber("+4475357422221"));
    assertFalse(Util.isValidNumber("+491517410888811111"));
  }

  @Test
  public void testNotE164() {
    assertFalse(Util.isValidNumber("+1 415 123 1234"));
    assertFalse(Util.isValidNumber("+1 (415) 123-1234"));
    assertFalse(Util.isValidNumber("+1 415)123-1234"));
    assertFalse(Util.isValidNumber("71234567890"));
    assertFalse(Util.isValidNumber("001447535742222"));
    assertFalse(Util.isValidNumber(" +14151231234"));
    assertFalse(Util.isValidNumber("+1415123123a"));
  }

  @Test
  public void testShortRegions() {
    assertTrue(Util.isValidNumber("+298123456"));
    assertTrue(Util.isValidNumber("+299123456"));
    assertTrue(Util.isValidNumber("+376123456"));
    assertTrue(Util.isValidNumber("+68512345"));
    assertTrue(Util.isValidNumber("+689123456"));
  }
}