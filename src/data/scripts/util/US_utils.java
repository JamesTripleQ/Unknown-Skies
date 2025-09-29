package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.MarketConditionSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import lunalib.lunaSettings.LunaSettings;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.getDataForGroup;
import static com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.preconditionsMet;
import static com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.*;
import static data.scripts.util.US_manualSystemFixer.US_STAR_VARIANT_KEY;

public class US_utils {

    /*-------------------------
    ---- TEXT & KEYS UTILS ----
    -------------------------*/

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

    // Returns a planet from the candidate list only picking moons if no other option is available (assumes candidate list isn't empty)
    public static PlanetAPI pickPlanetAvoidingMoons(List<PlanetAPI> candidates) {
        List<PlanetAPI> planets = new ArrayList<>();
        List<PlanetAPI> moons = new ArrayList<>();

        for (PlanetAPI candidate : candidates) {
            if (candidate.isMoon()) {
                moons.add(candidate);
            } else {
                planets.add(candidate);
            }
        }

        if (!planets.isEmpty()) {
            return planets.get(new Random().nextInt(planets.size()));
        }

        return moons.get(new Random().nextInt(moons.size()));
    }

    // Swaps gas/ice giants
    public static void swapGiant(PlanetAPI planet) {
        if (planet == null) {
            return;
        }

        String giantMode = LunaSettings.getString("US", "US_giantMode");

        if (giantMode == null || giantMode.equals("Vanilla")) {
            return;
        }

        switch (planet.getTypeId()) {
            case "gas_giant":
                switch (new Random().nextInt(giantMode.equals("Classic") ? 2 : 4)) {
                    case 1:
                        changePlanetType(planet, "US_gas_giantB");
                        break;
                    case 2:
                        changePlanetType(planet, "US_gas_giantB_2");
                        break;
                    case 3:
                        changePlanetType(planet, "US_gas_giantB_3");
                        break;
                }
                break;
            case "ice_giant":
                switch (new Random().nextInt(giantMode.equals("Classic") ? 2 : 2)) {
                    case 1:
                        changePlanetType(planet, "US_gas_giant");
                        break;
                    case 2:
                        changePlanetType(planet, "other");
                        break;
                }
                break;
        }
    }

    // Swaps to a random star variant
    public static void swapStarToRandom(PlanetAPI star) {
        if (star == null) {
            return;
        }

        String starMode = LunaSettings.getString("US", "US_starMode");

        if (starMode == null || starMode.equals("Vanilla")) {
            return;
        }

        switch (star.getTypeId()) {
            case "star_blue_giant":
            case "star_blue_supergiant":
                switch (new Random().nextInt(starMode.equals("Classic") ? 2 : 4)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_blue_giant"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_intense"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarBlue"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 255, 255));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_blue_giant_2"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_intense"));
                        star.applySpecChanges();
                        break;
                    case 3:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_blue_giant_3"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_yellow":
                switch (new Random().nextInt(starMode.equals("Classic") ? 2 : 4)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_yellow"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarYellow"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 255, 255));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_yellow_2"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                        star.applySpecChanges();
                        break;
                    case 3:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_yellow_3"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_orange":
            case "star_orange_giant":
                switch (new Random().nextInt(starMode.equals("Classic") ? 2 : 3)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_orange"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", star.getTypeId().contains("giant") ? "US_halo_intense" : "US_halo_unstable"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarYellow"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 125, 90));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_orange_2"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_red_giant":
            case "star_red_supergiant":
            case "star_red_dwarf":
                switch (new Random().nextInt(starMode.equals("Classic") ? 2 : 4)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_red_giant"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", star.getTypeId().contains("giant") ? "US_halo_intense" : "US_halo_unstable"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarYellow"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 200, 160));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_red_giant_2"));
                        star.applySpecChanges();
                        break;
                    case 3:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_red_giant_3"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", star.getTypeId().contains("giant") ? "US_halo_intense" : "US_halo_unstable"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_white":
                switch (new Random().nextInt(starMode.equals("Classic") ? 2 : 3)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_white"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarWhite"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 255, 255));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_white_2"));
                        star.applySpecChanges();
                        break;
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

    // Swaps to a given star variant based on memKeys
    public static void swapStarToVariant(PlanetAPI star) {
        if (star == null) {
            return;
        }

        switch (star.getMemoryWithoutUpdate().getInt(US_STAR_VARIANT_KEY)) {
            case 0:
                return;
            case -1:
                swapStarToRandom(star);
                return;
        }

        switch (star.getTypeId()) {
            case "star_blue_giant":
            case "star_blue_supergiant":
                switch (star.getMemoryWithoutUpdate().getInt(US_STAR_VARIANT_KEY)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_blue_giant"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_intense"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarBlue"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 255, 255));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_blue_giant_2"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_intense"));
                        star.applySpecChanges();
                        break;
                    case 3:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_blue_giant_3"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_yellow":
                switch (star.getMemoryWithoutUpdate().getInt(US_STAR_VARIANT_KEY)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_yellow"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarYellow"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 255, 255));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_yellow_2"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                        star.applySpecChanges();
                        break;
                    case 3:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_yellow_3"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_orange":
            case "star_orange_giant":
                switch (star.getMemoryWithoutUpdate().getInt(US_STAR_VARIANT_KEY)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_orange"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", star.getTypeId().contains("giant") ? "US_halo_intense" : "US_halo_unstable"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarYellow"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 125, 90));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_orange_2"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_red_giant":
            case "star_red_supergiant":
            case "star_red_dwarf":
                switch (star.getMemoryWithoutUpdate().getInt(US_STAR_VARIANT_KEY)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_red_giant"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", star.getTypeId().contains("giant") ? "US_halo_intense" : "US_halo_unstable"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarYellow"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 200, 160));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_red_giant_2"));
                        star.applySpecChanges();
                        break;
                    case 3:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_red_giant_3"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", star.getTypeId().contains("giant") ? "US_halo_intense" : "US_halo_unstable"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_white":
                switch (star.getMemoryWithoutUpdate().getInt(US_STAR_VARIANT_KEY)) {
                    case 1:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_white"));
                        star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                        star.getSpec().setCloudTexture(Global.getSettings().getSpriteName("clouds", "US_clouds_textureStarWhite"));
                        star.getSpec().setCloudRotation(star.getSpec().getRotation() - 3);
                        star.getSpec().setCloudColor(new Color(255, 255, 255));
                        star.applySpecChanges();
                        break;
                    case 2:
                        star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_white_2"));
                        star.applySpecChanges();
                        break;
                }
                break;
            case "star_browndwarf":
                if (star.getMemoryWithoutUpdate().getInt(US_STAR_VARIANT_KEY) == 1) {
                    star.getSpec().setTexture(Global.getSettings().getSpriteName("stars", "US_star_browndwarf"));
                    star.getSpec().setCoronaTexture(Global.getSettings().getSpriteName("coronas", "US_halo_unstable"));
                    star.applySpecChanges();
                }
                break;
        }
    }

    // Modified AddPlanet from StarSystemGenerator
    public static void changePlanetType(PlanetAPI planet, String type, boolean skipRandomColorVariation) {
        String glowTexture = planet.getSpec().getGlowTexture();
        Color glowColor = planet.getSpec().getGlowColor();
        boolean useReverseLightForGlow = planet.getSpec().isUseReverseLightForGlow();

        String shieldTexture = planet.getSpec().getShieldTexture();
        float shieldThickness = planet.getSpec().getShieldThickness();
        Color shieldColor = planet.getSpec().getShieldColor();

        String shieldTexture2 = planet.getSpec().getShieldTexture2();
        float shieldThickness2 = planet.getSpec().getShieldThickness2();
        Color shieldColor2 = planet.getSpec().getShieldColor2();

        planet.changeType(type, random);
        PlanetGenDataSpec planetData = (PlanetGenDataSpec) Global.getSettings().getSpec(PlanetGenDataSpec.class, planet.getSpec().getPlanetType(), false);

        if (!skipRandomColorVariation) {
            Color color = getColor(planetData.getMinColor(), planetData.getMaxColor());
            planet.getSpec().setPlanetColor(color);

            if (planet.getSpec().getAtmosphereThickness() > 0) {
                Color atmosphereColor = Misc.interpolateColor(planet.getSpec().getAtmosphereColor(), color, 0.25f);
                atmosphereColor = Misc.setAlpha(atmosphereColor, planet.getSpec().getAtmosphereColor().getAlpha());
                planet.getSpec().setAtmosphereColor(atmosphereColor);

                /* Disabled to keep consistency with a vanilla bug
                if (planet.getSpec().getCloudTexture() != null) {
                    Color cloudColor = Misc.interpolateColor(planet.getSpec().getCloudColor(), color, 0.25f);
                    cloudColor = Misc.setAlpha(cloudColor, planet.getSpec().getCloudColor().getAlpha());
                    planet.getSpec().setCloudColor(cloudColor);
                }*/
            }
        }

        float tilt = planet.getSpec().getTilt();
        float pitch = planet.getSpec().getPitch();

        float sign = Math.signum(random.nextFloat() - 0.5f);
        double r = random.nextFloat();

        if (sign > 0) {
            tilt += (float) (r * TILT_MAX);
        } else {
            tilt += (float) (r * TILT_MIN);
        }

        sign = Math.signum(random.nextFloat() - 0.5f);
        r = random.nextFloat();

        if (sign > 0) {
            pitch += (float) (r * PITCH_MAX);
        } else {
            tilt += (float) (r * PITCH_MIN);
        }

        planet.getSpec().setTilt(tilt);
        planet.getSpec().setPitch(pitch);

        if (planet.getSpec().getGlowTexture() == null && glowTexture != null) {
            planet.getSpec().setGlowTexture(glowTexture);
            if (glowColor != null) {
                planet.getSpec().setGlowColor(glowColor);
            }
            planet.getSpec().setUseReverseLightForGlow(useReverseLightForGlow);
            planet.getSpec().setAtmosphereThickness(0.5f);
            planet.getSpec().setCloudRotation(planet.getSpec().getCloudRotation() * (-1f - 2f * random.nextFloat()));
        }

        if (planet.getSpec().getShieldTexture() == null && shieldTexture != null) {
            planet.getSpec().setShieldTexture(shieldTexture);
            planet.getSpec().setShieldThickness(shieldThickness);
            planet.getSpec().setShieldColor(shieldColor);
        }

        if (planet.getSpec().getShieldTexture2() == null && shieldTexture2 != null) {
            planet.getSpec().setShieldTexture2(shieldTexture2);
            planet.getSpec().setShieldThickness2(shieldThickness2);
            planet.getSpec().setShieldColor2(shieldColor2);
        }

        planet.applySpecChanges();
    }

    public static void changePlanetType(PlanetAPI planet, String type) {
        changePlanetType(planet, type, false);
    }

    public static final WeightedRandomPicker<String> FLOATING_CONTINENT_RUINS = new WeightedRandomPicker<>();

    static {
        FLOATING_CONTINENT_RUINS.add(Conditions.RUINS_SCATTERED, 1);
        FLOATING_CONTINENT_RUINS.add(Conditions.RUINS_WIDESPREAD, 2);
        FLOATING_CONTINENT_RUINS.add(Conditions.RUINS_EXTENSIVE, 3);
        FLOATING_CONTINENT_RUINS.add(Conditions.RUINS_VAST, 1.5f);
    }

    public static void applyFloatingContinentRuins(PlanetAPI planet) {
        addConditionIfNeeded(planet, FLOATING_CONTINENT_RUINS.pick());
        if (new Random().nextInt(15) == 0) {
            addConditionIfNeeded(planet, Conditions.DECIVILIZED);
        }
    }

    public static final WeightedRandomPicker<String> METHANE_ORGANICS = new WeightedRandomPicker<>();

    static {
        METHANE_ORGANICS.add(Conditions.ORGANICS_COMMON, 5f);
        METHANE_ORGANICS.add(Conditions.ORGANICS_ABUNDANT, 20f);
        METHANE_ORGANICS.add(Conditions.ORGANICS_PLENTIFUL, 10f);
    }

    public static void applyMethaneOrganics(PlanetAPI planet) {
        addConditionIfNeeded(planet, METHANE_ORGANICS.pick());
    }

    /*--------------------
    ---- PICKER UTILS ----
    --------------------*/

    // Modified from PlanetConditionGenerator.java
    // Returns a picker with all the conditions of a given group
    public static WeightedRandomPicker<String> getGroupPicker(PlanetAPI planet, String group, Map<String, Float> baseWeights) {
        Set<String> conditionsSoFar = getConditionsSoFar(planet);

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(random);
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

    @SuppressWarnings("RedundantArrayCreation")
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
