/*
 * Original software: Copyright 2013-2020 Signal Messenger, LLC
 * Modified software: Copyright 2019-2022 Anton Alipov, sole trader
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package su.sres.shadowserver.util;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import static com.codahale.metrics.MetricRegistry.name;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

public class CircuitBreakerUtil {

  public static void registerMetrics(MetricRegistry metricRegistry, CircuitBreaker circuitBreaker, Class<?> clazz) {
    Meter successMeter     = metricRegistry.meter(name(clazz, circuitBreaker.getName(), "success"    ));
    Meter failureMeter     = metricRegistry.meter(name(clazz, circuitBreaker.getName(), "failure"    ));
    Meter unpermittedMeter = metricRegistry.meter(name(clazz, circuitBreaker.getName(), "unpermitted"));

    metricRegistry.gauge(name(clazz, circuitBreaker.getName(), "state"), () -> ()-> circuitBreaker.getState().getOrder());

    circuitBreaker.getEventPublisher().onSuccess(event -> successMeter.mark());
    circuitBreaker.getEventPublisher().onError(event -> failureMeter.mark());
    circuitBreaker.getEventPublisher().onCallNotPermitted(event -> unpermittedMeter.mark());
  }
  
  public static void registerMetrics(MetricRegistry metricRegistry, Retry retry, Class<?> clazz) {
	    Meter successMeter      = metricRegistry.meter(name(clazz, retry.getName(), "success"      ));
	    Meter retryMeter        = metricRegistry.meter(name(clazz, retry.getName(), "retry"        ));
	    Meter errorMeter        = metricRegistry.meter(name(clazz, retry.getName(), "error"        ));
	    Meter ignoredErrorMeter = metricRegistry.meter(name(clazz, retry.getName(), "ignored_error"));

	    retry.getEventPublisher().onSuccess(event -> successMeter.mark());
	    retry.getEventPublisher().onRetry(event -> retryMeter.mark());
	    retry.getEventPublisher().onError(event -> errorMeter.mark());
	    retry.getEventPublisher().onIgnoredError(event -> ignoredErrorMeter.mark());
	  }
}