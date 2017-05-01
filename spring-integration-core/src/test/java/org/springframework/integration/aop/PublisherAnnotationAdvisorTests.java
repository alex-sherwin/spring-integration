/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Before;
import org.junit.Test;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.integration.annotation.Publisher;
import org.springframework.integration.channel.QueueChannel;

/**
 * @author Mark Fisher
 * @since 2.0
 */
public class PublisherAnnotationAdvisorTests {

	private final StaticApplicationContext context = new StaticApplicationContext();


	@Before
	public void setup() {
		context.registerSingleton("testChannel", QueueChannel.class);
		context.registerSingleton("testMetaChannel", QueueChannel.class);
	}


	@Test
	public void annotationAtMethodLevelOnVoidReturnWithParamAnnotation() {
		PublisherAnnotationAdvisor advisor = new PublisherAnnotationAdvisor();
		advisor.setBeanFactory(context);
		QueueChannel testChannel = context.getBean("testChannel", QueueChannel.class);
		ProxyFactory pf = new ProxyFactory(new AnnotationAtMethodLevelTestBeanImpl());
		pf.addAdvisor(advisor);
		TestVoidBean proxy = (TestVoidBean) pf.getProxy();
		proxy.testVoidMethod("foo");
		Message<?> message = testChannel.receive(0);
		assertNotNull(message);
		assertEquals("foo", message.getPayload());
	}

	@Test
	public void annotationAtMethodLevel() {
		PublisherAnnotationAdvisor advisor = new PublisherAnnotationAdvisor();
		advisor.setBeanFactory(context);
		QueueChannel testChannel = context.getBean("testChannel", QueueChannel.class);
		ProxyFactory pf = new ProxyFactory(new AnnotationAtMethodLevelTestBeanImpl());
		pf.addAdvisor(advisor);
		TestBean proxy = (TestBean) pf.getProxy();
		proxy.test();
		Message<?> message = testChannel.receive(0);
		assertNotNull(message);
		assertEquals("foo", message.getPayload());
	}

	@Test
	public void annotationAtClassLevel() {
		PublisherAnnotationAdvisor advisor = new PublisherAnnotationAdvisor();
		advisor.setBeanFactory(context);
		QueueChannel testChannel = context.getBean("testChannel", QueueChannel.class);
		ProxyFactory pf = new ProxyFactory(new AnnotationAtClassLevelTestBeanImpl());
		pf.addAdvisor(advisor);
		TestBean proxy = (TestBean) pf.getProxy();
		proxy.test();
		Message<?> message = testChannel.receive(0);
		assertNotNull(message);
		assertEquals("foo", message.getPayload());
	}

	@Test
	public void metaAnnotationAtMethodLevel() {
		PublisherAnnotationAdvisor advisor = new PublisherAnnotationAdvisor();
		advisor.setBeanFactory(context);
		QueueChannel testMetaChannel = context.getBean("testMetaChannel", QueueChannel.class);
		ProxyFactory pf = new ProxyFactory(new MetaAnnotationAtMethodLevelTestBeanImpl());
		pf.addAdvisor(advisor);
		TestBean proxy = (TestBean) pf.getProxy();
		proxy.test();
		Message<?> message = testMetaChannel.receive(0);
		assertNotNull(message);
		assertEquals("foo", message.getPayload());
	}

	@Test
	public void metaAnnotationAtClassLevel() {
		PublisherAnnotationAdvisor advisor = new PublisherAnnotationAdvisor();
		advisor.setBeanFactory(context);
		QueueChannel testMetaChannel = context.getBean("testMetaChannel", QueueChannel.class);
		ProxyFactory pf = new ProxyFactory(new MetaAnnotationAtClassLevelTestBeanImpl());
		pf.addAdvisor(advisor);
		TestBean proxy = (TestBean) pf.getProxy();
		proxy.test();
		Message<?> message = testMetaChannel.receive(0);
		assertNotNull(message);
		assertEquals("foo", message.getPayload());
	}


	interface TestBean {

		String test();

	}


	interface TestVoidBean {

		void testVoidMethod(String s);

	}


	static class AnnotationAtMethodLevelTestBeanImpl implements TestBean, TestVoidBean {

		@Publisher(channel = "testChannel")
		public String test() {
			return "foo";
		}

		@Publisher(channel = "testChannel")
		public void testVoidMethod(@Payload String s) { }
	}


	@Publisher(channel = "testChannel")
	static class AnnotationAtClassLevelTestBeanImpl implements TestBean {

		public String test() {
			return "foo";
		}

	}


	@Target({ElementType.METHOD, ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	@Publisher(channel = "testMetaChannel")
	public @interface TestMetaPublisher {
	}


	static class MetaAnnotationAtMethodLevelTestBeanImpl implements TestBean {

		@TestMetaPublisher
		public String test() {
			return "foo";
		}
	}


	@TestMetaPublisher
	static class MetaAnnotationAtClassLevelTestBeanImpl implements TestBean {

		public String test() {
			return "foo";
		}
	}

}