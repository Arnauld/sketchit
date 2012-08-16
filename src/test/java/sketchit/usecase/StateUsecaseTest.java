package sketchit.usecase;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.fest.assertions.api.Assertions.assertThat;

import sketchit.domain.state.Repository;
import sketchit.dot.StateDiagramGenerator;
import sketchit.parser.RepositoryStateDiagramParserHandlerAdapter;
import sketchit.parser.StateDiagramParser;
import sketchit.testutil.LabeledParameterized;
import sketchit.transformer.SVGConverter;
import sketchit.transformer.SVGTransformer;
import sketchit.util.ProcessPipeline;

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
public class StateUsecaseTest {

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
                o("case04", "TD", "Basic"),
                o("case04", "LR", "Basic")
        );
    }

    private StateDiagramParser parser;
    private RepositoryStateDiagramParserHandlerAdapter stateParserHandler;
    private StateDiagramGenerator stateDiagramGenerator;
    private final String direction;
    private final String description;
    private final String resourcePath;

    public StateUsecaseTest(String resourcePath, String direction, String description) {
        this.resourcePath = resourcePath;
        this.direction = direction;
        this.description = description;
    }

    @Before
    public void setUp() {
        parser = new StateDiagramParser();
        Repository repository = new Repository();
        stateParserHandler = new RepositoryStateDiagramParserHandlerAdapter(repository);
        stateDiagramGenerator = new StateDiagramGenerator(repository).usingRankDir(direction);
    }

    @Test
    public void processResource() throws Exception {
        String expressions = resourceContent(resourcePath);
        process(resourcePath, expressions);
    }

    private void process(String resourcePath, String yuml) throws Exception
    {
        stateDiagramGenerator.usingFontName("jd");

        parser.parseExpressions(yuml, stateParserHandler);

        ByteArrayOutputStream dotOut = new ByteArrayOutputStream(1024);

        String ext = "svg";
        // svg generation
        new ProcessPipeline().invoke(
                asList("/usr/local/bin/dot", "-T" + ext),
                stateDiagramGenerator,
                dotOut
        );

        File svgFile = new File(getOutputDir(), resourcePath + "-" + direction + "." + ext);
        OutputStream transformOut = new FileOutputStream(svgFile);
        try {
            System.out.println("StateUsecaseTest.process\n " + new String(dotOut.toByteArray(), "utf8"));
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
        File file = new File(properties.getProperty("usecase.outputdir") + "/state");
        if (!file.mkdirs() && !file.exists()) {
            throw new RuntimeException("Fail to create output directory at " + file);
        }
        return file;
    }

    private static InputStream openResourceStream(String resourcePath) {
        return StateUsecaseTest.class.getResourceAsStream(resourcePath);
    }

    private static String resourceContent(String resourcePath) throws IOException {
        String completePath = "/usecases/state/" + resourcePath + ".state";
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
