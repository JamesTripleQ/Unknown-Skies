package data.scripts.util;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.util.*;

import static com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.preconditionsMet;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator.LOCR_MINERS;
import static data.scripts.util.US_utils.*;

public class US_hyceanManager {
    // The base weight for a condition, equivalent to a non-multiplier entry in condition_gen_data.csv
    // If a condition isn't entered then it will default to a weight of 0
    public static final Map<String, Float> hyceanWeights = new HashMap<>();
    // All condition groups that need to be applied
    public static final List<String> groups = new ArrayList<>();
    // All conditions that need to be removed
    public static final List<String> REMOVED_CONDITIONS = new ArrayList<>();

    static {
        REMOVED_CONDITIONS.add(Conditions.HABITABLE);
        REMOVED_CONDITIONS.add(Conditions.TECTONIC_ACTIVITY);
        REMOVED_CONDITIONS.add(Conditions.EXTREME_TECTONIC_ACTIVITY);
        REMOVED_CONDITIONS.add(Conditions.NO_ATMOSPHERE);
        REMOVED_CONDITIONS.add(Conditions.THIN_ATMOSPHERE);
        REMOVED_CONDITIONS.add(Conditions.TOXIC_ATMOSPHERE);
        REMOVED_CONDITIONS.add(Conditions.DENSE_ATMOSPHERE);
        REMOVED_CONDITIONS.add(Conditions.MILD_CLIMATE);
        REMOVED_CONDITIONS.add(Conditions.EXTREME_WEATHER);
        REMOVED_CONDITIONS.add(Conditions.INIMICAL_BIOSPHERE);
        REMOVED_CONDITIONS.add(Conditions.METEOR_IMPACTS);
        REMOVED_CONDITIONS.add(Conditions.POLLUTION);
        REMOVED_CONDITIONS.add(Conditions.ORE_SPARSE);
        REMOVED_CONDITIONS.add(Conditions.ORE_MODERATE);
        REMOVED_CONDITIONS.add(Conditions.ORE_ABUNDANT);
        REMOVED_CONDITIONS.add(Conditions.ORE_RICH);
        REMOVED_CONDITIONS.add(Conditions.ORE_ULTRARICH);
        REMOVED_CONDITIONS.add(Conditions.RARE_ORE_SPARSE);
        REMOVED_CONDITIONS.add(Conditions.RARE_ORE_MODERATE);
        REMOVED_CONDITIONS.add(Conditions.RARE_ORE_ABUNDANT);
        REMOVED_CONDITIONS.add(Conditions.RARE_ORE_RICH);
        REMOVED_CONDITIONS.add(Conditions.RARE_ORE_ULTRARICH);
        REMOVED_CONDITIONS.add(Conditions.VOLATILES_TRACE);
        REMOVED_CONDITIONS.add(Conditions.VOLATILES_DIFFUSE);
        REMOVED_CONDITIONS.add(Conditions.VOLATILES_ABUNDANT);
        REMOVED_CONDITIONS.add(Conditions.VOLATILES_PLENTIFUL);
        REMOVED_CONDITIONS.add(Conditions.ORGANICS_TRACE);
        REMOVED_CONDITIONS.add(Conditions.ORGANICS_COMMON);
        REMOVED_CONDITIONS.add(Conditions.ORGANICS_ABUNDANT);
        REMOVED_CONDITIONS.add(Conditions.ORGANICS_PLENTIFUL);
        REMOVED_CONDITIONS.add(Conditions.FARMLAND_POOR);
        REMOVED_CONDITIONS.add(Conditions.FARMLAND_ADEQUATE);
        REMOVED_CONDITIONS.add(Conditions.FARMLAND_RICH);
        REMOVED_CONDITIONS.add(Conditions.FARMLAND_BOUNTIFUL);
        REMOVED_CONDITIONS.add(Conditions.RUINS_SCATTERED);
        REMOVED_CONDITIONS.add(Conditions.RUINS_WIDESPREAD);
        REMOVED_CONDITIONS.add(Conditions.RUINS_EXTENSIVE);
        REMOVED_CONDITIONS.add(Conditions.RUINS_VAST);
        REMOVED_CONDITIONS.add(Conditions.DECIVILIZED);
        REMOVED_CONDITIONS.add("US_bedrock");
        REMOVED_CONDITIONS.add("US_tunnels");
        REMOVED_CONDITIONS.add("US_religious");
        REMOVED_CONDITIONS.add("US_elevator");
        REMOVED_CONDITIONS.add("US_floating");
        REMOVED_CONDITIONS.add("US_crash");
        REMOVED_CONDITIONS.add("US_base");

        groups.add("atmosphere");
        groups.add("weather");
        groups.add("biosphere");
        groups.add("ore");
        groups.add("rare_ore");
        groups.add("volatiles");
        groups.add("organics");
        groups.add("ruins");
        groups.add("decivilized");

        // Atmosphere
        hyceanWeights.put("atmosphere_no_pick", 5f);
        hyceanWeights.put(Conditions.THIN_ATMOSPHERE, 10f);
        hyceanWeights.put(Conditions.DENSE_ATMOSPHERE, 1f);

        // Weather
        hyceanWeights.put("weather_no_pick", 10f);
        hyceanWeights.put(Conditions.EXTREME_WEATHER, 4f);

        // Biosphere
        hyceanWeights.put("biosphere_no_pick", 10f);
        hyceanWeights.put(Conditions.INIMICAL_BIOSPHERE, 1f);

        // Ores
        hyceanWeights.put("ore_no_pick", 100f);
        hyceanWeights.put(Conditions.ORE_SPARSE, 10f);
        hyceanWeights.put(Conditions.ORE_MODERATE, 15f);
        hyceanWeights.put(Conditions.ORE_ABUNDANT, 4f);
        hyceanWeights.put(Conditions.ORE_RICH, 1f);

        // Rare Ores
        hyceanWeights.put("rare_ore_no_pick", 100f);
        hyceanWeights.put(Conditions.RARE_ORE_SPARSE, 10f);
        hyceanWeights.put(Conditions.RARE_ORE_MODERATE, 10f);
        hyceanWeights.put(Conditions.RARE_ORE_ABUNDANT, 5f);

        // Volatiles
        hyceanWeights.put(Conditions.VOLATILES_ABUNDANT, 20f);
        hyceanWeights.put(Conditions.VOLATILES_PLENTIFUL, 15f);

        // Organics
        hyceanWeights.put("organics_no_pick", 5f);
        hyceanWeights.put(Conditions.ORGANICS_TRACE, 5f);
        hyceanWeights.put(Conditions.ORGANICS_COMMON, 5f);

        // Ruins
        hyceanWeights.put("ruins_no_pick", 30f);
        hyceanWeights.put(Conditions.RUINS_SCATTERED, 10f);
        hyceanWeights.put(Conditions.RUINS_WIDESPREAD, 5f);
        hyceanWeights.put(Conditions.RUINS_EXTENSIVE, 3f);
        hyceanWeights.put(Conditions.RUINS_VAST, 1f);

        // Decivilized
        hyceanWeights.put("decivilized_no_pick", 100f);
        hyceanWeights.put(Conditions.DECIVILIZED, 30f);
    }

    public static void manageHyceanConditions(PlanetAPI planet) {
        // Removes conditions
        for (String condition : REMOVED_CONDITIONS) {
            removeConditionIfNeeded(planet, condition);
        }

        // Adds Water-covered Surface
        addConditionIfNeeded(planet, Conditions.WATER_SURFACE);

        // Picks a condition for each group
        for (String group : groups) {
            WeightedRandomPicker<String> picker = getGroupPicker(planet, group, hyceanWeights);
            String condition = picker.pick();

            if (condition == null) continue;
            // If a "no_pick" condition is picked then it will add nothing
            if (condition.endsWith(ConditionGenDataSpec.NO_PICK_SUFFIX)) continue;

            addConditionIfNeeded(planet, condition);
        }

        // Chance to add Ancient Religious Landmark if conditions are met
        if (preconditionsMet("US_religious", getConditionsSoFar(planet)) && new Random().nextInt(6) == 0) {
            addConditionIfNeeded(planet, "US_religious");
        }

        // Fix for a super rare bug with the vanilla miner colony event
        if (planet.getMemoryWithoutUpdate().getBoolean(LOCR_MINERS)) {
            addConditionIfNeeded(planet, Conditions.VOLATILES_PLENTIFUL);
        }
    }
}
