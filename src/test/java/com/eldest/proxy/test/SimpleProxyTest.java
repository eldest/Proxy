package com.eldest.proxy.test;

import org.junit.Test;

import com.eldest.proxy.simple.Proxy;
import com.eldest.proxy.simple.ProxyManager;
import com.eldest.proxy.support.SimpleLogger;
import com.eldest.proxy.support.TestBase;

public class SimpleProxyTest extends TestBase {
	private static final SimpleLogger log = new SimpleLogger(SimpleProxyTest.class);

	@Test
	public void someTest() throws Exception {
		Proxy proxy = new Proxy(8080, "http://www.odnoklassniki.ru/", 80);
		new Thread(proxy).start();
		Thread.sleep(10000);
		log.debug("Done");
	}

	@Test
	public void anotherSomeTest() throws Exception {
		ProxyManager manager = new ProxyManager();
		manager.configure(getResource("proxy.properties"));
		Thread.sleep(10000);
		manager.removeProxy("web");
		log.debug("Step2");
		Thread.sleep(10000);
		log.debug("Done");
	}

}
