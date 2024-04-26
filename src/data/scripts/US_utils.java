package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.awt.*;
import java.util.List;

public class US_utils {

    /*------------------
    ---- TEXT UTILS ----
    ------------------*/

    public static String txt(String id) {
        return Global.getSettings().getString("unknownSkies", id);
    }

    /*----------------------
    ---- PROC-GEN UTILS ----
    ----------------------*/

    public static void addRandomConditionIfNeeded(PlanetAPI p, List<String> toCheck, WeightedRandomPicker<String> picker) {
        // Check for the unwanted conditions
        boolean doIt = true;
        if (!p.getMarket().getConditions().isEmpty()) {
            for (MarketConditionAPI c : p.getMarket().getConditions()) {
                if (toCheck.contains(c.getId())) {
                    doIt = false;
                    break;
                }
            }
        }

        // Add the condition
        if (doIt) {
            p.getMarket().addCondition(picker.pick());
        }
    }

    public static void addConditionIfNeeded(PlanetAPI p, String toAdd) {
        // Check for the unwanted conditions
        boolean doIt = true;
        if (!p.getMarket().getConditions().isEmpty()) {
            for (MarketConditionAPI c : p.getMarket().getConditions()) {
                if (c.getId().equals(toAdd)) {
                    doIt = false;
                    break;
                }
            }
        }

        // Add the condition
        if (doIt) {
            p.getMarket().addCondition(toAdd);
        }
    }

    public static void removeConditionIfNeeded(PlanetAPI p, String toRemove) {
        // Check for the unwanted conditions
        boolean doIt = false;
        if (!p.getMarket().getConditions().isEmpty()) {
            for (MarketConditionAPI c : p.getMarket().getConditions()) {
                if (c.getId().equals(toRemove)) {
                    doIt = true;
                    break;
                }
            }
        }

        // Remove the condition
        if (doIt) {
            p.getMarket().removeCondition(toRemove);
        }
    }

    /*-----------------------
    ---- CONDITION UTILS ----
    -----------------------*/

    public static int getFixedMarketSize(int size) {
        return Math.min(Math.max(size, 3), 6);
    }

    public static void addScalingTable(MarketAPI market, TooltipMakerAPI tooltip, boolean expanded, String effect3, String effect4, String effect5, String effect6) {
        if (!expanded) {
            tooltip.addPara("Expand tooltip to view condition scaling", Misc.getGrayColor(), 10f);
        } else {
            Color base = market.getPlanetEntity().getSpec().getIconColor();
            Color dark = base.darker().darker().darker().darker();
            Color bright = base.brighter().brighter().brighter().brighter();

            if (market.getFactionId() != null) {
                if (!market.getFactionId().equals(Factions.NEUTRAL)) {
                    base = market.getFaction().getBaseUIColor();
                    dark = market.getFaction().getDarkUIColor();
                    bright = market.getFaction().getBrightUIColor();
                }
            }

            tooltip.addSectionHeading("Condition scaling", base, dark, Alignment.MID, 10f);

            tooltip.beginTable(base, dark, bright, 20f, true, true, new Object[]{"Colony size", 100f, "Condition effect", 130f});

            tooltip.addRow(Alignment.MID, Misc.getTextColor(), "3", Alignment.MID, Misc.getHighlightColor(), effect3);
            tooltip.addRow(Alignment.MID, Misc.getTextColor(), "4", Alignment.MID, Misc.getHighlightColor(), effect4);
            tooltip.addRow(Alignment.MID, Misc.getTextColor(), "5", Alignment.MID, Misc.getHighlightColor(), effect5);
            tooltip.addRow(Alignment.MID, Misc.getTextColor(), "6", Alignment.MID, Misc.getHighlightColor(), effect6);

            tooltip.addTable("", 0, 10f);
        }
    }
}
