package com.eldest.proxy.simple.simple;

import com.eldest.proxy.support.SimpleLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ProxyManager {
	private static final SimpleLogger log = new SimpleLogger(ProxyManager.class);
	Map<String, Proxy> proxyList = new HashMap<String, Proxy>();
	Map<Proxy, Thread> threadList = new HashMap<Proxy, Thread>();

	final static String KEY_LOCAL_PORT = "localPort";
	final static String KEY_REMOTE_HOST = "remoteHost";
	final static String KEY_REMOTE_PORT = "remotePort";

	// ------------------------ c ------------------------

	// ------------------------ l ------------------------

	public Proxy getProxy(String name) {
		return proxyList.get(name);
	}

	public void addProxy(String name, Proxy proxy) {
		proxyList.put(name, proxy);
		Thread thread = new Thread(proxy);
		threadList.put(proxy, thread);
		thread.start();
	}

	public void removeProxy(String name) {
		Proxy proxy = proxyList.remove(name);
		Thread thread = threadList.remove(proxy);
		thread.interrupt();
	}

	// ------------------------ f ------------------------

	public void configure(String fileName) {

		String localPort, remoteHost, remotePort;

		// Read properties file.
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(fileName));
		} catch (IOException e) {
			throw new ProxyException("Cought IOExceptio", e);
		}

		for (String key : properties.stringPropertyNames()) {
			List<String> list = new ArrayList<String>();

			if (key.contains(KEY_LOCAL_PORT)) {
				list.add(key.split("\\p{Punct}")[0]);
			}

			for (String name : list) {
				localPort = properties.getProperty(name + "." + KEY_LOCAL_PORT);
				remoteHost = properties.getProperty(name + "." + KEY_REMOTE_HOST);
				remotePort = properties.getProperty(name + "." + KEY_REMOTE_PORT);

				if (localPort != null && remoteHost != null && remotePort != null) {
					Proxy proxy = new Proxy(localPort, remoteHost, remotePort);
					addProxy(name, proxy);
					log.debug("Added Proxy with next parameters: localPort '%s', remoteHost '%s', remotePort '%s'",
                            localPort, remoteHost, remotePort);
				}
			}
		}
	}

}
