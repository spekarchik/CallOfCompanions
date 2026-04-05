package com.pekar.callofcompanions.tooltip;

import net.minecraft.network.chat.Component;

import javax.annotation.CheckReturnValue;
import java.util.function.Consumer;

public class Tooltip implements ITooltip
{
    private final Consumer<Component> tooltipComponent;
    private boolean ignoreEmptyLines;

    private Tooltip(Consumer<Component> tooltipComponent)
    {
        this.tooltipComponent = tooltipComponent;
    }

    public static ITooltip create(Consumer<Component> tooltipComponent)
    {
        return new Tooltip(tooltipComponent);
    }

    @Override
    public ITooltip ignoreEmptyLines()
    {
        ignoreEmptyLines = true;
        return this;
    }

    @Override
    public ITooltip includeEmptyLines()
    {
        ignoreEmptyLines = false;
        return this;
    }

    @CheckReturnValue
    @Override
    public void addEmptyLine()
    {
        tooltipComponent.accept(new TooltipLine(this).getComponent());
    }

    @Override
    public IMutableTooltipLine addLineById(String descriptionId)
    {
        return new TooltipLine(this, descriptionId, ignoreEmptyLines);
    }

    @CheckReturnValue
    @Override
    public IMutableTooltipLine addLine(String descriptionRoot)
    {
        return addLineById(descriptionRoot + ".desc");
    }

    @CheckReturnValue
    @Override
    public IMutableTooltipLine addLine(String descriptionRoot, int descNumber)
    {
        return addLineById(descriptionRoot + ".desc" + descNumber);
    }

    void apply(TooltipLine tooltipLine)
    {
        tooltipComponent.accept(tooltipLine.getComponent());
    }
}
