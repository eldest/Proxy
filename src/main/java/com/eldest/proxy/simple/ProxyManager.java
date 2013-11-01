package com.eldest.proxy.simple;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.eldest.proxy.support.SimpleLogger;

public class ProxyManager implements Runnable {
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

		String localport, remotehost, remoteport;

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
				localport = properties.getProperty(name + "." + KEY_LOCAL_PORT);
				remotehost = properties.getProperty(name + "." + KEY_REMOTE_HOST);
				remoteport = properties.getProperty(name + "." + KEY_REMOTE_PORT);

				if (localport == null || remotehost == null || remoteport == null) {
					continue;
				} else {
					Proxy proxy = new Proxy(localport, remotehost, remoteport);
					addProxy(name, proxy);
					log.debug("Added Proxy with next parametrs: localport '%s', remotehost '%s', remoteport '%s'", localport, remotehost, remoteport);
				}
			}
		}

//		sleep();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

//	private void sleep() {
//		while (active) {
//			try {
//				Thread.sleep(delay);
//			} catch (InterruptedException e) {
//				active = false;
//			}
//		}
//	}

}
