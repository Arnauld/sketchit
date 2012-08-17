package sketchit.transformer;

import static java.util.Arrays.asList;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class SVGConverter {

    private String inkscapePath;

    public SVGConverter() {
    }

    public SVGConverter usingInkscapePath(String inkscapePath) {
        this.inkscapePath = inkscapePath;
        return this;
    }

    public void svg2png(File svgFile, File pngFile, boolean fast) throws ConverterException, FileNotFoundException {
        if(fast || inkscapePath==null) {
            svg2pngUsingBatik(svgFile, pngFile);
        }
        else {
            svg2pngUsingInkscape(svgFile, pngFile);
        }
    }

    public void svg2pngUsingBatik(File svgFile, File pngFile) throws FileNotFoundException, ConverterException {
        FileInputStream inStream = null;
        FileOutputStream ostream = null;
        try {
            inStream = new FileInputStream(svgFile);
            ostream = new FileOutputStream(pngFile);
            svg2pngUsingBatik(inStream, ostream);
        }
        finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(ostream);
        }
    }

    private static final float KEY_PIXEL_UNIT_TO_MILLIMETER = 0.2645833f;
    private float keyPixelUnitToMillimeterFactor = 1.0f;

    public SVGConverter usingKeyPixelUnitToMillimeterFactor(float keyPixelUnitToMillimeterFactor) {
        this.keyPixelUnitToMillimeterFactor = keyPixelUnitToMillimeterFactor;
        return this;
    }

    public void svg2pngUsingBatik(InputStream svgIn, OutputStream pngOut) throws ConverterException {

        TranscoderInput input = new TranscoderInput(svgIn);
        TranscoderOutput output = new TranscoderOutput(pngOut);

        // Save the image.
        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER,
                KEY_PIXEL_UNIT_TO_MILLIMETER / keyPixelUnitToMillimeterFactor);
        try {
            transcoder.transcode(input, output);
        }
        catch (TranscoderException e) {
            throw new ConverterException("Fail to convert svg to png", e);
        }

    }

    public void svg2pngUsingInkscape(File svgFile, File pngFile) throws ConverterException {
        if(inkscapePath==null)
            throw new ConverterException("No runtime defined for Inkscape");

        // inkscape classDiag.svg -e classDiag.png -d 200
        ProcessBuilder pb = new ProcessBuilder(
                asList(inkscapePath, //
                        svgFile.getAbsolutePath(),//
                        "-e", //
                        pngFile.getAbsolutePath(),//
                        "-d", "200"));
        try {
            Process process = pb.start();
            process.waitFor();
        }
        catch (InterruptedException e) {
            throw new ConverterException("Fail to convert svg to png", e);
        }
        catch (IOException e) {
            throw new ConverterException("Fail to convert svg to png", e);
        }
    }

    public static class ConverterException extends Exception {
        public ConverterException(String message) {
            super(message);
        }

        public ConverterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
