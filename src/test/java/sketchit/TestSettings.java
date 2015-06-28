package sketchit;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class TestSettings {
    private Properties properties;

    public String buildDir() {
        return getProperties().getProperty("buildDir");
    }

    public Properties getProperties() {
        if (properties == null) {
            InputStream stream = TestSettings.class.getResourceAsStream("/usecases.properties");
            try {
                properties = new Properties();
                properties.load(stream);
            } catch (IOException e) {
                throw new RuntimeException("Fail to read properties", e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return properties;
    }
}
