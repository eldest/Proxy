package com.eldest.proxy.support;

import java.util.List;

public class SimpleLoggerMessage {
	String format;

	Object[] args;

	Object message;

	List<?> attachments;

	public SimpleLoggerMessage(Object message) {
		this(null, message);
	}

	public SimpleLoggerMessage(List<?> attachments, Object message) {
		this.attachments = attachments;
		this.message = message;
	}

	public SimpleLoggerMessage(String format, Object[] args) {
		this(null, format, args);
	}

	public SimpleLoggerMessage(List<?> attachments, String format, Object[] args) {
		this.attachments = attachments;
		this.format = format;
		this.args = args;
	}

	boolean hasAttachments() {
		return attachments != null && attachments.size() > 0;
	}

	void appendAttachments(StringBuilder sb) {
		for (Object att : attachments)
			sb.append("[").append(att).append("] ");
	}

	String format() {
		StringBuilder sb = new StringBuilder();
		if (hasAttachments())
			appendAttachments(sb);
		sb.append(String.format(format, args));
		return sb.toString();
	}

	public String toString() {
		return (message != null ? message : (message = format())).toString();
	}
}
