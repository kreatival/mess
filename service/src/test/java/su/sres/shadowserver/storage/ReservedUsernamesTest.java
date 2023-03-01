/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.storage;

import com.opentable.db.postgres.embedded.LiquibasePreparer;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.PreparedDbRule;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import su.sres.shadowserver.configuration.CircuitBreakerConfiguration;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class ReservedUsernamesTest {

  @Rule
  public PreparedDbRule db = EmbeddedPostgresRules.preparedDatabase(LiquibasePreparer.forClasspathLocation("accountsdb.xml"));

  private ReservedUsernames reserved;

  @Before
  public void setupAccountsDao() {
    FaultTolerantDatabase faultTolerantDatabase = new FaultTolerantDatabase("reservedUsernamesTest",
        Jdbi.create(db.getTestDatabase()),
        new CircuitBreakerConfiguration());

    this.reserved = new ReservedUsernames(faultTolerantDatabase);
  }

  @Test
  public void testReservedRegexp() {
    UUID reservedFor = UUID.randomUUID();
    String username = ".*myusername.*";

    reserved.setReserved(username, reservedFor);

    assertTrue(reserved.isReserved("myusername", UUID.randomUUID()));
    assertFalse(reserved.isReserved("myusername", reservedFor));
    assertFalse(reserved.isReserved("thyusername", UUID.randomUUID()));
    assertTrue(reserved.isReserved("somemyusername", UUID.randomUUID()));
    assertTrue(reserved.isReserved("myusernamesome", UUID.randomUUID()));
    assertTrue(reserved.isReserved("somemyusernamesome", UUID.randomUUID()));
  }

  @Test
  public void testReservedLiteral() {
    UUID reservedFor = UUID.randomUUID();
    String username = "^foobar$";

    reserved.setReserved(username, reservedFor);

    assertTrue(reserved.isReserved("foobar", UUID.randomUUID()));
    assertFalse(reserved.isReserved("foobar", reservedFor));
    assertFalse(reserved.isReserved("somefoobar", UUID.randomUUID()));
    assertFalse(reserved.isReserved("foobarsome", UUID.randomUUID()));
    assertFalse(reserved.isReserved("somefoobarsome", UUID.randomUUID()));
  }
}