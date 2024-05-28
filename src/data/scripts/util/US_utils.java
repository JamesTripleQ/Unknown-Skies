package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

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

    // Modified from AddCondition.java from Console Commands
    public static void addConditionIfNeeded(PlanetAPI planet, String toAdd) {
        final MarketAPI market = planet.getMarket();
        final MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(toAdd);

        if (spec == null) {
            return;
        }

        // Check if condition already exists
        final String id = spec.getId();

        if (market.hasCondition(id)) {
            return;
        }

        // Create condition and mark any existing conditions that conflict with it for later removal
        final MarketConditionAPI condition = market.getSpecificCondition(market.addCondition(id));
        final ConditionGenDataSpec gen = condition.getGenSpec();
        final Set<String> toRemove = new HashSet<>();

        if (gen != null) {
            final Set<String> mutuallyExclusive = gen.getRequiresNotAny();
            for (MarketConditionAPI otherCon : market.getConditions()) {
                if (otherCon == condition) continue;

                // Automatically remove any mutually exclusive conditions
                final String otherId = otherCon.getId();
                if (mutuallyExclusive.contains(otherCon.getId())) {
                    toRemove.add(otherId);
                    continue;
                }

                // Only allow one condition from the same condition group
                final ConditionGenDataSpec otherGen = otherCon.getGenSpec();
                if (otherGen != null && gen.getGroup().equals(otherGen.getGroup())) {
                    toRemove.add(otherId);
                }
            }
        }

        // Remove all conflicting conditions
        for (String tmp : toRemove) market.removeCondition(tmp);

        // Ensure new condition is visible if market has already been surveyed
        if (market.getSurveyLevel() == MarketAPI.SurveyLevel.FULL && condition.requiresSurveying()) {
            condition.setSurveyed(true);
        }

        market.reapplyConditions();
    }

    // Modified from RemoveCondition.java from Console Commands
    public static void removeConditionIfNeeded(PlanetAPI planet, String toRemove) {
        final MarketAPI market = planet.getMarket();
        final MarketConditionSpecAPI spec = Global.getSettings().getMarketConditionSpec(toRemove);

        if (spec == null) {
            return;
        }

        final String id = spec.getId();

        if (!market.hasCondition(id)) {
            return;
        }

        market.removeCondition(id);
        market.reapplyConditions();
    }

    /*-----------------------
    ---- CONDITION UTILS ----
    -----------------------*/

    public static int getFixedMarketSize(int size) {
        return Math.min(Math.max(size, 3), 6);
    }

    public static void addScalingTable(MarketAPI market, TooltipMakerAPI tooltip, boolean expanded, int type, String effect3, String effect4, String effect5, String effect6) {
        if (!expanded) {
            tooltip.addPara(txt("tooltip_hint"), Misc.getGrayColor(), 10f);
        } else {
            float effectWidth = 110;

            if (type == 2) {
                effectWidth = 145;
            }

            Color base = market.getPlanetEntity().getSpec().getIconColor();
            Color dark = base.darker().darker().darker().darker();
            Color bright = base.brighter().brighter().brighter().brighter();

            String faction = market.getFactionId();

            if (faction != null) {
                if (!faction.equals(Factions.NEUTRAL)) {
                    base = market.getFaction().getBaseUIColor();
                    dark = market.getFaction().getDarkUIColor();
                    bright = market.getFaction().getBrightUIColor();
                }
            }

            tooltip.addSectionHeading(txt("tooltip_header"), base, dark, Alignment.MID, 10f);

            tooltip.beginTable(base, dark, bright, 20f, true, true, new Object[]{txt("tooltip_size"), 100f, txt("tooltip_scale_" + type), effectWidth});

            tooltip.addRow(Alignment.MID, Misc.getTextColor(), "3", Alignment.MID, Misc.getHighlightColor(), effect3);
            tooltip.addRow(Alignment.MID, Misc.getTextColor(), "4", Alignment.MID, Misc.getHighlightColor(), effect4);
            tooltip.addRow(Alignment.MID, Misc.getTextColor(), "5", Alignment.MID, Misc.getHighlightColor(), effect5);
            tooltip.addRow(Alignment.MID, Misc.getTextColor(), "6", Alignment.MID, Misc.getHighlightColor(), effect6);

            tooltip.addTable("", 0, 10f);
        }
    }
}
