package be.yvanmazy.proxyonlinelinker.common.config;

import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class KebabCasePropertyUtils extends PropertyUtils {

    @Override
    public Property getProperty(final Class<?> type, final String name) {
        return super.getProperty(type, this.toCamelCase(name));
    }

    private String toCamelCase(final String kebabCase) {
        final StringBuilder builder = new StringBuilder();
        boolean next = false;
        for (final char c : kebabCase.toCharArray()) {
            if (c == '-') {
                next = true;
            } else if (next) {
                builder.append(Character.toUpperCase(c));
                next = false;
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

}