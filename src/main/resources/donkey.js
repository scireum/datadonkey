/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

/**
 * Writes a log message to stdout.
 *
 * @param msg the message or object to log
 */
function log(msg) {
    donkey.log(msg);
}

/**
 * Parses the given XML file
 * <p>
 * The given map consists of XPATH expressions as keys and callback functions as values.
 * The functions have a single parameter of type sirius.kernel.xml.StructuredNode.
 * </p>
 *
 * @param filename the name of the file to parse
 * @param parseMap a map which contains XPATH expressions as keys and callback functions as values
 */
function parseXML(filename, parseMap) {
    var map = new java.util.TreeMap();
    for (var prop in parseMap) {
        if (parseMap.hasOwnProperty(prop)) {
            map.put(prop, {
                process: parseMap[prop]
            });
        }
    }

    donkey.parseXML(filename, map);
}

/**
 * Parses an Excel or CSV file.
 *
 * @param filename the name of the file to parse
 * @param callback a handler function called for each row. It must accept two parameters, the line
 * number and the current row as sirius.kernel.commons.Values
 */
function inputFile(filename, callback) {
    donkey.importFile(filename, {
        row: callback
    });
}

/**
 * Generates a CSV file as output.
 *
 * @param filename the name of the file to create
 * @returns {com.scireum.dd.OutputCSV} which supports addRow(...) to output a row and close() to complete the output
 */
function outputCSV(filename) {
    return new com.scireum.dd.OutputCSV(filename);
}

/**
 * Generates an Excel file as output.
 *
 * @returns {com.scireum.dd.OutputExcel} which supports addRow(..) to ouput a row and save(filename) to create and
 * save the resulting Excel file
 */
function outputExcel() {
    return new com.scireum.dd.OutputExcel();
}
