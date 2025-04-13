package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.getDataForGroup;
import static com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.preconditionsMet;

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

    // Handles swapping stars
    public static void swapStar(PlanetAPI star) {
        if (star == null) {
            return;
        }

        switch (star.getTypeId()) {
            case "star_blue_giant":
            case "star_blue_supergiant":
                switch (new Random().nextInt(3)) {
                    case 0:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_blue_giant"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_intense"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarBlue"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 255, 255));
                        star.applySpecChanges();
                        break;
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_blue_giant_2"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_intense"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_yellow":
                if (new Random().nextBoolean()) {
                    star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_yellow"));
                    star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                    star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarYellow"));
                    star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                    star.getSpec().setCloudColor(new Color(255, 255, 255));
                    star.applySpecChanges();
                }
                break;
            case "star_orange":
            case "star_orange_giant":
                switch (new Random().nextInt(3)) {
                    case 0:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_orange"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", star.getTypeId().contains("giant") ? "US_halo_intense" : "US_halo_unstable"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarYellow"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 125, 90));
                        star.applySpecChanges();
                        break;
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_orange_2"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_red_giant":
            case "star_red_supergiant":
            case "star_red_dwarf":
                if (new Random().nextBoolean()) {
                    star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_red_giant"));
                    star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", star.getTypeId().contains("giant") ? "US_halo_intense" : "US_halo_unstable"));
                    star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarYellow"));
                    star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                    star.getSpec().setCloudColor(new Color(255, 200, 160));
                    star.applySpecChanges();
                }
                break;
            case "star_white":
                if (new Random().nextBoolean()) {
                    star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_white"));
                    star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                    star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarWhite"));
                    star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                    star.getSpec().setCloudColor(new Color(255, 255, 255));
                    star.applySpecChanges();
                }
                break;
            case "star_browndwarf":
                if (new Random().nextBoolean()) {
                    star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_browndwarf"));
                    star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                    star.applySpecChanges();
                }
                break;
        }
    }

    /*--------------------
    ---- PICKER UTILS ----
    --------------------*/

    // Modified from PlanetConditionGenerator.java
    // Returns a picker with all the conditions of a given group
    public static WeightedRandomPicker<String> getGroupPicker(PlanetAPI planet, String group, Map<String, Float> baseWeights) {
        Set<String> conditionsSoFar = getConditionsSoFar(planet);

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(StarSystemGenerator.random);
        List<ConditionGenDataSpec> groupData = getDataForGroup(group);

        for (ConditionGenDataSpec data : groupData) {
            float weight = 0f;

            // Assigns the corresponding baseWeight value
            if (baseWeights.get(data.getId()) != null) {
                weight = baseWeights.get(data.getId());
            }

            // Applies multipliers
            for (String cid : conditionsSoFar) {
                if (data.hasMultiplier(cid)) {
                    weight *= data.getMultiplier(cid);
                }
            }

            if (weight <= 0) continue;
            if (!preconditionsMet(data.getId(), conditionsSoFar)) continue;

            picker.add(data.getId(), weight);
        }

        return picker;
    }

    // Returns a Set with all condition IDs
    public static Set<String> getConditionsSoFar(PlanetAPI planet) {
        Set<String> conditionsSoFar = new HashSet<>();

        for (MarketConditionAPI cond : planet.getMarket().getConditions()) {
            conditionsSoFar.add(cond.getId());
        }

        return conditionsSoFar;
    }

    /*-----------------------
    ---- CONDITION UTILS ----
    -----------------------*/

    public static int getFixedMarketSize(int size) {
        return Math.min(Math.max(size, 3), 6);
    }

    public static boolean isMarketColonized(MarketAPI market) {
        return !market.getMemoryWithoutUpdate().getBoolean("$isPlanetConditionMarketOnly")
                && !market.isPlanetConditionMarketOnly()
                && market.getFaction() != null
                && !market.getFactionId().equals(Factions.NEUTRAL);
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
