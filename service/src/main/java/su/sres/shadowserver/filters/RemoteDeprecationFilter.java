/*
 * Copyright 2020 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package su.sres.shadowserver.filters;

import static com.codahale.metrics.MetricRegistry.name;

import com.vdurmont.semver4j.Semver;
import io.micrometer.core.instrument.Metrics;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import su.sres.shadowserver.configuration.dynamic.DynamicRemoteDeprecationConfiguration;
import su.sres.shadowserver.util.ua.ClientPlatform;
import su.sres.shadowserver.util.ua.UnrecognizedUserAgentException;
import su.sres.shadowserver.util.ua.UserAgent;
import su.sres.shadowserver.util.ua.UserAgentUtil;

/**
 * The remote deprecation filter rejects traffic from clients older than a configured minimum
 * version. It may optionally also reject traffic from clients with unrecognized User-Agent strings.
 * If a client platform does not have a configured minimum version, all traffic from that client
 * platform is allowed.
 */
public class RemoteDeprecationFilter implements Filter {

  private final DynamicRemoteDeprecationConfiguration dynamicRemoteDeprecationConfiguration;

  private static final String DEPRECATED_CLIENT_COUNTER_NAME = name(RemoteDeprecationFilter.class, "deprecated");
  private static final String PENDING_DEPRECATION_COUNTER_NAME = name(RemoteDeprecationFilter.class, "pendingDeprecation");
  private static final String PLATFORM_TAG = "platform";
  private static final String REASON_TAG_NAME = "reason";
  private static final String EXPIRED_CLIENT_REASON = "expired";
  private static final String BLOCKED_CLIENT_REASON = "blocked";
  private static final String UNRECOGNIZED_UA_REASON = "unrecognized_user_agent";

  public RemoteDeprecationFilter(DynamicRemoteDeprecationConfiguration dynamicRemoteDeprecationConfiguration) {
    this.dynamicRemoteDeprecationConfiguration = dynamicRemoteDeprecationConfiguration;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    
    final Map<ClientPlatform, Semver> minimumVersionsByPlatform = dynamicRemoteDeprecationConfiguration.getMinimumVersions();
    final Map<ClientPlatform, Semver> versionsPendingDeprecationByPlatform = dynamicRemoteDeprecationConfiguration.getVersionsPendingDeprecation();
    final Map<ClientPlatform, Set<Semver>> blockedVersionsByPlatform = dynamicRemoteDeprecationConfiguration.getBlockedVersions();
    final Map<ClientPlatform, Set<Semver>> versionsPendingBlockByPlatform = dynamicRemoteDeprecationConfiguration.getVersionsPendingBlock();
    final boolean allowUnrecognizedUserAgents = dynamicRemoteDeprecationConfiguration.isUnrecognizedUserAgentAllowed();

    boolean shouldBlock = false;

    try {
      final String userAgentString = ((HttpServletRequest) request).getHeader("User-Agent");
      final UserAgent userAgent = UserAgentUtil.parseUserAgentString(userAgentString);

      if (blockedVersionsByPlatform.containsKey(userAgent.getPlatform())) {
        if (blockedVersionsByPlatform.get(userAgent.getPlatform()).contains(userAgent.getVersion())) {
          recordDeprecation(userAgent, BLOCKED_CLIENT_REASON);
          shouldBlock = true;
        }
      }

      if (minimumVersionsByPlatform.containsKey(userAgent.getPlatform())) {
        if (userAgent.getVersion().isLowerThan(minimumVersionsByPlatform.get(userAgent.getPlatform()))) {
          recordDeprecation(userAgent, EXPIRED_CLIENT_REASON);
          shouldBlock = true;
        }
      }

      if (versionsPendingBlockByPlatform.containsKey(userAgent.getPlatform())) {
        if (versionsPendingBlockByPlatform.get(userAgent.getPlatform()).contains(userAgent.getVersion())) {
          recordPendingDeprecation(userAgent, BLOCKED_CLIENT_REASON);
        }
      }

      if (versionsPendingDeprecationByPlatform.containsKey(userAgent.getPlatform())) {
        if (userAgent.getVersion().isLowerThan(versionsPendingDeprecationByPlatform.get(userAgent.getPlatform()))) {
          recordPendingDeprecation(userAgent, EXPIRED_CLIENT_REASON);
        }
      }
    } catch (final UnrecognizedUserAgentException e) {
      if (!allowUnrecognizedUserAgents) {
        recordDeprecation(null, UNRECOGNIZED_UA_REASON);
        shouldBlock = true;
      }
    }

    if (shouldBlock) {
      ((HttpServletResponse) response).sendError(499);
    } else {
      chain.doFilter(request, response);
    }
  }

  private void recordDeprecation(final UserAgent userAgent, final String reason) {
    Metrics.counter(DEPRECATED_CLIENT_COUNTER_NAME,
        PLATFORM_TAG, userAgent != null ? userAgent.getPlatform().name().toLowerCase() : "unrecognized",
        REASON_TAG_NAME, reason).increment();
  }

  private void recordPendingDeprecation(final UserAgent userAgent, final String reason) {
    Metrics.counter(PENDING_DEPRECATION_COUNTER_NAME,
        PLATFORM_TAG, userAgent.getPlatform().name().toLowerCase(),
        REASON_TAG_NAME, reason).increment();
  }
}
