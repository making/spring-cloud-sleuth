/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sleuth.brave.instrument.web.client;

import java.util.function.Supplier;

import brave.Span;
import brave.propagation.B3Propagation;
import brave.propagation.Propagation;
import brave.sampler.Sampler;
import org.assertj.core.api.Assertions;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.sleuth.api.TraceContext;
import org.springframework.cloud.sleuth.brave.BraveTestSpanHandler;
import org.springframework.cloud.sleuth.brave.bridge.BraveTraceContext;
import org.springframework.cloud.sleuth.test.ReportedSpan;
import org.springframework.cloud.sleuth.test.TestSpanHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = { ReactorNettyHttpClientSpringBootTests.Config.class,
		org.springframework.cloud.sleuth.instrument.web.client.ReactorNettyHttpClientSpringBootTests.TestConfiguration.class },
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ReactorNettyHttpClientSpringBootTests
		extends org.springframework.cloud.sleuth.instrument.web.client.ReactorNettyHttpClientSpringBootTests {

	@Override
	public TraceContext traceContext() {
		return BraveTraceContext
				.fromBrave(brave.propagation.TraceContext.newBuilder().traceId(1).spanId(2).sampled(true).build());
	}

	@Override
	public void assertSingleB3Header(String b3SingleHeaderReadByServer, ReportedSpan clientSpan, TraceContext parent) {
		Assertions.assertThat(b3SingleHeaderReadByServer)
				.isEqualTo(parent.traceIdString() + "-" + clientSpan.id() + "-1-" + parent.spanIdString());
	}

	@Configuration(proxyBeanMethods = false)
	static class Config {

		@Bean
		Propagation.Factory propagationFactory() {
			return B3Propagation.newFactoryBuilder().injectFormat(B3Propagation.Format.SINGLE)
					.injectFormat(Span.Kind.CLIENT, B3Propagation.Format.SINGLE)
					.injectFormat(Span.Kind.SERVER, B3Propagation.Format.SINGLE).build();
		}

		@Bean
		TestSpanHandler testSpanHandlerSupplier(brave.test.IntegrationTestSpanHandler testSpanHandler) {
			return new BraveTestSpanHandler(testSpanHandler);
		}

		@Bean
		Sampler alwaysSampler() {
			return Sampler.ALWAYS_SAMPLE;
		}

		@Bean
		brave.test.IntegrationTestSpanHandler braveTestSpanHandler() {
			return new brave.test.IntegrationTestSpanHandler();
		}

	}

}
