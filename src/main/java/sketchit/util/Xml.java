package sketchit.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Various utilities around XML manipulation.
 *
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Xml {

    private ErrorHandler errorHandler;
    private EntityResolver entityResolver;
    //
    private DocumentBuilder documentBuilder;
    private XPathFactory xPathFactory;
    private TransformerFactory transformerFactory;

    public Xml () {
    }

    public void copyAttributes(Element fromElement, Element toElement) {
        NamedNodeMap attributes = fromElement.getAttributes();
        for(int i=0; i<attributes.getLength(); i++) {
            Node item = attributes.item(i);
            toElement.setAttribute(item.getNodeName(), item.getNodeValue());
        }
    }

    public Document newDocument  () throws ParserConfigurationException {
        return getDocumentBuilder().newDocument();
    }

    public Document newDocument  (InputStream inputStream)
            throws ParserConfigurationException, IOException, SAXException
    {
        return getDocumentBuilder().parse(inputStream);
    }

    public void serializeUTF8(Document doc, Writer writer) throws TransformerException {
        serialize(doc, writer, createPrettyPrintUTF8Transfromer());
    }

    public void serializeUTF8(Document doc, OutputStream os) throws TransformerException {
        serialize(doc, os, createPrettyPrintUTF8Transfromer());
    }

    public void serialize(Document doc, Writer writer, Transformer transformer) throws TransformerException {
        Source source = new DOMSource(doc);
        Result result = new StreamResult(writer);

        transformer.transform(source, result);
    }

    public void serialize(Document doc, OutputStream out, Transformer transformer) throws TransformerException {
        Source source = new DOMSource(doc);
        Result result = new StreamResult(out);

        transformer.transform(source, result);
    }

    public XPath newXPath() {
        return getXPathFactory().newXPath();
    }

    private XPathFactory getXPathFactory() {
        if(xPathFactory==null) {
            xPathFactory = XPathFactory.newInstance();
        }
        return xPathFactory;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if(documentBuilder==null) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setErrorHandler(getErrorHandler());
            documentBuilder.setEntityResolver(getEntityResolver());
        }
        return documentBuilder;
    }

    public ErrorHandler getErrorHandler() {
        if(errorHandler==null)
            errorHandler = createErrorHandler();
        return errorHandler;
    }

    public EntityResolver getEntityResolver () {
        if(entityResolver==null)
            entityResolver = createEntityResolver();
        return entityResolver;
    }

    protected EntityResolver createEntityResolver() {
        return new DefaultEntityResolver();
    }

    public String toString(Document document) throws TransformerException {
        StringWriter writer = new StringWriter();
        serializeUTF8(document, writer);
        return writer.toString();
    }

    public Transformer createPrettyPrintUTF8Transfromer() throws TransformerConfigurationException {
        return createPrettyPrintTransfromer("utf-8");
    }

    /**
    * Create a transformer for a pretty print result
    * <ul>
    *  <li>version</li>
    *  <li>indentation</li>
    *  <li>...</li>
    * </ul>
    * @param encoding
    * @return
    * @throws TransformerConfigurationException
    */
    public Transformer createPrettyPrintTransfromer(String encoding) throws TransformerConfigurationException {
        Transformer transformer = newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        return transformer;
    }

    private Transformer newTransformer() throws TransformerConfigurationException {
        if(transformerFactory==null)
            transformerFactory = TransformerFactory.newInstance();
        return transformerFactory.newTransformer();
    }

    protected ErrorHandler createErrorHandler() {
        return new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                // TODO logger
                System.err.println("WARN: " + exception.getLocalizedMessage());
                exception.printStackTrace();
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                // TODO logger
                System.err.println("ERROR: " + exception.getLocalizedMessage());
                exception.printStackTrace();
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                // TODO logger
                System.err.println("FATAL: " + exception.getLocalizedMessage());
                exception.printStackTrace();
            }
        };
    }

    public XPathExpression compileXPath(String expression) throws XPathExpressionException {
        return newXPath().compile(expression);
    }

    public static class DefaultEntityResolver implements EntityResolver {
        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            int indexOf = systemId.lastIndexOf('/');
            String resourceName = systemId.substring(indexOf+1);
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(
                    "svg_xsd/" + resourceName);
            if(resourceAsStream!=null) {
                return new InputSource(resourceAsStream);
            }
            else {
                // TODO logger
                System.out.println("WARN: Unknown entity: " + publicId + ", " + systemId + "");
                return null;
            }
        }
    }

    public static Element e(Document document, String tagName, A...attributes) {
        Element element = document.createElement(tagName);
        for(A a : attributes) {
            element.setAttribute(a.name, a.value);
        }
        return element;
    }

    public static A a(String name, String value) {
        return new A(name, value);
    }

    public final static class A {
        public final String name;
        public final String value;

        private A(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }


}
