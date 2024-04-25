package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.loading.specs.PlanetSpec;

import java.util.*;

public class US_utils {

    /*------------------
    ---- TEXT UTILS ----
    ------------------*/

    private static final String category = "unknownSkies";

    public static String txt(String id) {
        return Global.getSettings().getString(category, id);
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

    public static void changePlanetType(PlanetAPI planet, String newType) {
        PlanetSpecAPI planetSpec = planet.getSpec();
        for (final PlanetSpecAPI spec : Global.getSettings().getAllPlanetSpecs()) {
            if (spec.getPlanetType().equals(newType)) {
                planetSpec.setAtmosphereColor(spec.getAtmosphereColor());
                planetSpec.setAtmosphereThickness(spec.getAtmosphereThickness());
                planetSpec.setAtmosphereThicknessMin(spec.getAtmosphereThicknessMin());
                planetSpec.setCloudColor(spec.getCloudColor());
                planetSpec.setCloudRotation(spec.getCloudRotation());
                planetSpec.setCloudTexture(spec.getCloudTexture());
                planetSpec.setGlowColor(spec.getGlowColor());
                planetSpec.setGlowTexture(spec.getGlowTexture());
                planetSpec.setIconColor(spec.getIconColor());
                planetSpec.setPlanetColor(spec.getPlanetColor());
                planetSpec.setStarscapeIcon(spec.getStarscapeIcon());
                planetSpec.setTexture(spec.getTexture());
                planetSpec.setUseReverseLightForGlow(spec.isUseReverseLightForGlow());
                ((PlanetSpec) planetSpec).planetType = newType;
                ((PlanetSpec) planetSpec).name = spec.getName();
                ((PlanetSpec) planetSpec).descriptionId = ((PlanetSpec) spec).descriptionId;
                break;
            }
        }
        planet.setTypeId(newType);
        planet.applySpecChanges();
    }
}
