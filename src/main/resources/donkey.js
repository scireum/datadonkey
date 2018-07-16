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
    donkey.importFile(filename, 'UTF-8', {
        row: callback
    });
}

/**
 * Parses an Excel or CSV file.
 *
 * @param filename the name of the file to parse
 * @param encoding the encoding used to write the output file
 * @param callback a handler function called for each row. It must accept two parameters, the line
 * number and the current row as sirius.kernel.commons.Values
 */
function inputFileWithEncoding(filename, encoding, callback) {
    donkey.importFile(filename, encoding, {
        row: callback
    });
}

/**
 * Generates a CSV file as output.
 * <p>
 * UTF-8 will be used as encoding
 *
 * @param filename the name of the file to create
 * @returns {com.scireum.dd.OutputCSV} which supports addRow(...) to output a row and close() to complete the output
 */
function outputCSV(filename) {
    return new com.scireum.dd.OutputCSV(filename, 'UTF-8');
}

/**
 * Generates a CSV file as output.
 *
 * @param filename the name of the file to create
 * @param encoding the encoding used to write the output file
 * @returns {com.scireum.dd.OutputCSV} which supports addRow(...) to output a row and close() to complete the output
 */
function outputCSVWithEncoding(filename, encoding) {
    return new com.scireum.dd.OutputCSV(filename, encoding);
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

/**
 * Loads the given URL and provides a JQuery like query language using JSoup.
 * @param url the url to open
 * @returns {*} the Document which must notably supports "select" to perform a JQuery like expression to find Node(s).
 */
function jsoup(url) {
    return donkey.jsoup(url);
}

/**
 * Determines if the given string is non-null and not empty.
 * @param str the string to check
 * @returns true if the string is non-null and not empty, false otherwise
 */
function isFilled(str) {
    return donkey.isFilled(str);
}

/**
 * URL encodes the given string.
 * @param str the string to encode
 * @returns the URL encoded string
 */
function urlEncode(str) {
    return donkey.urlEncode(str);
}
