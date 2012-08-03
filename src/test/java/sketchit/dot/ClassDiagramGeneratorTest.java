package sketchit.dot;

import static java.util.Arrays.asList;
import static sketchit.domain.Relationship.Decoration.Composition;
import static sketchit.domain.Relationship.Decoration.None;
import static sketchit.domain.Relationship.LineStyle.Dashed;
import static sketchit.domain.Relationship.LineStyle.Solid;

import sketchit.domain.ClassElement;
import sketchit.domain.Id;
import sketchit.domain.NoteElement;
import sketchit.domain.Relationship;
import sketchit.domain.Repository;
import sketchit.transformer.SVGTransformer;
import sketchit.util.ProcessPipeline;
import sketchit.util.Xml;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 *
 */
public class ClassDiagramGeneratorTest {

    private ClassDiagramGenerator generator;
    private Repository repository;
    //
    private ClassElement customerClassElement;
    private ClassElement orderClassElement;
    private NoteElement noteElement;

    @Before
    public void setUp () {
        repository = new Repository();
        customerClassElement = new ClassElement("Customer", asList("Firstname", "Lastname"), asList("copy()"));
        orderClassElement = new ClassElement("Order", asList("+price", "+quantity"), Collections.<String>emptyList());
        noteElement = new NoteElement("An aggregate root!");
    }

    @Before
    public void initXmlStuffs () throws ParserConfigurationException, IOException, SAXException {
    }

    @Test
    public void generate_oneClass()
            throws IOException, InterruptedException, ParserConfigurationException,
                   SAXException, TransformerException, XPathExpressionException {
        Id idCust = repository.addOrComplete(customerClassElement);
        Id idOrdr = repository.addOrComplete(orderClassElement);
        Id idNote = repository.addOrComplete(noteElement);
        Relationship cust2Ordr = new Relationship(idCust, idOrdr).usingLineStyle(Solid);
        cust2Ordr.leftEndPoint().usingLabel("").usingDecoration(Composition);
        cust2Ordr.rightEndPoint().usingLabel("0..*").usingDecoration(None);
        repository.add(cust2Ordr);
        repository.add(new Relationship(idCust, idNote).usingLineStyle(Dashed));
        generator = new ClassDiagramGenerator(repository);

        ByteArrayOutputStream dotOut = new ByteArrayOutputStream(1024);

        String ext = "svg";
        // svg generation
        new ProcessPipeline().invoke(
                Arrays.asList("/usr/local/bin/dot","-T"+ext),
                generator,
                dotOut
        );

        byte[] resultBytes = dotOut.toByteArray();
        if(ext.equals("svg")) {
            ByteArrayOutputStream transformOut = new ByteArrayOutputStream();

            SVGTransformer transformer = new SVGTransformer();
            transformer.transform(new ByteArrayInputStream(resultBytes), transformOut);

            //
            resultBytes = transformOut.toByteArray();
            System.out.println(new String(resultBytes, "utf-8"));
        }

        FileOutputStream fout = new FileOutputStream("/Users/arnauld/tmp/classDiag." + ext);
        fout.write(resultBytes);
        fout.close();
    }
}
