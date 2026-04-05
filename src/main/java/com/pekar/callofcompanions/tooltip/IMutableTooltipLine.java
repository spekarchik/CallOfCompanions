package com.pekar.callofcompanions.tooltip;

public interface IMutableTooltipLine extends ITooltipLine
{
    ITooltipLine fillWith(Object... values);
}
