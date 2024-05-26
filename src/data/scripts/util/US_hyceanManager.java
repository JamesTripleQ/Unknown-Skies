package data.scripts.util;

import com.fs.graphics.H;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.campaign.econ.MarketCondition;

import java.util.*;

import static com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.getDataForGroup;
import static com.fs.starfarer.api.impl.campaign.procgen.PlanetConditionGenerator.preconditionsMet;
import static data.scripts.util.US_utils.addConditionIfNeeded;

public class US_hyceanManager {
    private static final Map<String, Float> baseWeight = new HashMap<>();

    static {
        baseWeight.put(Conditions.ORGANICS_TRACE, 0f);
        baseWeight.put(Conditions.ORGANICS_COMMON, 5f);
        baseWeight.put(Conditions.ORGANICS_ABUNDANT, 20f);
        baseWeight.put(Conditions.ORGANICS_PLENTIFUL, 10f);

        baseWeight.put(Conditions.VOLATILES_TRACE, 0f);
        baseWeight.put(Conditions.VOLATILES_DIFFUSE, 5f);
        baseWeight.put(Conditions.VOLATILES_ABUNDANT, 20f);
        baseWeight.put(Conditions.VOLATILES_PLENTIFUL, 10f);
    }


    public static void manageHyceanConditions(PlanetAPI planet) {
        addConditionIfNeeded(planet, Conditions.WATER_SURFACE);

        WeightedRandomPicker<String> organics = getGroupPicker(planet, "organics");
        WeightedRandomPicker<String> volatiles = getGroupPicker(planet, "volatiles");

        addConditionIfNeeded(planet, organics.pick());
        addConditionIfNeeded(planet, volatiles.pick());
    }

    public static WeightedRandomPicker<String> getGroupPicker(PlanetAPI planet, String group) {

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
