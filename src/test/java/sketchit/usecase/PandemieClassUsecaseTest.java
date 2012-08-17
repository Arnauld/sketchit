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
public class PandemieClassUsecaseTest {

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
            writer.write("<h2>" + o[2] + "(" + o[1] + ")</h2>");
            writer.write("  <pre>\n");
            writer.write(escapeHtml4(resourceContent(resourcePath)));
            writer.write("  </pre>\n");
            writer.write("  <table border=\"0\">\n");
            writer.write("    <tr>\n");
            writer.write("      <td>\n");
            writer.write("        <img src=\"" + resourcePath + "-" + o[1]  + ".png\" />\n");
            writer.write("      </td>\n");
//            writer.write("      <td>\n");
//            writer.write("        <img src=\"" + resourcePath + "-" + o[1]  + ".svg\" />\n");
//            writer.write("      </td>\n");
            writer.write("    </tr>");
            writer.write("  </table>");
        }
        writer.write("</body>\n");
        writer.write("</html>\n");
        writer.close();
    }

    @Parameterized.Parameters
    public static List<Object[]> values () {
        return Arrays.<Object[]>asList(
                o("first-overview-001", "LR", "001")
                ,o("first-overview-001a", "LR", "001a")
                ,o("first-overview-001b", "LR", "001b")
                ,o("first-overview-001c", "LR", "001c")
                ,o("first-overview-002b-cards", "TD", "002b")
                ,o("first-overview-003-actions", "TD", "003")
                ,o("first-overview-003b-actions", "LR", "003b")
                ,o("first-overview-003c-actions", "TD", "003c")
                ,o("first-overview-003d-actions", "TD", "003d")
                ,o("first-overview-003e-actions", "LR", "003e")
                ,o("first-overview-003f-actions", "TD", "003f")
                ,o("first-overview-003g-actions", "LR", "003g")
                ,o("first-overview-004-actions", "TD", "004")
                ,o("first-overview-004b-actions", "TD", "004b")
                ,o("first-overview-010-services", "LR", "010")
        );
    }

    private YumlParser parser;
    private RepositoryYumlParserHandlerAdapter yumlParserHandler;
    private ClassDiagramGenerator classDiagramGenerator;
    private final String direction;
    private final String description;
    private final String resourcePath;

    public PandemieClassUsecaseTest(String resourcePath, String direction, String description) {
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
            SVGTransformer transformer = new SVGTransformer();
            transformer.transform(new ByteArrayInputStream(dotOut.toByteArray()), transformOut);
        }
        finally {
            IOUtils.closeQuietly(transformOut);
        }

        File pngFile = new File(getOutputDir(), resourcePath + "-" + direction + ".png");
        new SVGConverter()
                .usingKeyPixelUnitToMillimeterFactor(0.85f)
                .svg2png(svgFile, pngFile, false);
    }

    private static File getOutputDir() {
        File file = new File(properties.getProperty("usecase.outputdir") + "/pandemie");
        if (!file.mkdirs() && !file.exists()) {
            throw new RuntimeException("Fail to create output directory at " + file);
        }
        return file;
    }

    private static InputStream openResourceStream(String resourcePath) {
        return PandemieClassUsecaseTest.class.getResourceAsStream(resourcePath);
    }

    private static String resourceContent(String resourcePath) throws IOException {
        String completePath = "/usecases/pandemie/" + resourcePath + ".yuml";
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
