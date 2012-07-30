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
import sketchit.util.ProcessPipeline;

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
    private DocumentBuilder documentBuilder;
    private XPath xPath;

    @Before
    public void setUp () {
        repository = new Repository();
        customerClassElement = new ClassElement("Customer", asList("Firstname", "Lastname"), asList("copy()"));
        orderClassElement = new ClassElement("Order", asList("+price", "+quantity"), Collections.<String>emptyList());
        noteElement = new NoteElement("An aggregate root!");
    }

    @Before
    public void initXmlStuffs () throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                System.out.println("ClassDiagramGeneratorTest.warning");
                exception.printStackTrace();
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                System.out.println("ClassDiagramGeneratorTest.error");
                exception.printStackTrace();
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                System.out.println("ClassDiagramGeneratorTest.fatalError");
                exception.printStackTrace();
            }
        });

        documentBuilder.setEntityResolver(new EntityResolver() {
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
                    System.out.println("ClassDiagramGeneratorTest.resolveEntity(Unknown entity: " + publicId + ", " + systemId + ")");
                    return null;
                }
            }
        });

        XPathFactory xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
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

        System.out.println("ClassDiagramGeneratorTest.generate_oneClass\n\n" + new String(resultBytes));

        System.out.println("ClassDiagramGeneratorTest.generate_oneClass\n\n");

        if(ext.equals("svg")) {
            //System.out.println("ClassDiagramGeneratorTest.generate_oneClass " + dotOut.toString("utf-8"));

            Document document = documentBuilder.parse(new ByteArrayInputStream(resultBytes));

            Element defs = document.createElement("defs");
            defs.appendChild(createGradientForNode(document));
            defs.appendChild(createShadowFilter(document));

            Element root = document.getDocumentElement();
            increaseSVGSize(root);
            root.insertBefore(defs, root.getFirstChild());

            // --- replace fill by a nice looking... gradient
            XPathExpression xPathExpression = xPath.compile("descendant::*[@fill and parent::g[@class != 'edge']]");
            NodeList evaluate = (NodeList)xPathExpression.evaluate(root, XPathConstants.NODESET);

            // skip the first node
            for(int i=1;i<evaluate.getLength();i++) {
                Element element = (Element)evaluate.item(i);
                element.setAttribute("style","fill:url(#GradientNode)");
                element.setAttribute("filter","url(#FilterShadow)");
                element.removeAttribute("fill");
            }

            // --- replace fill:none by fill:white
            xPathExpression = xPath.compile("descendant::*[@fill='none']");
            evaluate = (NodeList)xPathExpression.evaluate(root, XPathConstants.NODESET);
            for(int i=1;i<evaluate.getLength();i++) {
                Element element = (Element)evaluate.item(i);
                element.setAttribute("fill", "white");
            }

            // --- scruff it
            scruffit(document);

            //

            DOMSource domSource = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            String stringResult = writer.toString();

            System.out.println(stringResult);

            resultBytes = stringResult.getBytes("utf-8");
        }

        FileOutputStream fout = new FileOutputStream("/Users/arnauld/tmp/classDiag." + ext);
        fout.write(resultBytes);
        fout.close();
    }

    private void increaseSVGSize(Element root) {
        String width = root.getAttribute("width");
        String height = root.getAttribute("height");

        String[] viewCoords = root.getAttribute("viewBox").split(" ");
        double x1 = Double.parseDouble(viewCoords[0]);
        double y1 = Double.parseDouble(viewCoords[1]);
        double x2 = Double.parseDouble(viewCoords[2]);
        double y2 = Double.parseDouble(viewCoords[3]);
        double dx = 0.2 * (x2 - x1);
        double dy = 0.2 * (y2 - y1);
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setMaximumFractionDigits(3);

        String newViewBox =
                nf.format(x1 - dx) + " " + nf.format(y1 - dy) + " " + nf.format(x2 + 2*dx) + " " + nf.format(y2 + 2*dy);
        root.setAttribute("viewBox", newViewBox);
        root.setAttribute("width", String.valueOf((int)(x2-x1 + dx+2*dx)) + "pt");
        root.setAttribute("height", String.valueOf((int)(y2-y1 + dy+2*dy)) + "pt");
    }

    private Element createShadowFilter(Document document) {
//        <filter id="dropshadow" height="130%">
//          <feGaussianBlur in="SourceAlpha" stdDeviation="3"/>
//          <feOffset dx="2" dy="2" result="offsetblur"/>
//          <feComponentTransfer>
//            <feFuncA type="linear" slope="0.2"/>
//          </feComponentTransfer>
//          <feMerge>
//            <feMergeNode/>
//            <feMergeNode in="SourceGraphic"/>
//          </feMerge>
//        </filter>

        Element feComponentTransfer = e(document, "feComponentTransfer");
        feComponentTransfer.appendChild(e(document, "feFuncA", t("type","linear"), t("slope", "0.2")));

        Element feMerge = e(document, "feMerge");
        feMerge.appendChild(e(document, "feMergeNode"));
        feMerge.appendChild(e(document, "feMergeNode", t("in", "SourceGraphic")));

        Element filter = e(document, "filter", t("id", "FilterShadow"), t("height", "130%"));
        filter.appendChild(e(document, "feGaussianBlur", t("in", "SourceAlpha"), t("stdDeviation", "3")));
        filter.appendChild(e(document, "feOffset", t("dx", "2"), t("dy", "2"), t("result", "offsetblur")));
        filter.appendChild(feComponentTransfer);
        filter.appendChild(feMerge);

        return filter;
    }

    private static T t(String name, String value) {
        return new T(name, value);
    }

    private static class T {
        public final String name;
        public final String value;

        private T(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private static Element e(Document document, String tagName, T...attributes) {
        Element element = document.createElement(tagName);
        for(T a : attributes) {
            element.setAttribute(a.name, a.value);
        }
        return element;
    }

    private Element createGradientForNode(Document document) {
        Element stop1 = document.createElement("stop");
        stop1.setAttribute("offset", "10%");
        stop1.setAttribute("stop-color", "#FFE200");

        Element stop2 = document.createElement("stop");
        stop2.setAttribute("offset", "90%");
        stop2.setAttribute("stop-color", "#FFEF73");

        Element linearGradientNode = document.createElement("linearGradient");
        linearGradientNode.setAttribute("id", "GradientNode");
        linearGradientNode.setAttribute("x1", "0%");
        linearGradientNode.setAttribute("y1", "0%");
        linearGradientNode.setAttribute("x2", "100%");
        linearGradientNode.setAttribute("y2", "100%");
        linearGradientNode.appendChild(stop1);
        linearGradientNode.appendChild(stop2);
        return linearGradientNode;
    }

    private Random random = new Random();

    private void scruffit(Document document) throws XPathExpressionException {
        Element root = document.getDocumentElement();

        XPathExpression xPathExpression = xPath.compile("descendant::*[parent::g[@class = 'node']]");
        NodeList evaluate = (NodeList)xPathExpression.evaluate(root, XPathConstants.NODESET);

        // skip the first node
        for(int i=1;i<evaluate.getLength();i++) {
            Element element = (Element)evaluate.item(i);
            String tagName = element.getTagName();
            if("polygon".equals(tagName)) {
                scruffPolygon(document, element);
            }
            else if("polyline".equals(tagName)){
                scruffPolyline(document, element);
            }
        }
    }

    private void scruffPolygon(Document document, Element element) {
        scruffPolyline(document, element);
        //addShade(document, element);
    }

    private void addShade(Document document, Element element) {
        double d = 2;

        Element shadeElement = document.createElement(element.getTagName());
        copyAttributes(element, shadeElement);
        shadeElement.setAttribute("style", "fill:url(#GradientShade)");
        //shadeElement.setAttribute("fill", "#999999");
        //shadeElement.setAttribute("stroke", "#999999");
        /*
        if(!shadeElement.hasAttribute("stroke-width")) {
            shadeElement.setAttribute("stroke-width", d + "");
        }
        */
        shadeElement.setAttribute("transform", "translate(" + d  + ", " + d + ")");

        Node parentNode = element.getParentNode();
        parentNode.insertBefore(shadeElement, element);
    }

    private static void copyAttributes(Element fromElement, Element toElement) {
        NamedNodeMap attributes = fromElement.getAttributes();
        for(int i=0; i<attributes.getLength(); i++) {
            Node item = attributes.item(i);
            toElement.setAttribute(item.getNodeName(), item.getNodeValue());
        }
    }

    private void scruffPolyline(Document document, Element element) {
        StringBuilder modified = new StringBuilder();
        List<Point> points = Point.parsePoints(element.getAttribute("points"));

        for(int i=0, n=points.size()-1; i<n; i++) {
            Point pn0 = points.get(i);
            Point pn1 = points.get(i+1);
            modified.append(pn0.asString()).append(' ');

            double distance = pn0.lengthTo(pn1);
            if(distance>10) {
                Point interm = pn0.split(pn1, distance*random.nextDouble()).angularMove(
                        distance * 0.02, 2 * Math.PI * random.nextDouble());
                modified.append(interm.asString()).append(' ');
            }
        }

        // append last point
        modified.append(points.get(points.size() - 1).asString());
        element.setAttribute("points", modified.toString());
    }

    public static class Point {
        public double x, y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }

        public String asString(NumberFormat nf) {
            return nf.format(x) + "," + nf.format(y);
        }
        public String asString() {
            // make sure decimal separator is a '.' ...
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            nf.setMaximumFractionDigits(3);
            return asString(nf);
        }

        public static List<Point> parsePoints(String points) {
            List<Point> pts = new ArrayList<Point>();
            for(String point : points.split("[ \t]+")) {
                pts.add(parsePoint(point));
            }
            return pts;
        }

        public static Point parsePoint(String point) {
            String[] coords = point.split(",");
            return new Point(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
        }

        public double lengthTo(Point other) {
            double tx = other.x - x;
            double ty = other.y - y;
            return Math.sqrt(tx*tx + ty*ty);
        }

        public Point split(Point other, double distance) {
            double tx = other.x - x;
            double ty = other.y - y;
            double len = Math.sqrt(tx*tx + ty*ty);

            // unit vector from (this)->(other)
            double vx = tx / len;
            double vy = ty / len;
            return new Point(x + distance * vx, y + distance * vy);
        }

        public Point angularMove(double radius, double angle) {
            Point point = new Point(x + radius * Math.cos(angle), y + radius * Math.sin(angle));
            System.out.println("ClassDiagramGeneratorTest$Point.angularMove(" + this + " -> " + point + ") r:" + radius);
            return point;
        }
    }
}
