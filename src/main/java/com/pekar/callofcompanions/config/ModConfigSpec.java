package com.pekar.callofcompanions.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
        Properties properties = new Properties();

        if (Files.exists(path))
        {
            try (var reader = Files.newBufferedReader(path))
            {
                properties.load(reader);
            }
        }

        for (var definition : definitions)
        {
            String value = properties.getProperty(definition.name);

            if (value == null)
            {
                continue;
            }

            if (definition instanceof BooleanValue booleanValue)
            {
                booleanValue.setValue(Boolean.parseBoolean(value));
            }
            else if (definition instanceof IntValue intValue)
            {
                intValue.setValue(Integer.parseInt(value));
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
            for (var definition : definitions)
            {
                for (var comment : definition.getComment())
                {
                    writer.write("# " + comment);
                    writer.newLine();
                }

                writer.write(definition.name + "=" + definition.getValue());
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
            var val = Math.clamp(value, min, max);
            super.setValue(val);
        }
    }

    public static class Builder
    {
        private final List<String> comments = new ArrayList<>();
        private final List<Definition<?>> definitions = new ArrayList<>();

        public Builder comment(String... textLines)
        {
            comments.addAll(Arrays.asList(textLines));
            return this;
        }

        public BooleanValue define(String name, boolean defaultValue)
        {
            var definition = BooleanValue.define(name, defaultValue);
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
            var definition = IntValue.define(name, defaultValue, min, max);
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
    }
}
