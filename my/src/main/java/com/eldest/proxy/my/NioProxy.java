package com.eldest.proxy.my;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.eldest.proxy.support.SimpleLogger;

public class NioProxy implements Runnable {
	private static final SimpleLogger log = new SimpleLogger(NioProxy.class);

	private int localPort;
	private String remoteHost;
	private int remotePort;

	// The channel on which we'll accept connections
	private ServerSocketChannel serverChannel;

	// The selector we'll be monitoring
	private Selector selector;

	private ExecutorService executorService = Executors.newCachedThreadPool();

	// ------------------------ c ------------------------

	public NioProxy(int localPort, String remoteHost, int remotePort) {
		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;

		try {
			selector = initSelector();
		} catch (Exception e) {
			log.error("Cought exception, %s", e);
		}
	}

	// ------------------------ main ------------------------

	public static void main(String[] args) {
		new Thread(new NioProxy(8080, "www.odnoklassniki.ru", 80)).start();
	}

	// ------------------------ run ------------------------

	public void run() {
		while (true) {
			try {
				// Wait for an event one of the registered channels
				selector.select();

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

	private void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating we'd like to be notified when there's data waiting to be read
		// socketChannel.register(selector, SelectionKey.OP_READ);

		log.debug("Accepted %s", socketChannel.socket());

		// Create a non-blocking socket channel
		SocketChannel proxyChannel = SocketChannel.open();
		proxyChannel.configureBlocking(false);

		// Kick off connection establishment
		proxyChannel.connect(new InetSocketAddress(remoteHost, remotePort));
		
		log.debug("Connected %s", proxyChannel.socket());

//		executorService.execute(new NioThread(socketChannel, proxyChannel));
//		executorService.execute(new NioThread(proxyChannel, socketChannel));
	}
}