package com.pekar.callofcompanions.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModConfigSpec
{
    private final List<Definition<?>> definitions;

    private ModConfigSpec(List<Definition<?>> definitions)
    {
        this.definitions = definitions;
    }

    public List<Definition<?>> getDefinitions()
    {
        return definitions;
    }

    public void load(Path path) throws IOException
    {
        Map<String, String> values = readTomlLike(path);

        for (var definition : definitions)
        {
            String value = values.get(definition.name);

            if (value == null)
            {
                continue;
            }

            if (definition instanceof BooleanValue booleanValue)
            {
                booleanValue.setValue(parseBoolean(value));
            }
            else if (definition instanceof IntValue intValue)
            {
                intValue.setValue(parseInt(value));
            }
            else if (definition instanceof DoubleValue doubleValue)
            {
                doubleValue.setValue(parseDouble(value));
            }
            else if (definition instanceof ConfigValue<?> configValue)
            {
                //noinspection unchecked
                ((ConfigValue<String>) configValue).setValue(parseString(value));
            }
        }

        save(path);
    }

    public void save(Path path) throws IOException
    {
        if (path.getParent() != null)
        {
            Files.createDirectories(path.getParent());
        }

        try (var writer = Files.newBufferedWriter(path))
        {
            String lastSection = null;

            for (var definition : definitions)
            {
                var sectionAndKey = splitTomlName(definition.name);
                String section = sectionAndKey.section();
                String key = sectionAndKey.key();

                if (lastSection == null || !lastSection.equals(section))
                {
                    if (lastSection != null)
                    {
                        writer.newLine();
                    }

                    if (!section.isEmpty())
                    {
                        writer.write("[" + section + "]");
                        writer.newLine();
                        writer.newLine();
                    }

                    lastSection = section;
                }

                for (var comment : definition.getComment())
                {
                    writer.write("# " + comment);
                    writer.newLine();
                }

                writer.write(key + " = " + formatValue(definition));
                writer.newLine();
                writer.newLine();
            }
        }
    }

    public static class BooleanValue extends Definition<Boolean>
    {
        private BooleanValue(String name, boolean defaultValue)
        {
            super(name, defaultValue);
        }

        public static BooleanValue define(String name, boolean defaultValue)
        {
            return new BooleanValue(name, defaultValue);
        }

        public boolean isTrue()
        {
            return getValue();
        }

        public boolean isFalse()
        {
            return !getValue();
        }
    }

    public static class IntValue extends Definition<Integer>
    {
        private final int min;
        private final int max;

        private IntValue(String name, int defaultValue, int min, int max)
        {
            super(name, defaultValue);
            this.min = min;
            this.max = max;
        }

        public static IntValue define(String name, int defaultValue, int min, int max)
        {
            return new IntValue(name, defaultValue, min, max);
        }

        public int getAsInt()
        {
            return getValue();
        }

        @Override
        public void setValue(Integer value)
        {
            var val = clampInt(value, min, max);
            super.setValue(val);
        }
    }

    public static class DoubleValue extends Definition<Double>
    {
        private final double min;
        private final double max;

        private DoubleValue(String name, double defaultValue, double min, double max)
        {
            super(name, defaultValue);
            this.min = min;
            this.max = max;
        }

        public static DoubleValue define(String name, double defaultValue, double min, double max)
        {
            return new DoubleValue(name, defaultValue, min, max);
        }

        @Override
        public void setValue(Double value)
        {
            var val = clampDouble(value, min, max);
            super.setValue(val);
        }
    }

    public static class ConfigValue<T> extends Definition<T>
    {
        private ConfigValue(String name, T defaultValue)
        {
            super(name, defaultValue);
        }

        public static ConfigValue<String> define(String name, String defaultValue)
        {
            return new ConfigValue<>(name, defaultValue);
        }
    }

    public static class Builder
    {
        private final List<String> comments = new ArrayList<>();
        private final List<Definition<?>> definitions = new ArrayList<>();
        private final List<String> sections = new ArrayList<>();

        public Builder comment(String... textLines)
        {
            comments.addAll(Arrays.asList(textLines));
            return this;
        }

        public Builder push(String section)
        {
            sections.add(section);
            return this;
        }

        public Builder pop()
        {
            if (!sections.isEmpty())
            {
                sections.removeLast();
            }
            return this;
        }

        public BooleanValue define(String name, boolean defaultValue)
        {
            var definition = BooleanValue.define(withSection(name), defaultValue);
            for (var comment : comments)
            {
                definition.addComment(comment);
            }

            comments.clear();
            definitions.add(definition);

            return definition;
        }

        public ConfigValue<String> define(String name, String defaultValue)
        {
            var definition = ConfigValue.define(withSection(name), defaultValue);
            for (var comment : comments)
            {
                definition.addComment(comment);
            }

            comments.clear();
            definitions.add(definition);

            return definition;
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max)
        {
            var definition = IntValue.define(withSection(name), defaultValue, min, max);
            for (var comment : comments)
            {
                definition.addComment(comment);
            }

            comments.clear();
            definitions.add(definition);

            return definition;
        }

        public DoubleValue defineInRange(String name, double defaultValue, double min, double max)
        {
            var definition = DoubleValue.define(withSection(name), defaultValue, min, max);
            for (var comment : comments)
            {
                definition.addComment(comment);
            }

            comments.clear();
            definitions.add(definition);

            return definition;
        }

        public ModConfigSpec build()
        {
            return new ModConfigSpec(List.copyOf(definitions));
        }

        private String withSection(String name)
        {
            if (sections.isEmpty()) return name;
            return String.join(".", sections) + "." + name;
        }
    }

    private record TomlName(String section, String key)
    {}

    private static TomlName splitTomlName(String fullName)
    {
        int idx = fullName.lastIndexOf('.');
        if (idx < 0)
        {
            return new TomlName("", fullName);
        }
        return new TomlName(fullName.substring(0, idx), fullName.substring(idx + 1));
    }

    private static Map<String, String> readTomlLike(Path path) throws IOException
    {
        var values = new HashMap<String, String>();

        if (!Files.exists(path))
        {
            return values;
        }

        String currentSection = "";
        for (var rawLine : Files.readAllLines(path))
        {
            String line = rawLine.strip();
            if (line.isEmpty()) continue;
            if (line.startsWith("#")) continue;

            int commentStart = line.indexOf('#');
            if (commentStart >= 0)
            {
                line = line.substring(0, commentStart).strip();
                if (line.isEmpty()) continue;
            }

            if (line.startsWith("[") && line.endsWith("]"))
            {
                currentSection = line.substring(1, line.length() - 1).strip();
                continue;
            }

            int eq = line.indexOf('=');
            if (eq < 0) continue;

            String key = line.substring(0, eq).strip();
            String value = line.substring(eq + 1).strip();
            if (key.isEmpty()) continue;

            String fullKey = currentSection.isEmpty() ? key : currentSection + "." + key;
            values.put(fullKey, value);
        }

        return values;
    }

    private static boolean parseBoolean(String raw)
    {
        return Boolean.parseBoolean(raw.strip());
    }

    private static int parseInt(String raw)
    {
        return Integer.parseInt(raw.strip());
    }

    private static double parseDouble(String raw)
    {
        String val = raw.strip();
        if (val.endsWith("D") || val.endsWith("d"))
        {
            val = val.substring(0, val.length() - 1).strip();
        }
        return Double.parseDouble(val);
    }

    private static String parseString(String raw)
    {
        String val = raw.strip();
        if (val.length() >= 2)
        {
            char first = val.charAt(0);
            char last = val.charAt(val.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\''))
            {
                val = val.substring(1, val.length() - 1);
            }
        }

        return val
                .replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static String formatValue(Definition<?> definition)
    {
        var value = definition.getValue();

        if (value instanceof String stringValue)
        {
            return "\"" + escapeString(stringValue) + "\"";
        }

        return String.valueOf(value);
    }

    private static String escapeString(String value)
    {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static int clampInt(int value, int min, int max)
    {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampDouble(double value, double min, double max)
    {
        return Math.max(min, Math.min(max, value));
    }
}
