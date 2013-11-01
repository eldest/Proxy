package com.eldest.proxy.test.support;

import java.io.File;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.BeforeClass;

import com.eldest.proxy.support.SimpleLogger;

public class TestBase {
	protected static final SimpleLogger log = new SimpleLogger(TestBase.class);
	protected static final String resources = new File("./src/test/resources/").getAbsolutePath();

	@BeforeClass
	public static void prepare() {
		//	configure log4j
		//	BasicConfigurator.configure();
		DOMConfigurator.configure("./src/main/resources/log4j.xml");
	}
}
