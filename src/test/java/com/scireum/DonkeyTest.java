/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package com.scireum;

import org.junit.Test;

public class DonkeyTest {
    @Test
    public void testDonkey() {
        App.main(new String[]{"demo/example.js"});
    }

    @Test
    public void testXMLOutput() {
        App.main(new String[]{"demo/xml-output/xml-example.js"});
    }
}
