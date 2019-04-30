package io.jshift.kit.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedMap;

import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Utility methods to access spring-boot resources.
 */
public class SpringBootUtil {

    private static final transient Logger LOG = LoggerFactory.getLogger(SpringBootUtil.class);

    /**
     * Returns the spring boot configuration (supports `application.properties` and `application.yml`)
     * or an empty properties object if not found
     */
    public static Properties getSpringBootApplicationProperties(URLClassLoader compileClassLoader) {
        URL ymlResource = compileClassLoader.findResource("application.yml");
        URL propertiesResource = compileClassLoader.findResource("application.properties");

        Properties props = YamlUtil.getPropertiesFromYamlResource(ymlResource);
        props.putAll(getPropertiesResource(propertiesResource));
        return props;
    }

    /**
     * Returns the given properties resource on the project classpath if found or an empty properties object if not
     */
    protected static Properties getPropertiesResource(URL resource) {
        Properties answer = new Properties();
        if (resource != null) {
            try(InputStream stream = resource.openStream()) {
                answer.load(stream);
            } catch (IOException e) {
                throw new IllegalStateException("Error while reading resource from URL " + resource, e);
            }
        }
        return answer;
    }

    /**
     * Determine the spring-boot devtools version for the current project
     */
    public static Optional<String> getSpringBootDevToolsVersion(MavenProject mavenProject) {
        return getSpringBootVersion(mavenProject);
    }

    /**
     * Determine the spring-boot major version for the current project
     */
    public static Optional<String> getSpringBootVersion(MavenProject mavenProject) {
        return Optional.ofNullable(MavenUtil.getDependencyVersion(mavenProject, SpringBootConfigurationHelper.SPRING_BOOT_GROUP_ID, SpringBootConfigurationHelper.SPRING_BOOT_ARTIFACT_ID));
    }



}

