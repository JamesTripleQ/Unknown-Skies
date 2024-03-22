package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.loading.specs.PlanetSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static data.scripts.US_modPlugin.LOG;

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
        planet.applySpecChanges();
    }

    /*----------------------
    ---- SETTINGS UTILS ----
    ----------------------*/

    public static JSONObject mergeModSettings() {
        // Merge the modSettings.json files
        JSONObject modSettings = null;
        try {
            modSettings = Global.getSettings().getMergedJSONForMod("data/config/modSettings.json", "US");
        } catch (IOException | JSONException ex) {
            LOG.fatal("unable to read modSettings.json", ex);
        }
        return modSettings;
    }

    public static Map<String, String> getMap(JSONObject modSettings, String modId, String id) {
        Map<String, String> value = new HashMap<>();
        // Try to get the requested mod settings
        if (modSettings.has(modId)) {
            try {
                JSONObject reqSettings = modSettings.getJSONObject(modId);
                // Try to get the requested value
                if (reqSettings.has(id)) {
                    JSONObject list = reqSettings.getJSONObject(id);
                    if (list.length() > 0) {
                        for (Iterator<?> iter = list.keys(); iter.hasNext(); ) {
                            String key = (String) iter.next();
                            String data = list.getString(key);
                            value.put(key, data);
                        }
                    }
                } else {
                    LOG.warn("unable to find " + id + " within " + modId + " in modSettings.json");
                }
            } catch (JSONException ex) {
                LOG.error("unable to read content of " + modId + " in modSettings.json", ex);
            }
        } else {
            LOG.warn("unable to find " + modId + " in modSettings.json");
        }

        return value;
    }

    public static List<String> getList(JSONObject modSettings, String modId, String id) {
        List<String> value = new ArrayList<>();
        // Try to get the requested mod settings
        if (modSettings.has(modId)) {
            try {
                JSONObject reqSettings = modSettings.getJSONObject(modId);
                // Try to get the requested value
                if (reqSettings.has(id)) {
                    JSONArray list = reqSettings.getJSONArray(id);
                    if (list.length() > 0) {
                        for (int j = 0; j < list.length(); j++) {
                            value.add(list.getString(j));
                        }
                    }
                } else {
                    LOG.warn("unable to find " + id + " within " + modId + " in modSettings.json");
                }
            } catch (JSONException ex) {
                LOG.error("unable to read content of " + modId + " in modSettings.json", ex);
            }
        } else {
            LOG.warn("unable to find " + modId + " in modSettings.json");
        }
        return value;
    }
}
