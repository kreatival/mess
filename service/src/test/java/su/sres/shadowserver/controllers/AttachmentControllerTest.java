/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.controllers;

import com.google.common.collect.ImmutableSet;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.MalformedURLException;

import io.dropwizard.auth.PolymorphicAuthValueFactoryProvider;
import io.dropwizard.testing.junit.ResourceTestRule;

import su.sres.shadowserver.auth.DisabledPermittedAccount;
import su.sres.shadowserver.entities.AttachmentDescriptorV1;
import su.sres.shadowserver.entities.AttachmentDescriptorV2;
import su.sres.shadowserver.entities.AttachmentUri;

// federation excluded, reserved for future use
// import su.sres.shadowserver.federation.FederatedClientManager;

import su.sres.shadowserver.limits.RateLimiter;
import su.sres.shadowserver.limits.RateLimiters;
import su.sres.shadowserver.storage.Account;
import su.sres.shadowserver.util.AuthHelper;
import su.sres.shadowserver.util.Base64;
import su.sres.shadowserver.util.SystemMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttachmentControllerTest {

  // federation excluded, reserved for future use
  // private static FederatedClientManager federatedClientManager =
  // mock(FederatedClientManager.class );
  private static RateLimiters rateLimiters = mock(RateLimiters.class);
  private static RateLimiter rateLimiter = mock(RateLimiter.class);

  static {

    when(rateLimiters.getAttachmentLimiter()).thenReturn(rateLimiter);
  }

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addProvider(AuthHelper.getAuthFilter())
      .addProvider(new PolymorphicAuthValueFactoryProvider.Binder<>(ImmutableSet.of(Account.class, DisabledPermittedAccount.class)))
      .setMapper(SystemMapper.getMapper())
      .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
      // federation excluded, reserved for future use
      // .addResource(new AttachmentController(rateLimiters, federatedClientManager,
      // urlSigner))
      .addResource(new AttachmentControllerV1(rateLimiters, "accessKey", "accessSecret", "attachment-bucket", "https://minio.example.com"))
      .addResource(new AttachmentControllerV2(rateLimiters, "accessKey", "accessSecret", "us-east-1", "attachmentv2-bucket"))
      .build();

  @Test
  public void testV2Form() throws IOException {
    AttachmentDescriptorV2 descriptor = resources.getJerseyTest()
        .target("/v2/attachments/form/upload")
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER, AuthHelper.VALID_PASSWORD))
        .get(AttachmentDescriptorV2.class);

    assertThat(descriptor.getKey()).isEqualTo(descriptor.getAttachmentIdString());
    assertThat(descriptor.getAcl()).isEqualTo("private");
    assertThat(descriptor.getAlgorithm()).isEqualTo("AWS4-HMAC-SHA256");
    assertThat(descriptor.getAttachmentId()).isGreaterThan(0);
    assertThat(String.valueOf(descriptor.getAttachmentId())).isEqualTo(descriptor.getAttachmentIdString());

    String[] credentialParts = descriptor.getCredential().split("/");

    assertThat(credentialParts[0]).isEqualTo("accessKey");
    assertThat(credentialParts[2]).isEqualTo("us-east-1");
    assertThat(credentialParts[3]).isEqualTo("s3");
    assertThat(credentialParts[4]).isEqualTo("aws4_request");

    assertThat(descriptor.getDate()).isNotBlank();
    assertThat(descriptor.getPolicy()).isNotBlank();
    assertThat(descriptor.getSignature()).isNotBlank();

    assertThat(new String(Base64.decode(descriptor.getPolicy()))).contains("[\"content-length-range\", 1, 104857600]");
  }

  @Test
  public void testV2FormDisabled() {
    Response response = resources.getJerseyTest()
        .target("/v2/attachments/form/upload")
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.DISABLED_NUMBER, AuthHelper.DISABLED_PASSWORD))
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void testUnacceleratedPut() {
    AttachmentDescriptorV1 descriptor = resources.getJerseyTest()
        .target("/v1/attachments/")
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER_TWO, AuthHelper.VALID_PASSWORD_TWO))
        .get(AttachmentDescriptorV1.class);

    assertThat(descriptor.getLocation()).startsWith("https://minio.example.com");
    assertThat(descriptor.getId()).isGreaterThan(0);
    assertThat(descriptor.getIdString()).isNotBlank();
  }

  @Test
  public void testUnacceleratedGet() throws MalformedURLException {
    AttachmentUri uri = resources.getJerseyTest()
        .target("/v1/attachments/1234")
        .request()
        .header("Authorization", AuthHelper.getAuthHeader(AuthHelper.VALID_NUMBER_TWO, AuthHelper.VALID_PASSWORD_TWO))
        .get(AttachmentUri.class);

    assertThat(uri.getLocation().getHost()).isEqualTo("minio.example.com");
  }
}
