package be.yvanmazy.proxyonlinelinker.common.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigurationReader {

    private ConfigurationReader() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    @Contract(pure = true)
    public static @NotNull Configuration read(final @NotNull Path path) throws IOException {
        if (Files.notExists(path)) {
            final Path parent = path.getParent();
            if (Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            final InputStream stream = ConfigurationReader.class.getClassLoader().getResourceAsStream(path.getFileName().toString());
            if (stream != null) {
                try (stream) {
                    Files.copy(stream, path);
                }
            }
        }
        final Constructor constructor = new Constructor(DummyConfiguration.class, new LoaderOptions());

        final PropertyUtils propertyUtils = new KebabCasePropertyUtils();
        propertyUtils.setSkipMissingProperties(true);
        propertyUtils.setAllowReadOnlyProperties(true);
        constructor.setPropertyUtils(propertyUtils);

        final Yaml yaml = new Yaml(constructor);

        try (final InputStream in = Files.newInputStream(path)) {
            final DummyConfiguration configuration = yaml.loadAs(in, DummyConfiguration.class);
            configuration.validate();
            return configuration;
        }
    }

}