package sketchit.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Point2D {

    public double x, y;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "Point2D{" +
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

    public static List<Point2D> parsePoints(String point2Ds) {
        List<Point2D> pts = new ArrayList<Point2D>();
        for(String point2D : point2Ds.split("[ \t]+")) {
            pts.add(parsePoint(point2D));
        }
        return pts;
    }

    public static Point2D parsePoint(String point2D) {
        String[] coords = point2D.split(",");
        return new Point2D(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
    }

    public double lengthTo(Point2D other) {
        double tx = other.x - x;
        double ty = other.y - y;
        return Math.sqrt(tx*tx + ty*ty);
    }

    public Point2D split(Point2D other, double distance) {
        double tx = other.x - x;
        double ty = other.y - y;
        double len = Math.sqrt(tx*tx + ty*ty);

        // unit vector from (this)->(other)
        double vx = tx / len;
        double vy = ty / len;
        return new Point2D(x + distance * vx, y + distance * vy);
    }

    public Point2D angularMove(double radius, double angle) {
        return new Point2D(x + radius * Math.cos(angle), y + radius * Math.sin(angle));
    }
}
