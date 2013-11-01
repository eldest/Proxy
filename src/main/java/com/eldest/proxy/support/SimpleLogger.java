package com.eldest.proxy.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleLogger {

    protected Logger logger;

    public SimpleLogger(Logger logger) {
        this.logger = logger;
    }

    public SimpleLogger(Class<?> target) {
        this(LoggerFactory.getLogger(target));
    }

    public SimpleLogger(String name) {
        this(LoggerFactory.getLogger(name));
    }

    public void debug(String format, Object... params) {
        logger.debug(String.format(format, params));
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void error(Throwable t, String format, Object... params) {
        logger.error(String.format(format, params), t);
    }

    public void error(String format, Object... params) {
        logger.error(String.format(format, params));
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(Throwable e) {
        logger.error("Error:", e);
    }

    public void error(String msg, Throwable e) {
        logger.error(msg, e);
    }

    public void info(String format, Object... params) {
        logger.info(String.format(format, params));
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String format, Object... params) {
        logger.warn(String.format(format, params));
    }

    public void warn(String message) {
        logger.warn(message);
    }

    public void action(String message) {
        logger.warn(message);
    }
}
