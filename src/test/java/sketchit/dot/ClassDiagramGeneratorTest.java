package sketchit.dot;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import sketchit.TestSettings;
import sketchit.domain.Id;
import sketchit.domain.klazz.ClassElement;
import sketchit.domain.klazz.NoteElement;
import sketchit.domain.klazz.Relationship;
import sketchit.domain.klazz.Repository;
import sketchit.transformer.SVGTransformer;
import sketchit.util.ProcessPipeline;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static sketchit.domain.Styles.Decoration.Composition;
import static sketchit.domain.Styles.Decoration.None;
import static sketchit.domain.Styles.LineStyle.Dashed;
import static sketchit.domain.Styles.LineStyle.Solid;

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
    public void setUp() {
        repository = new Repository();
        customerClassElement = new ClassElement("Customer", asList("Firstname", "Lastname"), asList("copy()"));
        orderClassElement = new ClassElement("Order", asList("+price", "+quantity"), Collections.<String>emptyList());
        noteElement = new NoteElement("An aggregate root!");
    }

    @Before
    public void initXmlStuffs() throws ParserConfigurationException, IOException, SAXException {
    }

    @Test
    public void generate_oneClass__as_text()
            throws IOException, InterruptedException, ParserConfigurationException,
            SAXException, TransformerException, XPathExpressionException {
        initGenerator();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        generator.writeTo(bout);
        assertThat(bout.toString()).isEqualTo("" +
                "digraph G {\n" +
                "    ranksep = 0.75\n" +
                "    rankdir = TD\n" +
                "    node [\n" +
                "           shape = \"record\"\n" +
                "          height = 0.50\n" +
                "        fontsize = 12\n" +
                "          margin = \"0.20,0.05\"\n" +
                "    ];\n" +
                "    N1 [\n" +
                "            label = \"{Customer\\n|Firstname\\nLastname\\n|copy()}\"\n" +
                "            style = \"filled\"\n" +
                "        fillcolor = \"lightgrey\"\n" +
                "         fontname = \"jd\"\n" +
                "    ];\n" +
                "    node [\n" +
                "           shape = \"record\"\n" +
                "          height = 0.50\n" +
                "        fontsize = 12\n" +
                "          margin = \"0.20,0.05\"\n" +
                "    ];\n" +
                "    N2 [\n" +
                "            label = \"{Order\\n|+price\\n+quantity}\"\n" +
                "            style = \"filled\"\n" +
                "        fillcolor = \"lightgrey\"\n" +
                "         fontname = \"jd\"\n" +
                "    ];\n" +
                "    node [\n" +
                "           shape = \"note\"\n" +
                "          height = 0.50\n" +
                "        fontsize = 10\n" +
                "          margin = \"0.20,0.05\"\n" +
                "      constraint = false\n" +
                "    ];\n" +
                "    N3 [\n" +
                "              label = \"An aggregate root!\"\n" +
                "              style = \"filled\"\n" +
                "          fillcolor = \"cornsilk\"\n" +
                "           fontname = \"jd\"\n" +
                "    ];\n" +
                "    edge [\n" +
                "               shape = \"edge\"\n" +
                "                 dir = \"both\"\n" +
                "               style = \"solid\"\n" +
                "           arrowtail = \"diamond\"\n" +
                "           arrowhead = \"none\"\n" +
                "           taillabel = \"\"\n" +
                "           headlabel = \"0..*\"\n" +
                "       labeldistance = 2\n" +
                "            fontsize = 8\n" +
                "            fontname = \"jd\"\n" +
                "    ];\n" +
                "    N1 -> N2\n" +
                "    edge [\n" +
                "               shape = \"edge\"\n" +
                "                 dir = \"both\"\n" +
                "               style = \"dashed\"\n" +
                "           arrowtail = \"none\"\n" +
                "           arrowhead = \"none\"\n" +
                "           taillabel = \"\"\n" +
                "           headlabel = \"\"\n" +
                "       labeldistance = 2\n" +
                "            fontsize = 8\n" +
                "            fontname = \"jd\"\n" +
                "    ];\n" +
                "    N1 -> N3\n" +
                "}\n");
    }

    @Test
    public void generate_oneClass()
            throws IOException, InterruptedException, ParserConfigurationException,
            SAXException, TransformerException, XPathExpressionException {
        initGenerator();

        ByteArrayOutputStream dotOut = new ByteArrayOutputStream(1024);

        String ext = "svg";
        // svg generation
        new ProcessPipeline().invoke(
                Arrays.asList("/usr/local/bin/dot", "-T" + ext),
                generator,
                dotOut
        );

        byte[] resultBytes = dotOut.toByteArray();
        if (ext.equals("svg")) {
            ByteArrayOutputStream transformOut = new ByteArrayOutputStream();

            SVGTransformer transformer = new SVGTransformer();
            transformer.transform(new ByteArrayInputStream(resultBytes), transformOut);

            //
            resultBytes = transformOut.toByteArray();
            System.out.println(new String(resultBytes, "utf-8"));
        }

        FileOutputStream fout = new FileOutputStream(new File(outputDir(getClass().getSimpleName()), "classDiag." + ext));
        fout.write(resultBytes);
        fout.close();
    }

    private File outputDir(String subDir) {
        File outputDir = new File(new TestSettings().buildDir(), subDir);
        outputDir.mkdirs();
        return outputDir;
    }

    private void initGenerator() {
        Id idCust = repository.addOrComplete(customerClassElement);
        Id idOrdr = repository.addOrComplete(orderClassElement);
        Id idNote = repository.addOrComplete(noteElement);
        Relationship cust2Ordr = new Relationship(idCust, idOrdr).usingLineStyle(Solid);
        cust2Ordr.leftEndPoint().usingLabel("").usingDecoration(Composition);
        cust2Ordr.rightEndPoint().usingLabel("0..*").usingDecoration(None);
        repository.add(cust2Ordr);
        repository.add(new Relationship(idCust, idNote).usingLineStyle(Dashed));
        generator = new ClassDiagramGenerator(repository);
    }
}
