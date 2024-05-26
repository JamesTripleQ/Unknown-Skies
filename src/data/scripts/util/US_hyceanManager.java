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
    public static Map<String, Float> baseWeight = new HashMap<>();
    private static final List<String> groups = new ArrayList<>();

    static {
        groups.add("volatiles");
        groups.add("organics");

        /*
        baseWeight.put("atmosphere_no_pick", 0f);
        baseWeight.put(Conditions.DENSE_ATMOSPHERE, 0f);

        baseWeight.put("weather_no_pick", 0f);
        baseWeight.put(Conditions.EXTREME_WEATHER, 0f);
        */

        /*
        pollution_no_pick
        pollution

        ore_no_pick
        ore_sparse
        ore_moderate
        ore_abundant
        ore_rich
        ore_ultrarich

        rare_ore_no_pick
        rare_ore_sparse
        rare_ore_moderate
        rare_ore_abundant
        rare_ore_rich
        rare_ore_ultrarich
        */

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

        /*
        US_special_no_pick
        US_religious
         */
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
