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
import sirius.kernel.commons.Values;
import sirius.kernel.commons.Watch;
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
import java.util.Map;

/**
 * Responsible for loading and executing the JavaScript.
 * <p>
 * Also provides helper function visible in the scripting environment. These are called by the functions
 * defined in <tt>donkey.js</tt>
 */
public class App {

    private static ScriptEngine engine;
    private static final Log LOG = Log.get("donkey");

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

    /*
     * Bridge method used by donkey.js
     */
    public void log(Object o) {
        LOG.INFO(o);
    }

    /*
     * Bridge method used by donkey.js
     */
    public void parseXML(String file, Map<String, Object> handlers) throws IOException {
        XMLReader r = new XMLReader();
        for (final Map.Entry<String, Object> pair : handlers.entrySet()) {
            r.addHandler(pair.getKey(), (node) -> {
                try {
                    ((Invocable) engine).invokeMethod(pair.getValue(), "process", node);
                } catch (ScriptException | NoSuchMethodException e) {
                    Exceptions.handle(e);
                }
            });
        }
        r.parse(new FileInputStream(file));
    }

    /*
     * Bridge method used by donkey.js
     */
    public void importFile(String file, Object callback) {
        try {
            try (FileInputStream in = new FileInputStream(file)) {
                LineBasedProcessor.create(file, in).run((int lineNumber, Values row) -> {
                    try {
                        ((Invocable) engine).invokeMethod(callback, "row", lineNumber, row);
                    } catch (ScriptException | NoSuchMethodException e) {
                        Exceptions.handle(e);
                    }
                });
            }
        } catch (Throwable t) {
            Exceptions.handle(LOG, t);
        }
    }
}
