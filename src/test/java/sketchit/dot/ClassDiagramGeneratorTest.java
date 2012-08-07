package sketchit.dot;

import static java.util.Arrays.asList;
import static sketchit.domain.Styles.Decoration.Composition;
import static sketchit.domain.Styles.Decoration.None;
import static sketchit.domain.Styles.LineStyle.Dashed;
import static sketchit.domain.Styles.LineStyle.Solid;

import sketchit.domain.klazz.ClassElement;
import sketchit.domain.Id;
import sketchit.domain.klazz.NoteElement;
import sketchit.domain.klazz.Relationship;
import sketchit.domain.klazz.Repository;
import sketchit.transformer.SVGTransformer;
import sketchit.util.ProcessPipeline;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

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
