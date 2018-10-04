package com.scireum.dd;

import com.scireum.App;
import sirius.kernel.health.Exceptions;
import sirius.kernel.xml.Attribute;
import sirius.kernel.xml.XMLStructuredOutput;

import java.io.FileOutputStream;

/**
 * Creates and holds an {@link XMLStructuredOutput} for writing XML in JavaScript.
 */
public class OutputXML {

    private XMLStructuredOutput out;

    /**
     * Creates an {@link XMLStructuredOutput} for the given parameters.
     *
     * @param fileName      the fileName of the output file
     * @param doctypeSystem the SYSTEM of the doctype
     */
    public OutputXML(String fileName, String doctypeSystem) {
        App.LOG.INFO("Generating xml output: %s (%s)", fileName, "UTF-8");
        try {
            out = new XMLStructuredOutput(new FileOutputStream(fileName), doctypeSystem);
        } catch (Exception e) {
            throw Exceptions.handle(e);
        }

    }

    /**
     * @return the {@link XMLStructuredOutput} to write XML.
     */
    public XMLStructuredOutput xml() {
        return out;
    }

    /**
     * Bridge method to create an {@link Attribute}.
     *
     * @param name  attribute name
     * @param value attribute value
     * @return A {@link Attribute} created for the provided values.
     */
    public Attribute attr(String name, Object value) {
        return Attribute.set(name, value);
    }
}
