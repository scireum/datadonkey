/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package com.scireum;

import com.google.common.collect.Maps;
import com.scireum.dd.LineBasedProcessor;
import org.apache.log4j.Level;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import sirius.kernel.Setup;
import sirius.kernel.Sirius;
import sirius.kernel.commons.Explain;
import sirius.kernel.commons.RateLimit;
import sirius.kernel.commons.Strings;
import sirius.kernel.commons.Values;
import sirius.kernel.commons.Wait;
import sirius.kernel.commons.Watch;
import sirius.kernel.health.Average;
import sirius.kernel.health.Exceptions;
import sirius.kernel.health.HandledException;
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
import java.net.URL;
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

    /**
     * Contains the logger used by the applicatiobn layer.
     */
    public static final Log LOG = Log.get("donkey");

    private static final String LINE =
            "-------------------------------------------------------------------------------";
    private static ScriptEngine engine;
    private static final RateLimit reportLimit = RateLimit.timeInterval(5, TimeUnit.SECONDS);

    public static void main(String[] args) {
        try {
            initialize();
            verifyCommandLine(args);
            createScriptingEngine();
            evalDonkeyLibrary();
            loadAndExecuteScript(args[0]);
        } catch (Exception t) {
            Exceptions.handle(t);
        } finally {
            Sirius.stop();
        }
    }

    private static void loadAndExecuteScript(String arg) throws ScriptException, FileNotFoundException {
        engine.put(ScriptEngine.FILENAME, arg);
        LOG.INFO("Executing '%s'...", arg);
        LOG.INFO(LINE);
        Watch w = Watch.start();
        engine.eval(new FileReader(arg));
        LOG.INFO(LINE);
        LOG.INFO("Execution completed: %s", w.duration());
    }

    private static void evalDonkeyLibrary() throws ScriptException {
        LOG.INFO("Loading 'donkey.js'...");
        LOG.INFO(LINE);
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
        LOG.INFO(LINE);
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
    @SuppressWarnings("squid:S1845")
    @Explain("Name is fixed as it is used in JavaScript")
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
        LOG.INFO(LINE);
        Average avg = new Average();
        Watch w = Watch.start();
        try {
            XMLReader r = new XMLReader();
            for (final Map.Entry<String, Object> pair : handlers.entrySet()) {
                r.addHandler(pair.getKey(), node -> {
                    try {
                        ((Invocable) engine).invokeMethod(pair.getValue(), "process", node);
                        avg.addValue(w.elapsed(TimeUnit.MICROSECONDS, true));
                        if (reportLimit.check()) {
                            LOG.INFO("Read %d elements. Avg. duration per element: %1.2f ms",
                                     avg.getCount(),
                                     avg.getAvg() / 1000d);
                        }
                    } catch (ScriptException | NoSuchMethodException e) {
                        Exceptions.handle(e);
                    }
                });
            }
            r.parse(new FileInputStream(file));
            LOG.INFO("Read %d elements. Avgerage duration per element: %1.2f ms", avg.getCount(), avg.getAvg() / 1000d);
            LOG.INFO("Completed reading: %s", file);
            LOG.INFO(LINE);
        } catch (HandledException t) {
            LOG.SEVERE(t);
        } catch (Exception t) {
            Exceptions.handle(LOG, t);
        }
    }

    /*
     * Bridge method used by donkey.js
     */
    public void importFile(String file, String charset, Object callback) {
        LOG.INFO("Reading: %s", file);
        LOG.INFO(LINE);
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
        } catch (HandledException t) {
            LOG.SEVERE(t);
        } catch (Exception t) {
            Exceptions.handle(LOG, t);
        }
    }

    /**
     * Used to rate-limit the requests made against a host to a certain amount (roughly every 1,5s).
     * <p>
     * That shouldn't cause any trouble in the target system when scraping or indexing a site.
     */
    private Map<String, Long> lastInteraction = Maps.newConcurrentMap();

    /*
     * Bridge method used by donkey.js
     */
    public Document jsoup(String url) {
        try {
            filterHostBlocker();

            URL u = new URL(url);
            long li = lastInteraction.getOrDefault(u.getHost(), 0L);
            long delta = System.currentTimeMillis() - li;
            if (delta < 1000) {
                Wait.randomMillis(1000, 2000);
            }
            lastInteraction.put(u.getHost(), System.currentTimeMillis());

            return Jsoup.connect(url)
                        .header("Connection", "close")
                        .userAgent("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)")
                        .followRedirects(true)
                        .get();
        } catch (IOException e) {
            Exceptions.handle(LOG, e);
            return null;
        }
    }

    private void filterHostBlocker() {
        long limit = System.currentTimeMillis() - 5000;
        lastInteraction.entrySet()
                       .stream()
                       .filter(e -> e.getValue().longValue() < limit)
                       .forEach(e -> lastInteraction.remove(e.getKey()));
    }

    /*
     * Bridge method used by donkey.js
     */
    public boolean isFilled(Object o) {
        return Strings.isFilled(o);
    }

    /*
     * Bridge method used by donkey.js
     */
    public String urlEncode(String o) {
        return Strings.urlEncode(o);
    }
}
