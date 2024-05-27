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

public class US_hyceanManager {
    public static final Map<String, Float> baseWeight = new HashMap<>();
    public static final List<String> groups = new ArrayList<>();

    static {
        groups.add("atmosphere");
        groups.add("weather");
        groups.add("biosphere");
        groups.add("ore");
        groups.add("rare_ore");
        groups.add("volatiles");
        groups.add("organics");
        groups.add("special");

        baseWeight.put("atmosphere_no_pick", 0f);
        baseWeight.put(Conditions.THIN_ATMOSPHERE, 0f);
        baseWeight.put(Conditions.DENSE_ATMOSPHERE, 0f);

        baseWeight.put("weather_no_pick", 0f);
        baseWeight.put(Conditions.EXTREME_WEATHER, 0f);

        baseWeight.put("biosphere_no_pick", 0f);
        baseWeight.put(Conditions.INIMICAL_BIOSPHERE, 0f);

        baseWeight.put("ore_no_pick", 0f);
        baseWeight.put(Conditions.ORE_SPARSE, 0f);
        baseWeight.put(Conditions.ORE_MODERATE, 0f);
        baseWeight.put(Conditions.ORE_ABUNDANT, 0f);
        baseWeight.put(Conditions.ORE_RICH, 0f);
        baseWeight.put(Conditions.ORE_ULTRARICH, 0f);

        baseWeight.put("rare_ore_no_pick", 0f);
        baseWeight.put(Conditions.RARE_ORE_SPARSE, 0f);
        baseWeight.put(Conditions.RARE_ORE_MODERATE, 0f);
        baseWeight.put(Conditions.RARE_ORE_ABUNDANT, 0f);
        baseWeight.put(Conditions.RARE_ORE_RICH, 0f);
        baseWeight.put(Conditions.RARE_ORE_ULTRARICH, 0f);

        // Weights are WIP
        baseWeight.put("organics_no_pick", 20f);
        baseWeight.put(Conditions.ORGANICS_TRACE, 0f);
        baseWeight.put(Conditions.ORGANICS_COMMON, 5f);
        baseWeight.put(Conditions.ORGANICS_ABUNDANT, 20f);
        baseWeight.put(Conditions.ORGANICS_PLENTIFUL, 10f);

        baseWeight.put("volatiles_no_pick", 20f);
        baseWeight.put(Conditions.VOLATILES_TRACE, 0f);
        baseWeight.put(Conditions.VOLATILES_DIFFUSE, 5f);
        baseWeight.put(Conditions.VOLATILES_ABUNDANT, 20f);
        baseWeight.put(Conditions.VOLATILES_PLENTIFUL, 10f);

        baseWeight.put("US_special_no_pick", 20f);
        baseWeight.put("US_religious", 0f);
    }


    public static void manageHyceanConditions(PlanetAPI planet) {
        addConditionIfNeeded(planet, Conditions.WATER_SURFACE);

        for (String group : groups) {
            WeightedRandomPicker<String> picker = getGroupPicker(planet, group);
            String condition = picker.pick();

            if (!condition.endsWith(ConditionGenDataSpec.NO_PICK_SUFFIX)) {
                addConditionIfNeeded(planet, condition);
            }
        }
    }

    private static WeightedRandomPicker<String> getGroupPicker(PlanetAPI planet, String group) {
        Set<String> conditionsSoFar = new HashSet<>();

        for (MarketConditionAPI cond : planet.getMarket().getConditions()) {
            conditionsSoFar.add(cond.getId());
        }

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
}
