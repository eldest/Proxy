package com.eldest.proxy.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.eldest.proxy.support.SimpleLogger;

public class NioServer implements Runnable {
	private static final SimpleLogger log = new SimpleLogger(NioServer.class);

	private int localPort;

	// The channel on which we'll accept connections
	private ServerSocketChannel serverChannel;

	// The selector we'll be monitoring
	private Selector selector;

	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	private EchoWorker worker;


	// A list of PendingChange instances
	private List<ChangeRequest> pendingChanges = new LinkedList<ChangeRequest>();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();

	// ------------------------ c ------------------------

	public NioServer(int localPort, String remoteHost, int remotePort) throws IOException {
		this.localPort = localPort;

		selector = initSelector();

		worker = new EchoWorker();
		new Thread(worker).start();
	}

	// ------------------------ main ------------------------

	public static void main(String[] args) {
		try {
			new Thread(new NioServer(8080, "http://www.odnoklassniki.ru/", 80)).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ------------------------ run ------------------------

	public void run() {
		while (true) {
			try {
				log.debug("Part 1");

				// Process any pending changes
				synchronized (pendingChanges) {
					for (ChangeRequest change : pendingChanges) {
						switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(selector);
							key.interestOps(change.ops);
						}
					}
					pendingChanges.clear();
				}

				// Wait for an event one of the registered channels
				selector.select();

				log.debug("Part 2");

				// Iterate over the set of keys for which events are available
				Iterator<?> selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isAcceptable()) {
						accept(key);
					} else if (key.isReadable()) {
						read(key);
					} else if (key.isWritable()) {
						write(key);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ------------------------ f ------------------------

	private Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		serverChannel.socket().bind(new InetSocketAddress(localPort));

		// Register the server socket channel, indicating an interest in accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	// ------------------------ accept ------------------------

	private void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		Socket socket = socketChannel.socket();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating we'd like to be notified when there's data waiting to be read
		socketChannel.register(selector, SelectionKey.OP_READ);

		log.debug("Accepted %s", socket);
	}

	// ------------------------ read ------------------------

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(readBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}

		// Hand the data off to our worker thread
		worker.processData(this, socketChannel, readBuffer.array(), numRead);

		log.debug("Readed %s", socketChannel.socket());
	}

	// ------------------------ write ------------------------

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (pendingData) {
			List<ByteBuffer> queue = pendingData.get(socketChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested in writing on this socket. Switch back to waiting for data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}

		log.debug("Writing %s", socketChannel.socket());
	}

	// ------------------------ public ------------------------

	public void send(SocketChannel socket, byte[] data) {
		log.debug("Sending data from %s, size: %s", socket.socket(), data.length);

		synchronized (pendingChanges) {
			// Indicate we want the interest ops set changed
			pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (pendingData) {
				List<ByteBuffer> queue = pendingData.get(socket);
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}

		// Finally, wake up our selecting thread so it can make the required changes
		selector.wakeup();
	}

}