/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.controllers;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.signal.zkgroup.InvalidInputException;
import org.signal.zkgroup.ServerSecretParams;
import org.signal.zkgroup.VerificationFailedException;
import org.signal.zkgroup.auth.AuthCredential;
import org.signal.zkgroup.auth.AuthCredentialResponse;
import org.signal.zkgroup.auth.ClientZkAuthOperations;
import org.signal.zkgroup.auth.ServerZkAuthOperations;
import su.sres.shadowserver.auth.CertificateGenerator;
import su.sres.shadowserver.auth.DisabledPermittedAccount;
import su.sres.shadowserver.auth.OptionalAccess;
import su.sres.shadowserver.crypto.Curve;
import su.sres.shadowserver.entities.DeliveryCertificate;
import su.sres.shadowserver.entities.GroupCredentials;
import su.sres.shadowserver.entities.MessageProtos.SenderCertificate;
import su.sres.shadowserver.entities.MessageProtos.ServerCertificate;
import su.sres.shadowserver.storage.Account;
import su.sres.shadowserver.util.AuthHelper;
import su.sres.shadowserver.util.Base64;
import su.sres.shadowserver.util.SystemMapper;
import su.sres.shadowserver.util.Util;

import io.dropwizard.auth.PolymorphicAuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.io.IOException;
import java.util.Arrays;
import javax.ws.rs.core.Response;

public class CertificateControllerTest {

  private static final String caPublicKey = "BWh+UOhT1hD8bkb+MFRvb6tVqhoG8YYGCzOd7mgjo8cV";
  private static final String caPrivateKey = "EO3Mnf0kfVlVnwSaqPoQnAxhnnGL1JTdXqktCKEe9Eo=";

  private static final String signingCertificate = "CiUIDBIhBbTz4h1My+tt+vw+TVscgUe/DeHS0W02tPWAWbTO2xc3EkD+go4bJnU0AcnFfbOLKoiBfCzouZtDYMOVi69rE7r4U9cXREEqOkUmU2WJBjykAxWPCcSTmVTYHDw7hkSp/puG";
  private static final String signingKey = "ABOxG29xrfq4E7IrW11Eg7+HBbtba9iiS0500YoBjn4=";

  private static ServerSecretParams serverSecretParams = ServerSecretParams.generate();
  private static CertificateGenerator certificateGenerator;
  private static ServerZkAuthOperations serverZkAuthOperations;

  static {
    try {
      certificateGenerator = new CertificateGenerator(Base64.decode(signingCertificate), Curve.decodePrivatePoint(Base64.decode(signingKey)), 1);
      serverZkAuthOperations = new ServerZkAuthOperations(serverSecretParams);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder().addProvider(AuthHelper.getAuthFilter())
      .addProvider(new PolymorphicAuthValueFactoryProvider.Binder<>(
          ImmutableSet.of(Account.class, DisabledPermittedAccount.class)))
      .setMapper(SystemMapper.getMapper()).setTestContainerFactory(new GrizzlyWebTestContainerFactory())
      .addResource(new CertificateController(certificateGenerator, serverZkAuthOperations, true))
      .build();

  @Test
  public void testValidCertificate() throws Exception {
    DeliveryCertificate certificateObject = resources.getJerseyTest().target("/v1/certificate/delivery").request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(DeliveryCertificate.class);

    SenderCertificate certificateHolder = SenderCertificate.parseFrom(certificateObject.getCertificate());
    SenderCertificate.Certificate certificate = SenderCertificate.Certificate
        .parseFrom(certificateHolder.getCertificate());

    ServerCertificate serverCertificateHolder = certificate.getSigner();
    ServerCertificate.Certificate serverCertificate = ServerCertificate.Certificate
        .parseFrom(serverCertificateHolder.getCertificate());

    assertTrue(Curve.verifySignature(Curve.decodePoint(serverCertificate.getKey().toByteArray(), 0),
        certificateHolder.getCertificate().toByteArray(), certificateHolder.getSignature().toByteArray()));
    assertTrue(Curve.verifySignature(Curve.decodePoint(Base64.decode(caPublicKey), 0),
        serverCertificateHolder.getCertificate().toByteArray(),
        serverCertificateHolder.getSignature().toByteArray()));

    assertEquals(certificate.getSender(), AuthHelper.VALID_NUMBER);
    assertEquals(certificate.getSenderDevice(), 1L);
    assertTrue(certificate.hasSenderUuid());
    assertEquals(AuthHelper.VALID_UUID.toString(), certificate.getSenderUuid());
    assertTrue(Arrays.equals(certificate.getIdentityKey().toByteArray(), Base64.decode(AuthHelper.VALID_IDENTITY)));
  }

  @Test
  public void testValidCertificateWithUuid() throws Exception {
    DeliveryCertificate certificateObject = resources.getJerseyTest().target("/v1/certificate/delivery")
        .queryParam("includeUuid", "true").request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(DeliveryCertificate.class);

    SenderCertificate certificateHolder = SenderCertificate.parseFrom(certificateObject.getCertificate());
    SenderCertificate.Certificate certificate = SenderCertificate.Certificate
        .parseFrom(certificateHolder.getCertificate());

    ServerCertificate serverCertificateHolder = certificate.getSigner();
    ServerCertificate.Certificate serverCertificate = ServerCertificate.Certificate
        .parseFrom(serverCertificateHolder.getCertificate());

    assertTrue(Curve.verifySignature(Curve.decodePoint(serverCertificate.getKey().toByteArray(), 0),
        certificateHolder.getCertificate().toByteArray(), certificateHolder.getSignature().toByteArray()));
    assertTrue(Curve.verifySignature(Curve.decodePoint(Base64.decode(caPublicKey), 0),
        serverCertificateHolder.getCertificate().toByteArray(),
        serverCertificateHolder.getSignature().toByteArray()));

    assertEquals(certificate.getSender(), AuthHelper.VALID_NUMBER);
    assertEquals(certificate.getSenderDevice(), 1L);
    assertEquals(certificate.getSenderUuid(), AuthHelper.VALID_UUID.toString());
    assertTrue(Arrays.equals(certificate.getIdentityKey().toByteArray(), Base64.decode(AuthHelper.VALID_IDENTITY)));
  }

  @Test
  public void testValidCertificateWithUuidNoE164() throws Exception {
    DeliveryCertificate certificateObject = resources.getJerseyTest()
        .target("/v1/certificate/delivery")
        .queryParam("includeUuid", "true")
        .queryParam("includeE164", "false")
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(DeliveryCertificate.class);

    SenderCertificate certificateHolder = SenderCertificate.parseFrom(certificateObject.getCertificate());
    SenderCertificate.Certificate certificate = SenderCertificate.Certificate.parseFrom(certificateHolder.getCertificate());

    ServerCertificate serverCertificateHolder = certificate.getSigner();
    ServerCertificate.Certificate serverCertificate = ServerCertificate.Certificate.parseFrom(serverCertificateHolder.getCertificate());

    assertTrue(Curve.verifySignature(Curve.decodePoint(serverCertificate.getKey().toByteArray(), 0), certificateHolder.getCertificate().toByteArray(), certificateHolder.getSignature().toByteArray()));
    assertTrue(Curve.verifySignature(Curve.decodePoint(Base64.decode(caPublicKey), 0), serverCertificateHolder.getCertificate().toByteArray(), serverCertificateHolder.getSignature().toByteArray()));

    assertTrue(StringUtils.isBlank(certificate.getSender()));
    assertEquals(certificate.getSenderDevice(), 1L);
    assertEquals(certificate.getSenderUuid(), AuthHelper.VALID_UUID.toString());
    assertTrue(Arrays.equals(certificate.getIdentityKey().toByteArray(), Base64.decode(AuthHelper.VALID_IDENTITY)));
  }

  @Test
  public void testBadAuthentication() throws Exception {
    Response response = resources.getJerseyTest().target("/v1/certificate/delivery").request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.INVALID_PASSWORD))
        .get();

    assertEquals(response.getStatus(), 401);
  }

  @Test
  public void testNoAuthentication() throws Exception {
    Response response = resources.getJerseyTest().target("/v1/certificate/delivery").request().get();

    assertEquals(response.getStatus(), 401);
  }

  @Test
  public void testUnidentifiedAuthentication() throws Exception {
    Response response = resources.getJerseyTest().target("/v1/certificate/delivery").request()
        .header(OptionalAccess.UNIDENTIFIED, AuthHelper.getUnidentifiedAccessHeader("1234".getBytes())).get();

    assertEquals(response.getStatus(), 401);
  }

  @Test
  public void testDisabledAuthentication() throws Exception {
    Response response = resources.getJerseyTest().target("/v1/certificate/delivery").request()
        .header("Authorization",
            AuthHelper.getAuthHeader(AuthHelper.DISABLED_NUMBER, AuthHelper.DISABLED_PASSWORD))
        .get();

    assertEquals(response.getStatus(), 401);
  }

  @Test
  public void testGetSingleAuthCredential() throws InvalidInputException, VerificationFailedException {
    GroupCredentials credentials = resources.getJerseyTest()
        .target("/v1/certificate/group/" + Util.currentDaysSinceEpoch() + "/" + Util.currentDaysSinceEpoch())
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(GroupCredentials.class);

    assertThat(credentials.getCredentials().size()).isEqualTo(1);
    assertThat(credentials.getCredentials().get(0).getRedemptionTime()).isEqualTo(Util.currentDaysSinceEpoch());

    ClientZkAuthOperations clientZkAuthOperations = new ClientZkAuthOperations(serverSecretParams.getPublicParams());
    AuthCredential credential = clientZkAuthOperations.receiveAuthCredential(AuthHelper.VALID_UUID, Util.currentDaysSinceEpoch(), new AuthCredentialResponse(credentials.getCredentials().get(0).getCredential()));
  }

  @Test
  public void testGetWeekLongAuthCredentials() throws InvalidInputException, VerificationFailedException {
    GroupCredentials credentials = resources.getJerseyTest()
        .target("/v1/certificate/group/" + Util.currentDaysSinceEpoch() + "/" + (Util.currentDaysSinceEpoch() + 7))
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(GroupCredentials.class);

    assertThat(credentials.getCredentials().size()).isEqualTo(8);

    for (int i = 0; i <= 7; i++) {
      assertThat(credentials.getCredentials().get(i).getRedemptionTime()).isEqualTo(Util.currentDaysSinceEpoch() + i);

      ClientZkAuthOperations clientZkAuthOperations = new ClientZkAuthOperations(serverSecretParams.getPublicParams());
      AuthCredential credential = clientZkAuthOperations.receiveAuthCredential(AuthHelper.VALID_UUID, Util.currentDaysSinceEpoch() + i, new AuthCredentialResponse(credentials.getCredentials().get(i).getCredential()));
    }
  }

  @Test
  public void testTooManyDaysOut() throws InvalidInputException {
    Response response = resources.getJerseyTest()
        .target("/v1/certificate/group/" + Util.currentDaysSinceEpoch() + "/" + (Util.currentDaysSinceEpoch() + 8))
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get();

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void testBackwardsInTime() throws InvalidInputException {
    Response response = resources.getJerseyTest()
        .target("/v1/certificate/group/" + (Util.currentDaysSinceEpoch() - 1) + "/" + (Util.currentDaysSinceEpoch() + 7))
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get();

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  public void testBadAuth() throws InvalidInputException {
    Response response = resources.getJerseyTest()
        .target("/v1/certificate/group/" + Util.currentDaysSinceEpoch() + "/" + (Util.currentDaysSinceEpoch() + 7))
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.INVALID_PASSWORD))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }
}