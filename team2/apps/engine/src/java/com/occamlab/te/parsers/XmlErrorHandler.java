package com.occamlab.te.parsers;

import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * A SAX error handler that collects validation errors raised while verifying
 * the structure and content of XML entities.
 *
 * @author rmartell
 * @version $Rev: 1 $
 */
public class XmlErrorHandler implements ErrorHandler {

    /** Storage for error messages. */
    private StringBuffer buf = new StringBuffer();

    /** Collection of reported validation errors. */
    private List<ValidationError> errors = new ArrayList<ValidationError>();

    /**
     * Indicates whether any validation errors have been reported.
     *
     * @return true if any validation errors have been received.
     */
    public boolean isEmpty() {
        return errors.isEmpty();
    }

    /**
     * Receive notification of a warning.
     *
     * @param spex
     *            a non-error condition reported by the parser
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException spex) {

	//printError("Warning", spex);
        addError(ValidationError.WARNING, spex);
    }

    /**
     * Receive notification of a recoverable error. Typically this indicates
     * that a validation constraint has been violated.
     *
     * @param spex
     *            a non-fatal error condition reported by the parser
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException spex) {

	//printError("Error", spex);
        addError(ValidationError.ERROR, spex);
    }

    /**
    * Prints the error to STDOUT, used to be consistent with TEAM Engine error handler.
    *
    */
    private void printError(String type, SAXParseException e) {

    	PrintWriter logger = new PrintWriter(System.out);
        logger.print(type);
        if (e.getLineNumber() >= 0) {
            logger.print(" at line " + e.getLineNumber());
            if (e.getColumnNumber() >= 0) {
                logger.print(", column " + e.getColumnNumber());
            }
            if (e.getSystemId() != null) {
                logger.print(" of " + e.getSystemId());
            }
        } else {
            if (e.getSystemId() != null) {
                logger.print(" in " + e.getSystemId());
            }
        }
        logger.println(":");
        logger.println("  " + e.getMessage());
        logger.flush();
    }

    /**
     * Receive notification of a non-recoverable error, such as a violation of
     * the well-formedness constraint.
     *
     * @param spex
     *            a fatal error condition reported by the parser
     * @throws SAXException
     *             if a fatal error (e.g., non-XML input, or ill-formed XML)
     *             occurs while parsing the input
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException spex) throws SAXException {

        addError(ValidationError.FATAL_ERROR, spex);
        throw new SAXException("Fatal error while parsing input.");
    }

    /**
     * Adds a validation error based on a <code>SAXParseException</code>.
     *
     * @param severity
     *            the severity of the error
     * @param spex
     *            the <code>SAXParseException</code> raised while validating
     *            the XML source
     */
    private void addError(short severity, SAXParseException spex) {

        buf.append("Line " + spex.getLineNumber() + " - ");
        buf.append(spex.getMessage()+"\n");
        ValidationError error = new ValidationError(severity, buf.toString());
        errors.add(error);
        buf.setLength(0);
    }

    /**
     * Returns a concatenation of all received error messages.
     *
     * @return a consolidated error message
     */
    public String toString() {

        buf.setLength(0);
        ErrorIterator errIterator = iterator();
        while (errIterator.hasNext()) {
            ValidationError err = errIterator.next();
            buf.append(err.getMessage());
        }
        return buf.toString();
    }

    /**
     * Returns a list of errors as strings.
     *
     * @return a list of error strings.
     */
    public List<String> toList() {

        List<String> errorStrings = new ArrayList<String>();
        ErrorIterator errIterator = iterator();
        while (errIterator.hasNext()) {
            ValidationError err = errIterator.next();
            errorStrings.add(err.getMessage());
        }
        return errorStrings;
    }

    /**
     * Returns the errors in a simple nodelist (needed for CTL processing).
     *
     * @return a list of errors in a NodeList.
     */
    public NodeList toNodeList() {

	Document doc = null;
	try {
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
		  "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.newDocument();
	} catch (Exception e) {
		e.printStackTrace();
	}

	Element root = doc.createElement("errors");
	doc.appendChild(root);
        ErrorIterator errIterator = iterator();
        while (errIterator.hasNext()) {
            ValidationError err = errIterator.next();
            Element elem = doc.createElement("error");
            elem.setTextContent(err.getMessage());
            root.appendChild(elem);
        }

        return (NodeList) doc.getElementsByTagName("error");

    }

    /**
     * Returns an iterator over the validation errors collected by this handler.
     *
     * @return a read-only <code>ErrorIterator</code> for this handler
     */
    public ErrorIterator iterator() {
        return new ErrorIterator();
    }

    /**
     * Clears all errors and messages.
     */
    public void reset() {
        buf.setLength(0);
        errors.clear();
    }

    /**
     * Helper class that provides a read-only iterator over validation errors.
     *
     * @author rmartell
     * @version $Rev: 1 $
     */
    public class ErrorIterator {
        /** The underlying errors for this iterator. */
        Iterator underlying = errors.iterator();

        /**
         * Indicates if more errors remain in the iteration.
         *
         * @return <code>true</code> if more errors remain.
         */
        public boolean hasNext() {
            return underlying.hasNext();
        }

        /**
         * Returns the next validation error in the iteration.
         *
         * @return the next error
         */
        public ValidationError next() {
            return (ValidationError) underlying.next();
        }
    }
}