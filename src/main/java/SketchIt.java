import static java.util.Arrays.asList;

import sketchit.domain.Repository;
import sketchit.dot.ClassDiagramGenerator;
import sketchit.transformer.SVGConverter;
import sketchit.transformer.SVGTransformer;
import sketchit.util.ProcessPipeline;
import sketchit.yuml.RepositoryYumlParserHandlerAdapter;
import sketchit.yuml.YumlParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class SketchIt {

    private PrintStream out;
    private InputStream inputStream;
    private String fontName;
    private String dotExec;
    private String direction;

    public static void main(String[] args) {
        new SketchIt().parseAndExecute(args);
    }

    private Options options = new Options();

    private SketchIt() {
    }

    @SuppressWarnings("AccessStaticViaInstance")
    protected void initOptions(Options options) {
        options.addOption(new Option("h", "help", false, "print this message"));
        options.addOption(new Option("v", "version", false, "print the version information and exit"));
        options.addOption(new Option("c", "class", false, "generate a diagram class"));
        options.addOption(OptionBuilder.withArgName("path")
                .hasArg()
                .withDescription("dot executable path")
                .create("dot"));
        options.addOption(OptionBuilder.withArgName("path")
                .hasArg()
                .withDescription("input file")
                .create("in"));
        options.addOption(OptionBuilder.withArgName("path")
                .hasArg()
                .withDescription("output file")
                .create("out"));
        options.addOption(OptionBuilder.withArgName("fontname")
                .hasArg()
                .withDescription("Font family name (be aware there are issues with name containing '.' or '-'...")
                .create("font"));
        options.addOption(OptionBuilder.withArgName("direction")
                .hasArg()
                .withDescription("Diagram direction: TD, LR...")
                .create("direction"));

    }

    public void parseAndExecute(String[] args) {
        initOptions(options);
        // create the parser
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            parse(line);
        }
        catch (Exception exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            return;
        }


        try {
            execute();
        }
        catch (Exception e) {
            // oops, something went wrong
            System.err.println("Execution failed.  Reason: " + e.getMessage());
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(out);
        }
    }

    private void execute() throws
                           IOException,
                           TransformerException,
                           SAXException,
                           XPathExpressionException,
                           ParserConfigurationException, SVGConverter.ConverterException
    {
        YumlParser parser = new YumlParser();
        Repository repository = new Repository();
        RepositoryYumlParserHandlerAdapter yumlParserHandler = new RepositoryYumlParserHandlerAdapter(repository);
        ClassDiagramGenerator classDiagramGenerator = new ClassDiagramGenerator(repository).usingRankDir(direction);
        classDiagramGenerator.usingFontName(fontName);

        // Let's go!
        String content = IOUtils.toString(inputStream, "utf-8");
        parser.parseExpressions(content, yumlParserHandler);

        ByteArrayOutputStream dotOut = new ByteArrayOutputStream(1024);

        String ext = "svg";
        // svg generation
        new ProcessPipeline().invoke(
                asList(dotExec, "-T" + ext),
                classDiagramGenerator,
                dotOut
        );

        ByteArrayOutputStream svgStream = new ByteArrayOutputStream();
        new SVGTransformer().transform(new ByteArrayInputStream(dotOut.toByteArray()), svgStream);
        new SVGConverter()
                .svg2pngUsingBatik(
                        new ByteArrayInputStream(svgStream.toByteArray()),
                        out);
    }

    private void parse(CommandLine line) throws FileNotFoundException, UnsupportedEncodingException {
        if(line.getArgList().isEmpty() || line.hasOption("help")) {
            printUsage();
            return;
        }

        if(line.hasOption("in")) {
            String inFile = line.getOptionValue("in");
            File file = new File(inFile);
            if(!file.exists()) {
                System.err.println("File not found at " + file);
                return;
            }
            inputStream = new FileInputStream(file);
        }
        else {
            System.out.println("Waiting for data in standard input");
            inputStream = System.in;
        }

        if(line.hasOption("out")) {
            String outFile = line.getOptionValue("out");
            File file = new File(outFile);
            if(file.exists()) {
                System.out.println("File " + file + " will be overwritten");
            }
            File parentFile = file.getParentFile();
            if(!parentFile.mkdirs() && !parentFile.exists()) {
                System.err.println("Failed to create parent directory at " + parentFile);
                return;
            }

            out = new PrintStream(new FileOutputStream(file, false), true, "utf-8");
        }
        else {
            out = System.out;
        }

        fontName = line.getOptionValue("font", "jd");
        dotExec = line.getOptionValue("dot", "/usr/local/bin/dot");
        direction = line.getOptionValue("direction", "TD");
    }

    private void printUsage() {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "sketchIt", options );
    }
}
