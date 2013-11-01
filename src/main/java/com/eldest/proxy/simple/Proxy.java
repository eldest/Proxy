package com.eldest.proxy.simple;

import com.eldest.proxy.support.SimpleLogger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Proxy implements Runnable {
	private static final SimpleLogger log = new SimpleLogger(Proxy.class);

	private int localPort;
	private String remoteHost;
	private int remotePort;

	private Socket incoming, outgoing = null;
	private ServerSocket server = null;

	private ProxyThread thread1, thread2;

	private final static String CONDITION_1 = "http://";
	private final static String CONDITION_2 = "/";

	// ------------------------ c ------------------------

	public Proxy(String localport, String remotehost, String remoteport) {
		this(Integer.valueOf(localport), remotehost, Integer.valueOf(remoteport));
	}

	public Proxy(int localPort, String remoteHost, int remotePort) {
		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}

	// ------------------------ f ------------------------

	public int getLocalport() {
		return localPort;
	}

	public String getRemotehost() {
		return remoteHost;
	}

	public int getRemoteport() {
		return remotePort;
	}

	// ------------------------ r ------------------------

	@Override
	public void run() {
		// Check for valid local and remote port, hostname not null
		log.debug("Checking Proxy: port '%s' to '%s' port '%s'", localPort, remoteHost, remotePort);

		// Input data validation
		if (localPort <= 0) {
			throw new ProxyException("Error: invalid Local Port specification");
		}
		if (remotePort <= 0) {
			throw new ProxyException("Error: invalid Remote Port specification");
		}
		if (remoteHost == null) {
			throw new ProxyException("Error: invalid Remote Host specification");
		}
		if (remoteHost.startsWith(CONDITION_1)) {
			remoteHost = remoteHost.substring(CONDITION_1.length());
		}
		if (remoteHost.endsWith(CONDITION_2)) {
			remoteHost = remoteHost.substring(0, remoteHost.length() - CONDITION_2.length());
		}

		// log.debug("Proxy is ready: port '%s' to '%s' port '%s'", localPort, remoteHost, remotePort);

		log.debug("Starting Proxy: port '%s' to '%s' port '%s'", localPort, remoteHost, remotePort);

		
		// Test and create a listening socket at proxy
		try {
			server = new ServerSocket(localPort);
		} catch (IOException e) {
			throw new ProxyException("Error: Couldn't create proxy server", e);
		}

		// Loop to listen for incoming connection, and accept if there is one
		while (true) {
			// Create the 2 threads for the incoming and outgoing traffic of proxy server
			try {
				incoming = server.accept();
				outgoing = new Socket(remoteHost, remotePort);

				thread1 = new ProxyThread(incoming, outgoing);
				thread1.start();

				thread2 = new ProxyThread(outgoing, incoming);
				thread2.start();

			} catch (UnknownHostException e) {
				throw new ProxyException("Error: Unknown Host " + remoteHost, e);
			} catch (IOException e) {
				throw new ProxyException("Error: Couldn't initiate I/O connection for " + remoteHost, e);
			}
		}
	}

}
