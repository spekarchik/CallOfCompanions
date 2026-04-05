package com.pekar.callofcompanions.tooltip;

import net.minecraft.ChatFormatting;

public interface ITooltipLine
{
    ITooltipLine styledAs(TextStyle style, boolean applyStyle);
    ITooltipLine asHeader();
    ITooltipLine asSubHeader();
    ITooltipLine asNotice();
    ITooltipLine asImportantNotice();
    ITooltipLine asDarkGrey();
    ITooltipLine withFormatting(ChatFormatting formatting, boolean applyFormatting);
    void apply();
}
