package data.scripts.util;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.util.*;

import static com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.getDataForGroup;
import static com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.preconditionsMet;
import static data.scripts.util.US_utils.addConditionIfNeeded;
import static data.scripts.util.US_utils.removeConditionIfNeeded;

public class US_hyceanManager {
    public static final Map<String, Float> baseWeight = new HashMap<>();
    public static final List<String> groups = new ArrayList<>();
    public static final List <String> REMOVED_CONDITIONS = new ArrayList<>();

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

        // Atmosphere
        baseWeight.put("atmosphere_no_pick", 10f);
        baseWeight.put(Conditions.THIN_ATMOSPHERE, 1f);
        baseWeight.put(Conditions.DENSE_ATMOSPHERE, 1f);

        // Weather
        baseWeight.put("weather_no_pick", 10f);
        baseWeight.put(Conditions.EXTREME_WEATHER, 4f);

        // Biosphere
        baseWeight.put("biosphere_no_pick", 10f);
        baseWeight.put(Conditions.INIMICAL_BIOSPHERE, 1f);

        // Ores
        baseWeight.put("ore_no_pick", 100f);
        baseWeight.put(Conditions.ORE_SPARSE, 10f);
        baseWeight.put(Conditions.ORE_MODERATE, 15f);
        baseWeight.put(Conditions.ORE_ABUNDANT, 4f);
        baseWeight.put(Conditions.ORE_RICH, 1f);

        // Rare Ores
        baseWeight.put("rare_ore_no_pick", 100f);
        baseWeight.put(Conditions.RARE_ORE_SPARSE, 5f);
        baseWeight.put(Conditions.RARE_ORE_MODERATE, 10f);
        baseWeight.put(Conditions.RARE_ORE_ABUNDANT, 4f);
        baseWeight.put(Conditions.RARE_ORE_RICH, 1f);

        // Volatiles
        baseWeight.put(Conditions.VOLATILES_ABUNDANT, 20f);
        baseWeight.put(Conditions.VOLATILES_PLENTIFUL, 15f);

        // Organics
        baseWeight.put(Conditions.ORGANICS_TRACE, 5f);
        baseWeight.put(Conditions.ORGANICS_COMMON, 20f);
        baseWeight.put(Conditions.ORGANICS_ABUNDANT, 5f);
    }


    public static void manageHyceanConditions(PlanetAPI planet) {
        for (String condition : REMOVED_CONDITIONS){
            removeConditionIfNeeded(planet, condition);
        }

        addConditionIfNeeded(planet, Conditions.WATER_SURFACE);

        for (String group : groups) {
            WeightedRandomPicker<String> picker = getGroupPicker(planet, group);
            String condition = picker.pick();

            if (condition == null) continue;

            if (!condition.endsWith(ConditionGenDataSpec.NO_PICK_SUFFIX)) {
                addConditionIfNeeded(planet, condition);
            }
        }

        if (preconditionsMet("US_religious", getConditionsSoFar(planet)) && Math.random() > 0.8f) {
            addConditionIfNeeded(planet, "US_religious");
        }
    }

    // Modified from PlanetConditionGenerator.java
    private static WeightedRandomPicker<String> getGroupPicker(PlanetAPI planet, String group) {
        Set<String> conditionsSoFar = getConditionsSoFar(planet);

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>(StarSystemGenerator.random);
        List<ConditionGenDataSpec> groupData = getDataForGroup(group);

        for (ConditionGenDataSpec data : groupData) {
            float weight = 0f;

            if (baseWeight.get(data.getId()) != null) {
                weight = baseWeight.get(data.getId());
            }

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
    private static Set<String> getConditionsSoFar(PlanetAPI planet) {
        Set<String> conditionsSoFar = new HashSet<>();

        for (MarketConditionAPI cond : planet.getMarket().getConditions()) {
            conditionsSoFar.add(cond.getId());
        }

        return conditionsSoFar;
    }
}
