package com.eldest.proxy.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.eldest.proxy.support.SimpleLogger;

public class ProxyThread extends Thread {
	private static final SimpleLogger log = new SimpleLogger(ProxyThread.class);

	private Socket incoming, outgoing;

	// ------------------------ c ------------------------

	ProxyThread(Socket incoming, Socket outgoing) {
		this.incoming = incoming;
		this.outgoing = outgoing;
	}

	// ------------------------ r ------------------------

	public void run() {
		byte[] buffer = new byte[60];
		int numberRead = 0;
		OutputStream toClient;
		InputStream fromClient;

		try {
			toClient = outgoing.getOutputStream();
			fromClient = incoming.getInputStream();
			while (true) {
				numberRead = fromClient.read(buffer, 0, 50);
				log.debug("read: %s from %s to %s", numberRead, incoming, outgoing);

				if (numberRead == -1) {
					incoming.close();
					outgoing.close();
				}

				toClient.write(buffer, 0, numberRead);

			}
		} catch (IOException e) {
			log.error("Cought IOException", e);
		}

		log.debug("ProxyThread '%s' is stopped", this);
	}

}
