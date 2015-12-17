/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package com.scireum;

import com.scireum.dd.LineBasedProcessor;
import org.apache.log4j.Level;
import sirius.kernel.Setup;
import sirius.kernel.Sirius;
import sirius.kernel.commons.RateLimit;
import sirius.kernel.commons.Values;
import sirius.kernel.commons.Watch;
import sirius.kernel.health.Average;
import sirius.kernel.health.Exceptions;
import sirius.kernel.health.Log;
import sirius.kernel.info.Product;
import sirius.kernel.xml.XMLReader;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for loading and executing the JavaScript.
 * <p>
 * Also provides helper function visible in the scripting environment. These are called by the functions
 * defined in <tt>donkey.js</tt>
 */
public class App {

    private static ScriptEngine engine;
    public static final Log LOG = Log.get("donkey");
    private static final RateLimit reportLimit = RateLimit.timeInterval(5, TimeUnit.SECONDS);

    public static void main(String[] args) {
        try {
            initialize();
            verifyCommandLine(args);
            createScriptingEngine();
            evalDonkeyLibrary();
            loadAndExecuteScript(args[0]);
        } catch (Throwable t) {
            Exceptions.handle(t);
        } finally {
            Sirius.stop();
        }
    }

    private static void loadAndExecuteScript(String arg) throws ScriptException, FileNotFoundException {
        engine.put(ScriptEngine.FILENAME, arg);
        LOG.INFO("Executing '%s'...", arg);
        LOG.INFO("-------------------------------------------------------------------------------");
        Watch w = Watch.start();
        engine.eval(new FileReader(arg));
        LOG.INFO("-------------------------------------------------------------------------------");
        LOG.INFO("Execution completed: %s", w.duration());
    }

    private static void evalDonkeyLibrary() throws ScriptException {
        LOG.INFO("Loading 'donkey.js'...");
        LOG.INFO("-------------------------------------------------------------------------------");
        engine.put(ScriptEngine.FILENAME, "donkey.js");
        engine.eval(new InputStreamReader(App.class.getResourceAsStream("/donkey.js")));
    }

    private static void createScriptingEngine() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        engine = mgr.getEngineByExtension("js");
        engine.getContext().setAttribute("donkey", new App(), ScriptContext.ENGINE_SCOPE);
    }

    private static void verifyCommandLine(String[] args) {
        LOG.INFO(Product.getProduct().getName());
        LOG.INFO(Product.getProduct().getDetails());
        LOG.INFO("-------------------------------------------------------------------------------");
        if (args.length == 0) {
            LOG.INFO("Supply a JavaScript file as parameter!");
            LOG.INFO("Visit https://github.com/scireum/datadonkey for further information");
            System.exit(-1);
        }
    }

    private static void initialize() {
        Setup setup = new Setup(Setup.Mode.PROD, App.class.getClassLoader());
        setup.withDefaultLogLevel(Level.WARN).withConsoleLogFormat("%m%n").withLogToConsole(true).withLogToFile(false);
        Sirius.start(setup);
    }

    /**
     * Writes a log message to the output
     *
     * @param o the object / message to log
     */
    public void log(Object o) {
        LOG.INFO(o);
    }

    /**
     * Can be used to report the current state or progress.
     * <p>
     * Writes the the output just like {@link #log(Object)} would but is rate limited to
     * log at most every five seconds.
     *
     * @param o the object / message to log
     */
    public void report(Object o) {
        if (reportLimit.check()) {
            log(o);
        }
    }

    /*
     * Bridge method used by donkey.js
     */
    public void parseXML(String file, Map<String, Object> handlers) throws IOException {
        LOG.INFO("Reading: %s", file);
        LOG.INFO("-------------------------------------------------------------------------------");
        Average avg = new Average();
        Watch w = Watch.start();
        try {
            XMLReader r = new XMLReader();
            for (final Map.Entry<String, Object> pair : handlers.entrySet()) {
                r.addHandler(pair.getKey(), (node) -> {
                    try {
                        ((Invocable) engine).invokeMethod(pair.getValue(), "process", node);
                        avg.addValue(w.elapsed(TimeUnit.MICROSECONDS, true));
                        if (reportLimit.check()) {
                            LOG.INFO("Read %d elements. Avg. duration per element: %1.2d ms",
                                     avg.getCount(),
                                     avg.getAvg() / 1000d);
                        }
                    } catch (ScriptException | NoSuchMethodException e) {
                        Exceptions.handle(e);
                    }
                });
            }
            r.parse(new FileInputStream(file));
            LOG.INFO("Read %d elements. Avgerage duration per element: %1.2d ms", avg.getCount(), avg.getAvg() / 1000d);
            LOG.INFO("Completed reading: %s", file);
            LOG.INFO("-------------------------------------------------------------------------------");
        } catch (Throwable t) {
            Exceptions.handle(LOG, t);
        }
    }

    /*
     * Bridge method used by donkey.js
     */
    public void importFile(String file, String charset, Object callback) {
        LOG.INFO("Reading: %s", file);
        LOG.INFO("-------------------------------------------------------------------------------");
        try {
            Average avg = new Average();
            Watch w = Watch.start();
            try (FileInputStream in = new FileInputStream(file)) {
                LineBasedProcessor.create(file, in, Charset.forName(charset)).run((int lineNumber, Values row) -> {
                    try {
                        ((Invocable) engine).invokeMethod(callback, "row", lineNumber, row);
                        avg.addValue(w.elapsed(TimeUnit.MICROSECONDS, true));
                        if (reportLimit.check()) {
                            LOG.INFO("Read %d lines. Average duration per line: %1.2f ms",
                                     avg.getCount(),
                                     avg.getAvg() / 1000d);
                        }
                    } catch (ScriptException | NoSuchMethodException e) {
                        Exceptions.handle(e);
                    }
                });
            }
            LOG.INFO("Read %d lines. Average duration per line: %1.2f ms", avg.getCount(), avg.getAvg() / 1000d);
            LOG.INFO("Completed reading: %s", file);
            LOG.INFO("-------------------------------------------------------------------------------\n");
        } catch (Throwable t) {
            Exceptions.handle(LOG, t);
        }
    }
}
