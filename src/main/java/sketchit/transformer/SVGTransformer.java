package sketchit.transformer;

import static sketchit.util.Xml.e;
import static sketchit.util.Xml.a;

import sketchit.util.Point2D;
import sketchit.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class SVGTransformer {

    private Random random = new Random();
    private Xml xml = new Xml();

    public void transform(InputStream inputStream, OutputStream outputStream)
            throws
            IOException,
            SAXException,
            ParserConfigurationException,
            XPathExpressionException,
            TransformerException
    {
        Document document = xml.newDocument(inputStream);

        increaseSVGSize(document);
        createDefs(document);
        applyGradientExceptOnEdge(document);
        scruffit(document);

        xml.serializeUTF8(document, outputStream);
    }

    private void applyGradientExceptOnEdge(Document document) throws XPathExpressionException {
        Element root = document.getDocumentElement();

        // --- replace fill by a nice looking... gradient

        XPathExpression xPathExpression = xml.compileXPath("descendant::*[@fill and parent::g[@class != 'edge']]");
        NodeList evaluate = (NodeList)xPathExpression.evaluate(root, XPathConstants.NODESET);

        // skip the first node
        for(int i=1;i<evaluate.getLength();i++) {
            Element element = (Element)evaluate.item(i);
            element.setAttribute("style","fill:url(#GradientNode)");
            element.setAttribute("filter","url(#FilterShadow)");
            element.removeAttribute("fill");
        }

        // --- replace fill:none by fill:white
        xPathExpression = xml.compileXPath("descendant::*[@fill='none']");
        evaluate = (NodeList)xPathExpression.evaluate(root, XPathConstants.NODESET);
        for(int i=1;i<evaluate.getLength();i++) {
            Element element = (Element)evaluate.item(i);
            element.setAttribute("fill", "white");
        }
    }

    private void createDefs(Document document) {
        Element defs = document.createElement("defs");
        defs.appendChild(createGradientForNode(document));
        defs.appendChild(createShadowFilter(document));

        Element root = document.getDocumentElement();
        root.insertBefore(defs, root.getFirstChild());
    }

    private void increaseSVGSize(Document document) {
        Element root = document.getDocumentElement();

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
        feComponentTransfer.appendChild(e(document, "feFuncA", a("type", "linear"), a("slope", "0.2")));

        Element feMerge = e(document, "feMerge");
        feMerge.appendChild(e(document, "feMergeNode"));
        feMerge.appendChild(e(document, "feMergeNode", a("in", "SourceGraphic")));

        Element filter = e(document, "filter", a("id", "FilterShadow"), a("height", "130%"));
        filter.appendChild(e(document, "feGaussianBlur", a("in", "SourceAlpha"), a("stdDeviation", "3")));
        filter.appendChild(e(document, "feOffset", a("dx", "2"), a("dy", "2"), a("result", "offsetblur")));
        filter.appendChild(feComponentTransfer);
        filter.appendChild(feMerge);

        return filter;
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

    private void scruffit(Document document) throws XPathExpressionException {
        Element root = document.getDocumentElement();

        XPathExpression xPathExpression = xml.compileXPath("descendant::*[parent::g[@class = 'node']]");
        NodeList evaluate = (NodeList)xPathExpression.evaluate(root, XPathConstants.NODESET);

        // skip the first node
        for(int i=1;i<evaluate.getLength();i++) {
            Element element = (Element)evaluate.item(i);
            String tagName = element.getTagName();
            if("polygon".equals(tagName)) {
                scruffPolygon(element);
            }
            else if("polyline".equals(tagName)){
                scruffPolyline(element);
            }
        }
    }

    private void scruffPolygon(Element element) {
        scruffPolyline(element);
    }

    private void scruffPolyline(Element element) {
        StringBuilder modified = new StringBuilder();
        List<Point2D> points = Point2D.parsePoints(element.getAttribute("points"));

        for(int i=0, n=points.size()-1; i<n; i++) {
            Point2D pn0 = points.get(i);
            Point2D pn1 = points.get(i+1);
            modified.append(pn0.asString()).append(' ');

            double distance;
            while((distance = pn0.lengthTo(pn1))>20) {
                Point2D interm = pn0.split(pn1, distance*random.nextDouble()).angularMove(
                        distance * 0.02, 2 * Math.PI * random.nextDouble());
                modified.append(interm.asString()).append(' ');
                pn0 = interm;
            }
        }

        // append last point
        modified.append(points.get(points.size() - 1).asString());
        element.setAttribute("points", modified.toString());
    }

}
