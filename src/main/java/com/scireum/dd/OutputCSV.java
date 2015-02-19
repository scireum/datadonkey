/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package com.scireum.dd;

import au.com.bytecode.opencsv.CSVWriter;
import sirius.kernel.health.Exceptions;
import sirius.kernel.nls.NLS;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Used to write a CSV file from JavaScript
 * <p>
 * Used by donkey.js
 *
 * @author Andreas Haufler (aha@scireum.de)
 */
public class OutputCSV {

    /*
     * Bridge method used by donkey.js
     */
    public OutputCSV(String filename) throws IOException {
        writer = new CSVWriter(new FileWriter(filename), ';', '"');
    }

    protected CSVWriter writer;

    /*
     * Bridge method used by donkey.js
     */
    public OutputCSV addRow(Object... row) {
        String[] arr = new String[row.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = NLS.toUserString(row[i]);
        }
        writer.writeNext(arr);

        return this;
    }


    /*
     * Bridge method used by donkey.js
     */
    public void close() {
        try {
            this.writer.close();
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }

}
