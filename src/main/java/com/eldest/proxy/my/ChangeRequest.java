package com.eldest.proxy.my;

import java.nio.channels.SocketChannel;

public class ChangeRequest {

	public static enum ChangeRequestType {
		REGISTER, CHANGEOPS
	}

//	public static enum OperationSetBit {
//		OP_READ(SelectionKey.OP_READ),
//		OP_WRITE(SelectionKey.OP_WRITE),
//		OP_CONNECT(SelectionKey.OP_CONNECT),
//		OP_ACCEPT(SelectionKey.OP_ACCEPT);
//
//		// ------------------------ i ------------------------
//
//		int ops;
//
//		private OperationSetBit(int ops) {
//			this.ops = ops;
//		}
//	}

	// ------------------------ p ------------------------

	public static final int REGISTER = 1;
	public static final int CHANGEOPS = 2;

	public SocketChannel socket;
	public ChangeRequestType type;
	public int ops;

	// ------------------------ c ------------------------

	public ChangeRequest(SocketChannel socket, ChangeRequestType type, int ops) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
	}
}