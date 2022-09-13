/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 * <p>
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 * <p>
 * or (per the licensee's choosing)
 * <p>
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package chapters.layouts;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import chapters.layouts.TraceIdThrowableProxyConverter;

public class ConverterSample {

    static public void main(String[] args) throws Exception {
        // code();
        xml("traceIdConverter.xml");

        // xml("mySampleConverterConfig.xml");
    }

    private static void xml(String filename) {
        Logger rootLogger = (Logger) LoggerFactory.getLogger("root");
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            lc.reset();
            configurator.setContext(lc);
            configurator.doConfigure(SampleLogging.class.getResource("/chapters/layouts/" + filename).getPath());
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

        print(rootLogger);
    }

    private static void code() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger("root");
        LoggerContext loggerContext = rootLogger.getLoggerContext();
        loggerContext.reset();

        // 异常日志打印时，带上 jar 包
        loggerContext.setPackagingDataEnabled(true);

        Map<String, String> ruleRegistry = (Map<String, String>) loggerContext
                .getObject(CoreConstants.PATTERN_RULE_REGISTRY);
        if (ruleRegistry == null) {
            ruleRegistry = new HashMap<String, String>();
            loggerContext.putObject(CoreConstants.PATTERN_RULE_REGISTRY, ruleRegistry);
        }
        // 可以 xml 配置
        ruleRegistry.put("tEx", TraceIdThrowableProxyConverter.class.getName());

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("[%X{traceId}] %-5level [%thread]: %message%n%tEx");
        encoder.start();

        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        appender.start();

        rootLogger.addAppender(appender);

        print(rootLogger);
    }

    private static void print(Logger rootLogger) {
        MDC.put("traceId", System.nanoTime() + "");

        rootLogger.debug("Message 1");
        rootLogger.warn("Message 2");
        rootLogger.error("Message 3", new Exception("display error stacktrace more than one line",
                new RuntimeException("cause exception 1", new RuntimeException("cause exception 2"))));
        rootLogger.debug("Message 4");
    }
}