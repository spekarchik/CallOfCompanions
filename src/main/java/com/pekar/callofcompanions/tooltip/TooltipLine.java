package com.pekar.callofcompanions.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.CheckReturnValue;

public class TooltipLine implements IMutableTooltipLine
{
    public static final String PLACEHOLDER = "{}";
    private final Tooltip tooltip;
    private MutableComponent component;
    private final boolean ignoreEmptyLines;

    TooltipLine(Tooltip tooltip, String descriptionRoot, boolean ignoreEmptyLines)
    {
        this.tooltip = tooltip;
        this.component = Component.translatable(descriptionRoot).withStyle(ChatFormatting.GRAY);
        this.ignoreEmptyLines = ignoreEmptyLines;
    }

    TooltipLine(Tooltip tooltip)
    {
        this.tooltip = tooltip;
        this.component = Component.empty();
        this.ignoreEmptyLines = false;
    }

    @CheckReturnValue
    @Override
    public ITooltipLine styledAs(TextStyle style, boolean applyStyle)
    {
        if (applyStyle)
        {
            switch (style)
            {
                case Header -> component.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE);
                case Subheader -> component.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GRAY);
                case Notice -> component.withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
                case ImportantNotice -> component.withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.WHITE);
                case DarkGray -> component.withStyle(ChatFormatting.DARK_GRAY);
                default -> component.withStyle(ChatFormatting.RESET).withStyle(ChatFormatting.GRAY);
            }
        }
        return this;
    }

    @CheckReturnValue
    @Override
    public final ITooltipLine asHeader()
    {
        return styledAs(TextStyle.Header);
    }

    @CheckReturnValue
    @Override
    public final ITooltipLine asSubHeader()
    {
        return styledAs(TextStyle.Subheader);
    }

    @CheckReturnValue
    @Override
    public final ITooltipLine asNotice()
    {
        return styledAs(TextStyle.Notice);
    }

    @CheckReturnValue
    @Override
    public final ITooltipLine asImportantNotice()
    {
        return styledAs(TextStyle.ImportantNotice);
    }

    @CheckReturnValue
    @Override
    public final ITooltipLine asDarkGrey()
    {
        return styledAs(TextStyle.DarkGray);
    }

    @CheckReturnValue
    @Override
    public final ITooltipLine withFormatting(ChatFormatting formatting, boolean applyFormatting)
    {
        if (applyFormatting)
            component.withStyle(formatting);

        return this;
    }

    @Override
    public ITooltipLine fillWith(Object... values)
    {
        String text = component.getString();
        int firstPlaceholder = text.indexOf(PLACEHOLDER);
        if (firstPlaceholder < 0 || values == null || values.length == 0)
            return this;

        StringBuilder replacedText = new StringBuilder(text.length());
        int valueIndex = 0;
        int currentIndex = 0;

        while (firstPlaceholder >= 0)
        {
            replacedText.append(text, currentIndex, firstPlaceholder);
            if (valueIndex < values.length)
                replacedText.append(values[valueIndex++]);
            else
                replacedText.append(PLACEHOLDER);

            currentIndex = firstPlaceholder + 2;
            firstPlaceholder = text.indexOf(PLACEHOLDER, currentIndex);
        }

        replacedText.append(text.substring(currentIndex));
        component = Component.literal(replacedText.toString()).withStyle(component.getStyle());
        return this;
    }

    @Override
    public final void apply()
    {
        if (isEmpty() && ignoreEmptyLines) return;
        tooltip.apply(this);
    }

    final Component getComponent()
    {
        return component;
    }

    private boolean isEmpty()
    {
        return component.getString().isEmpty();
    }

    private ITooltipLine styledAs(TextStyle style)
    {
        return styledAs(style, true);
    }
}