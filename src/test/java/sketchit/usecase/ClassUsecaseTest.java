package sketchit.usecase;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.fest.assertions.api.Assertions.assertThat;

import sketchit.domain.klazz.Repository;
import sketchit.dot.ClassDiagramGenerator;
import sketchit.testutil.LabeledParameterized;
import sketchit.transformer.SVGConverter;
import sketchit.transformer.SVGTransformer;
import sketchit.util.ProcessPipeline;
import sketchit.yuml.RepositoryYumlParserHandlerAdapter;
import sketchit.yuml.YumlParser;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
@RunWith(LabeledParameterized.class)
public class ClassUsecaseTest {

    private static Properties properties;

    @BeforeClass
    public static void loadProperties() throws IOException {
        initializeProperties();
        generateUsecaseHtml(values());
    }

    private static void generateUsecaseHtml(List<Object[]> values) throws IOException {
        File htmlOut = new File(getOutputDir(), "index.html");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(htmlOut, false), "utf-8");
        writer.write("<html>\n");
        writer.write("<head>\n");
        writer.write("  <meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n");
        writer.write("</head>\n");
        writer.write("<body>\n");
        for(Object[] o : values()) {
            String resourcePath = (String)o[0];
            writer.write("<h2>" + o[2] + "</h2>");
            writer.write("  <pre>\n");
            writer.write(escapeHtml4(resourceContent(resourcePath)));
            writer.write("  </pre>\n");
            writer.write("  <table border=\"0\">\n");
            writer.write("    <tr>\n");
            writer.write("      <td>\n");
            writer.write("        <img src=\"" + resourcePath + "-" + o[1]  + ".png\" />\n");
            writer.write("      </td>\n");
            writer.write("      <td>\n");
            writer.write("        <img src=\"" + resourcePath + "-" + o[1]  + ".svg\" />\n");
            writer.write("      </td>\n");
            writer.write("    </tr>");
            writer.write("  </table>");
        }
        writer.write("</body>\n");
        writer.write("</html>\n");
        writer.close();
    }

    @Parameterized.Parameters
    public static List<Object[]> values () {
        return Arrays.asList(
                o("case00", "TD", "Class"),
                o("case00a1", "TD", "Note & Background in hexa"),
                o("case00a2", "TD", "Note & Background using html named color"),
                o("case00b", "LR", "Association"),
                o("case00d", "LR", "Dashed"),
                o("case00e", "LR", "Dotted"),
                o("case00f", "LR", "Bold"),
                o("case00g", "LR", "Directional"),
                o("case00h", "LR", "Aggregation"),
                o("case00i", "LR", "Composition"),
                o("case00j", "TD", "Inheritance"),
                o("case01", "TD", "Long note text"),
                o("case02a", "TD", "Class with attributes"),
                o("case02", "TD", "Class with Details (TD)"),
                o("case02", "DT", "Class with Details (DT)"),
                o("case02", "LR", "Class with Details (LR)"),
                o("case02", "RL", "Class with Details (RL)"),
                o("case03", "TD", "Stereotype"),
                o("case03b", "TD", "Stereotypes"),
                o("case04", "LR", "Dependencies (Stereotype dans la relation)"),
                o("case05", "LR", "InterfaceInheritance"),
                o("case06", "LR", "Inheritance"),
                o("case07", "LR", "Notes"),
                o("case09", "LR", "Aggregation"),
                o("case10", "LR", "utf8 & backgroung"),
                o("case11", "LR", "DirectionalAssociation"),
                o("case12", "LR", "Cardinality"),
                o("case13", "LR", "SimpleAssociation"),
                o("case14", "LR", "SimpleCase"),
                o("case20", "TD", "SomethingMeaty"),
                o("case21a", "TD", "Maven aggregator"),
                o("case21b", "TD", "Maven parent as aggregator")
        );
    }

    private YumlParser parser;
    private RepositoryYumlParserHandlerAdapter yumlParserHandler;
    private ClassDiagramGenerator classDiagramGenerator;
    private final String direction;
    private final String description;
    private final String resourcePath;

    public ClassUsecaseTest(String resourcePath, String direction, String description) {
        this.resourcePath = resourcePath;
        this.direction = direction;
        this.description = description;
    }

    @Before
    public void setUp() {
        parser = new YumlParser();
        Repository repository = new Repository();
        yumlParserHandler = new RepositoryYumlParserHandlerAdapter(repository);
        classDiagramGenerator = new ClassDiagramGenerator(repository).usingRankDir(direction);
    }

    @Test
    public void processResource() throws Exception {
        String expressions = resourceContent(resourcePath);
        process(resourcePath, expressions);
    }

    private void process(String resourcePath, String yuml) throws Exception
    {
        classDiagramGenerator.usingFontName("jd");

        parser.parseExpressions(yuml, yumlParserHandler);

        ByteArrayOutputStream dotOut = new ByteArrayOutputStream(1024);

        String ext = "svg";
        // svg generation
        new ProcessPipeline().invoke(
                asList("/usr/local/bin/dot", "-T" + ext),
                classDiagramGenerator,
                dotOut
        );

        File svgFile = new File(getOutputDir(), resourcePath + "-" + direction + "." + ext);
        OutputStream transformOut = new FileOutputStream(svgFile);
        try {
            System.out.println("ClassUsecaseTest.process\n " + new String(dotOut.toByteArray(), "utf8"));
            SVGTransformer transformer = new SVGTransformer();
            transformer.transform(new ByteArrayInputStream(dotOut.toByteArray()), transformOut);
        }
        finally {
            IOUtils.closeQuietly(transformOut);
        }

        boolean useInkscape = false;
        File pngFile = new File(getOutputDir(), resourcePath + "-" + direction + ".png");
        new SVGConverter()
                .usingInkscapePath("/Applications/Inkscape.app/Contents/Resources/bin/inkscape")
                .svg2png(svgFile, pngFile, !useInkscape);
    }

    private static File getOutputDir() {
        File file = new File(properties.getProperty("usecase.outputdir") + "/class");
        if (!file.mkdirs() && !file.exists()) {
            throw new RuntimeException("Fail to create output directory at " + file);
        }
        return file;
    }

    private static InputStream openResourceStream(String resourcePath) {
        return ClassUsecaseTest.class.getResourceAsStream(resourcePath);
    }

    private static String resourceContent(String resourcePath) throws IOException {
        String completePath = "/usecases/class/" + resourcePath + ".yuml";
        InputStream stream = openResourceStream(completePath);
        assertThat(stream).describedAs("Resource at " + completePath).isNotNull();

        try {
            return IOUtils.toString(stream, "utf-8");
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private static Object[] o(Object... values) {
        return values;
    }

    private static void initializeProperties() throws IOException {
        InputStream stream = openResourceStream("/usecases.properties");
        try {
            properties = new Properties();
            properties.load(stream);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
