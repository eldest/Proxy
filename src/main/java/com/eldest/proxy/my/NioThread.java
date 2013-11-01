package com.eldest.proxy.my;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.eldest.proxy.nio.ChangeRequest;
import com.eldest.proxy.support.SimpleLogger;

public class NioThread implements Runnable {
	private static final SimpleLogger log = new SimpleLogger(NioThread.class);

	private Selector selector;
	private SocketChannel incoming, outgoing;

	// A list of PendingChange instances
	private List<ChangeRequest> pendingChanges = new LinkedList<ChangeRequest>();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();

	// ------------------------ c ------------------------

	public NioThread(SocketChannel incoming, SocketChannel outgoing) {
		this.incoming = incoming;
		this.outgoing = outgoing;

		try {
			selector = Selector.open();
			incoming.register(selector, SelectionKey.OP_READ);
			outgoing.register(selector, SelectionKey.OP_WRITE);
		} catch (IOException e) {
			log.error("Cought exception, %s", e);
		}
	}

	// ------------------------ run ------------------------

	@Override
	public void run() {
		while (true) {
			try {
				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator<?> selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isConnectable()) {
						this.finishConnection(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ------------------------ f ------------------------

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		log.debug("Reading %s", socketChannel.socket());

		// Make a buffer so it's ready for new data
		ByteBuffer readBuffer = ByteBuffer.allocate(8192);

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}

		// Handle the response
		processData(socketChannel, readBuffer, numRead);

		log.debug("Readed %s", socketChannel.socket());
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		log.debug("Writing data from %s", socketChannel.socket());

		synchronized (pendingData) {
			List<ByteBuffer> queue = pendingData.get(socketChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}

		log.debug("Writed data from %s", socketChannel.socket());
	}

	private void finishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			// Cancel the channel's registration with our selector
			System.out.println(e);
			key.cancel();
			return;
		}

		// Register an interest in writing on this channel
		key.interestOps(SelectionKey.OP_WRITE);
	}

	private void processData(SocketChannel socketChannel, ByteBuffer data, int count) {
		log.debug("Processing data from %s to %s, size: %s", socketChannel.socket(), outgoing.socket(), count);

		synchronized (pendingChanges) {
			// And queue the data we want written
			synchronized (pendingData) {
				List<ByteBuffer> queue = pendingData.get(outgoing);
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					pendingData.put(outgoing, queue);
				}
				queue.add(data);
			}
		}

		// Finally, wake up our selecting thread so it can make the required changes
		selector.wakeup();
	}
}
