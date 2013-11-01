package com.eldest.proxy.support;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class SimpleLogger {

	Logger logger;

	public SimpleLogger(Logger logger) {
		this.logger = logger;
	}

	public SimpleLogger(Class<?> target) {
		this(Logger.getLogger(target));
	}

	public SimpleLogger(String name) {
		this(Logger.getLogger(name));
	}

	// Using stack trace of the current thread to obtain caller class name
	// static final SimpleLogger log = new SimpleLogger();
	public SimpleLogger() {
		// 0 - the getStackTrace call, 1 - this constructor invocation
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		logger = Logger.getLogger(stack[2].getClassName());
	}

	public void debug(String format, Object... params) {
		logger.debug(new SimpleLoggerMessage(format, params));
	}

	public void debug(Object message) {
		logger.debug(message);
	}

	public void error(Throwable t, String format, Object... params) {
		logger.error(new SimpleLoggerMessage(format, params), t);
	}

	public void error(String format, Object... params) {
		logger.error(new SimpleLoggerMessage(format, params));
	}

	public void error(Throwable t, Object message) {
		logger.error(message, t);
	}

	public void error(Object message) {
		logger.error(message);
	}

	public void info(String format, Object... params) {
		logger.info(new SimpleLoggerMessage(format, params));
	}

	public void info(Object message) {
		logger.info(message);
	}

	public void warn(String format, Object... params) {
		logger.warn(new SimpleLoggerMessage(format, params));
	}

	public void warn(Object message) {
		logger.warn(message);
	}

	public void fatal(String format, Object... params) {
		logger.fatal(new SimpleLoggerMessage(format, params));
	}

	public void fatal(Throwable t, String format, Object... params) {
		logger.fatal(new SimpleLoggerMessage(format, params), t);
	}

	public void fatal(Throwable t, Object message) {
		logger.fatal(message, t);
	}

	public void fatal(Object message) {
		logger.fatal(message);
	}

	public void log(Level level, Object message) {
		logger.log(level, message);
	}

	public void log(Level level, String format, Object... params) {
		logger.log(level, new SimpleLoggerMessage(format, params));
	}

	public String getName() {
		return logger.getName();
	}
}
