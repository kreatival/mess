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
import su.sres.shadowserver.auth.StoredVerificationCode;
import su.sres.shadowserver.configuration.CircuitBreakerConfiguration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PendingDevicesTest {

  @Rule
  public PreparedDbRule db = EmbeddedPostgresRules.preparedDatabase(LiquibasePreparer.forClasspathLocation("accountsdb.xml"));

  private PendingDevices pendingDevices;

  @Before
  public void setupAccountsDao() {
    this.pendingDevices = new PendingDevices(new FaultTolerantDatabase("peding_devices-test", Jdbi.create(db.getTestDatabase()), new CircuitBreakerConfiguration()));
  }

  @Test
  public void testStore() throws SQLException {
    pendingDevices.insert("alice", "1234", 1111);

    PreparedStatement statement = db.getTestDatabase().getConnection().prepareStatement("SELECT * FROM pending_devices WHERE number = ?");
    statement.setString(1, "alice");

    ResultSet resultSet = statement.executeQuery();

    if (resultSet.next()) {
      assertThat(resultSet.getString("verification_code")).isEqualTo("1234");
      assertThat(resultSet.getLong("timestamp")).isEqualTo(1111);
    } else {
      throw new AssertionError("no results");
    }

    assertThat(resultSet.next()).isFalse();
  }

  @Test
  public void testRetrieve() throws Exception {
    pendingDevices.insert("alice", "4321", 2222);
    pendingDevices.insert("bob", "1212", 5555);

    Optional<StoredVerificationCode> verificationCode = pendingDevices.getCodeForNumber("alice");

    assertThat(verificationCode.isPresent()).isTrue();
    assertThat(verificationCode.get().getCode()).isEqualTo("4321");
    assertThat(verificationCode.get().getTimestamp()).isEqualTo(2222);

    Optional<StoredVerificationCode> missingCode = pendingDevices.getCodeForNumber("pat");
    assertThat(missingCode.isPresent()).isFalse();
  }

  @Test
  public void testOverwrite() throws Exception {
    pendingDevices.insert("alice", "4321", 2222);
    pendingDevices.insert("alice", "4444", 3333);

    Optional<StoredVerificationCode> verificationCode = pendingDevices.getCodeForNumber("alice");

    assertThat(verificationCode.isPresent()).isTrue();
    assertThat(verificationCode.get().getCode()).isEqualTo("4444");
    assertThat(verificationCode.get().getTimestamp()).isEqualTo(3333);
  }

  @Test
  public void testRemove() {
    pendingDevices.insert("alice", "4321", 2222);
    pendingDevices.insert("bob", "1212", 5555);

    Optional<StoredVerificationCode> verificationCode = pendingDevices.getCodeForNumber("alice");

    assertThat(verificationCode.isPresent()).isTrue();
    assertThat(verificationCode.get().getCode()).isEqualTo("4321");
    assertThat(verificationCode.get().getTimestamp()).isEqualTo(2222);

    pendingDevices.remove("alice");

    verificationCode = pendingDevices.getCodeForNumber("alice");
    assertThat(verificationCode.isPresent()).isFalse();

    verificationCode = pendingDevices.getCodeForNumber("bob");
    assertThat(verificationCode.isPresent()).isTrue();
    assertThat(verificationCode.get().getCode()).isEqualTo("1212");
    assertThat(verificationCode.get().getTimestamp()).isEqualTo(5555);
  }

}