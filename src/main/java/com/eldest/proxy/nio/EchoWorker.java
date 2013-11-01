package com.eldest.proxy.nio;

import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.eldest.proxy.support.SimpleLogger;

public class EchoWorker implements Runnable {
	private static final SimpleLogger log = new SimpleLogger(EchoWorker.class);

	private BlockingQueue<ServerDataEvent> queue = new LinkedBlockingQueue<ServerDataEvent>();

	public void processData(NioServer server, SocketChannel socket, byte[] data, int count) {
		log.debug("Procesing data from %s, size: %s", socket.socket(), count);

		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0, count);

		try {
			queue.put(new ServerDataEvent(server, socket, dataCopy));
		} catch (InterruptedException e) {
			log.debug("Interrupted");
		}
	}

	public void run() {
		ServerDataEvent dataEvent;

		while (true) {
			// Wait for data to become available
			try {
				dataEvent = (ServerDataEvent) queue.take();

				// Return to sender
				log.debug("Sending data from %s, size: %s", dataEvent.socket.socket(), dataEvent.data.length);
				dataEvent.server.send(dataEvent.socket, dataEvent.data);

			} catch (InterruptedException e) {
				log.debug("Interrupted");
			}
		}
	}
}