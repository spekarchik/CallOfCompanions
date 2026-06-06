package com.pekar.callofcompanions.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class Definition<T>
{
    protected final String name;
    protected final T defaultValue;
    private T value;
    private final List<String> comment = new ArrayList<>();

    protected Definition(String name, T defaultValue)
    {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public T getValue()
    {
        return value;
    }

    public T get()
    {
        return getValue();
    }

    public void setValue(T value)
    {
        this.value = value;
    }

    protected void addComment(String comment)
    {
        this.comment.add(comment);
    }

    protected List<String> getComment()
    {
        return Collections.unmodifiableList(comment);
    }
}
