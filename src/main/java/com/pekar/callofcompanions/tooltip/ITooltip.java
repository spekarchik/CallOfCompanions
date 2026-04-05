package com.pekar.callofcompanions.tooltip;

public interface ITooltip
{
    ITooltip ignoreEmptyLines();
    ITooltip includeEmptyLines();
    void addEmptyLine();
    IMutableTooltipLine addLineById(String descriptionId);
    IMutableTooltipLine addLine(String descriptionRoot);
    IMutableTooltipLine addLine(String descriptionRoot, int descNumber);
}
