package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.PlanetSpecAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation.ConstellationType;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import com.fs.starfarer.loading.specs.PlanetSpec;

import java.awt.Color;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.log4j.Logger;

public class US_modPlugin extends BaseModPlugin {
    public static Logger LOG = Global.getLogger(US_modPlugin.class);
    private final List<String> BG_YOUNG = new ArrayList<>();
    private final List<String> BG_AVERAGE = new ArrayList<>();
    private final List<String> BG_OLD = new ArrayList<>();
    private final List<String> NEB_YOUNG = new ArrayList<>();
    private final List<String> NEB_AVERAGE = new ArrayList<>();
    private final List<String> NEB_OLD = new ArrayList<>();

    private List<String> CRYSTAL_LIST = new ArrayList<>();
    private List<String> SPORE_LIST = new ArrayList<>();
    private List<String> SHROOM_LIST = new ArrayList<>();
    private List<String> VIRUS_LIST = new ArrayList<>();
    private List<String> ARTIFICIAL_LIST = new ArrayList<>();

    private final WeightedRandomPicker<String> RUINS = new WeightedRandomPicker<>();

    {
        RUINS.add("ruins_scattered", 1);
        RUINS.add("ruins_widespread", 2);
        RUINS.add("ruins_extensive", 3);
        RUINS.add("ruins_vast", 1.5f);
        RUINS.add("decivilized", 0.5f);
    }

    private final Map<String, Integer> PLANET_TYPES = new HashMap<>();
    private final Map<String, Integer> SPECIAL_CONDITIONS = new HashMap<>();

    @Override
    public void onNewGame() {
        // Store mod version for save patching
        Global.getSector().getMemoryWithoutUpdate().set("$unknownSkies_version", 1.00f);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        // Save patching code
        if (Global.getSector().getMemoryWithoutUpdate().getFloat("$unknownSkies_version") < 1.00f) {
            Global.getSector().getMemoryWithoutUpdate().set("$unknownSkies_version", 1.00f);
            for (StarSystemAPI s : Global.getSector().getStarSystems()) {
                if (s != null && s.isProcgen() && s.getConstellation() != null) {
                    getData();
                    BGReplacement(s);
                }
            }
        }
    }

    @Override
    public void onNewGameAfterProcGen() {
        // Set aquaculture planets
        Farming.AQUA_PLANETS.add("US_water");
        Farming.AQUA_PLANETS.add("US_waterB");

        // Read the background list
        if (BG_YOUNG.isEmpty()) {
            getData();
        }

        // Replace backgrounds
        for (StarSystemAPI s : Global.getSector().getStarSystems()) {
            if (s != null && s.isProcgen() && s.getConstellation() != null) {
                BGReplacement(s);
            }
        }

        List<PlanetAPI> crystalCandidates = new ArrayList<>();
        List<PlanetAPI> sporeCandidates = new ArrayList<>();
        List<PlanetAPI> shroomCandidates = new ArrayList<>();
        List<PlanetAPI> virusCandidates = new ArrayList<>();
        List<PlanetAPI> artificialCandidates = new ArrayList<>();

        float planets = 0;

        // Seed planetary conditions
        for (StarSystemAPI s : Global.getSector().getStarSystems()) {
            if (s == null) continue;
            if (!s.isProcgen()) continue;
            if (s.getPlanets().isEmpty()) continue;

            for (PlanetAPI p : s.getPlanets()) {
                if (p.isStar()) continue;

                // Log planet types
                if (PLANET_TYPES.containsKey(p.getTypeId())) {
                    PLANET_TYPES.put(p.getTypeId(), PLANET_TYPES.get(p.getTypeId()) + 1);
                } else {
                    PLANET_TYPES.put(p.getTypeId(), 1);
                }

                planets++;

                // Log special conditions
                if (p.getMarket().hasCondition("US_floating")) {
                    LOG.info("FLOATING CONTINENT found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    AddRandomConditionIfNeeded(p, RUINS.getItems(), RUINS);
                    LOG.info(" ");
                    countConditions("Floating Continent");
                }
                if (p.getMarket().hasCondition("US_religious")) {
                    LOG.info("RELIGIOUS SITE found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    LOG.info(" ");
                    countConditions("Religious Landmark");
                }
                if (p.getMarket().hasCondition("US_base")) {
                    LOG.info("MILITARY BASE found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    LOG.info(" ");
                    countConditions("Abandoned Base");
                }
                if (p.getMarket().hasCondition("US_crash")) {
                    LOG.info("CRASHED DRONE found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    LOG.info(" ");
                    countConditions("Crashed Drone");
                }
                if (p.getMarket().hasCondition("US_virus")) {
                    LOG.info("VIRUS found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    LOG.info(" ");
                    countConditions("Military Virus");
                }
                if (p.getMarket().hasCondition("US_elevator")) {
                    LOG.info("SPACE ELEVATOR found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    LOG.info(" ");
                    countConditions("Space Elevator");
                }
                if (p.getMarket().hasCondition("US_shrooms")) {
                    LOG.info("MAGIC SHROOMS found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    LOG.info(" ");
                    countConditions("Magic Shrooms");
                }
                if (p.getMarket().hasCondition("US_tunnels")) {
                    LOG.info("UNDERGROUND MAZE found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    LOG.info(" ");
                    countConditions("Underground Maze");
                }
                if (p.getMarket().hasCondition("US_mind")) {
                    LOG.info("PARASITIC SPORES found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    LOG.info(" ");
                    countConditions("Parasitic Spores");
                }
                if (p.getMarket().hasCondition("US_bedrock")) {
                    LOG.info("ACCESSIBLE BEDROCK found on " + p.getName() + " (" + p.getTypeId() + ") in " + s.getName());
                    LOG.info(" ");
                    countConditions("Accessible Bedrock");
                }

                // Find special condition candidates
                if (CRYSTAL_LIST.contains(p.getTypeId())) {
                    crystalCandidates.add(p);
                } else if (SPORE_LIST.contains(p.getTypeId())) {
                    if (!p.getStarSystem().isDeepSpace()) {
                        sporeCandidates.add(p);
                    }
                } else if (SHROOM_LIST.contains(p.getTypeId())) {
                    if (!p.getStarSystem().isDeepSpace()) {
                        shroomCandidates.add(p);
                    }
                } else if (VIRUS_LIST.contains(p.getTypeId())) {
                    if (!p.getStarSystem().isDeepSpace()) {
                        virusCandidates.add(p);
                    }
                } else if (ARTIFICIAL_LIST.contains(p.getTypeId())) {
                    if ((p.getStarSystem().hasTag(Tags.THEME_DERELICT) || p.getStarSystem().hasTag(Tags.THEME_RUINS)) && !p.getStarSystem().isDeepSpace()) {
                        artificialCandidates.add(p);
                    }
                }

                // Add irradiated to burnt planets
                if (p.getTypeId().equals("US_burnt")) {
                    LOG.info("Adding IRRADIATED condition to " + p.getName() + " in " + s.getName());
                    AddConditionIfNeeded(p, "irradiated");
                    LOG.info(" ");
                    countConditions("Irradiated Environment");
                }
            }
        }

        // Crystal placement
        if (!crystalCandidates.isEmpty()) {
            for (PlanetAPI planet : crystalCandidates) {
                if (Math.random() > 0.75f) {
                    LOG.info("Adding CRYSTAL condition to " + planet.getName() + " in " + planet.getStarSystem().getName());
                    AddConditionIfNeeded(planet, "US_crystals");
                    LOG.info(" ");
                    countConditions("Chemical Crystals");
                }
            }
        }

        // Spore placement
        if (!sporeCandidates.isEmpty()) {
            PlanetAPI planet = sporeCandidates.get(new Random().nextInt(sporeCandidates.size()));
            LOG.info("Adding SPORE condition to " + planet.getName() + " in " + planet.getStarSystem().getName());
            AddConditionIfNeeded(planet, "US_mind");
            LOG.info(" ");
            countConditions("Parasitic Spores");

            // Setup for magnetic planet pick
            sporeCandidates.remove(planet);
        }

        // Fungus placement
        if (!shroomCandidates.isEmpty()) {
            PlanetAPI planet = shroomCandidates.get(new Random().nextInt(shroomCandidates.size()));
            LOG.info("Adding FUNGUS condition to " + planet.getName() + " in " + planet.getStarSystem().getName());
            AddConditionIfNeeded(planet, "US_shrooms");
            LOG.info(" ");
            countConditions("Psychoactive Fungus");

            // Setup for storm planet pick
            shroomCandidates.remove(planet);
        }

        // Virus placement
        if (!virusCandidates.isEmpty()) {
            PlanetAPI planet = virusCandidates.get(new Random().nextInt(virusCandidates.size()));
            LOG.info("Adding VIRUS condition to " + planet.getName() + " in " + planet.getStarSystem().getName());
            AddConditionIfNeeded(planet, "US_virus");
            LOG.info(" ");
            countConditions("Military Virus");

            // Add ruins if needed (at least widespread)
            RemoveConditionIfNeeded(planet, Conditions.RUINS_SCATTERED);
            if (!planet.getMarket().hasCondition(Conditions.RUINS_EXTENSIVE) && !planet.getMarket().hasCondition(Conditions.RUINS_VAST) && !planet.getMarket().hasCondition(Conditions.RUINS_WIDESPREAD)) {
                AddConditionIfNeeded(planet, Conditions.RUINS_EXTENSIVE);
            }
        }

        // Storm swap
        if (!shroomCandidates.isEmpty()) {
            PlanetAPI planet = shroomCandidates.get(new Random().nextInt(shroomCandidates.size()));
            LOG.info("Changing planet " + planet.getName() + " in " + planet.getStarSystem().getName() + " to STROM type.");
            ChangePlanetType(planet, "US_storm");
            planet.setTypeId("US_storm");
            AddConditionIfNeeded(planet, "US_storm");
            RemoveConditionIfNeeded(planet, "extreme_weather");
            RemoveConditionIfNeeded(planet, "mild_climate");
            LOG.info(" ");
            countConditions("Perpetual Dust Storm");
        }

        // Magnetic swap
        if (!sporeCandidates.isEmpty()) {
            PlanetAPI planet = sporeCandidates.get(new Random().nextInt(sporeCandidates.size()));
            LOG.info("Changing planet " + planet.getName() + " in " + planet.getStarSystem().getName() + " to MAGNETIC type.");
            ChangePlanetType(planet, "US_magnetic");
            planet.setTypeId("US_magnetic");
            AddConditionIfNeeded(planet, "US_magnetic");
            SectorEntityToken magField = planet.getStarSystem().addTerrain(Terrain.MAGNETIC_FIELD, new MagneticFieldTerrainPlugin.MagneticFieldParams(80, // terrain effect band width
                    planet.getRadius() + 50, // terrain effect middle radius
                    planet, // entity that it's around
                    planet.getRadius(), // visual band start
                    planet.getRadius() + 110, // visual band end
                    new Color(50, 175, 200, 100), // base color
                    0.25f, // probability to spawn aurora sequence, checked once/day when no aurora in progress
                    new Color(25, 250, 100, 150)));
            magField.setCircularOrbit(planet, 0, 0, 100);
            LOG.info(" ");
            countConditions("Magnetic Crust");
        }

        // Artificial swap
        if (!artificialCandidates.isEmpty()) {
            PlanetAPI planet = artificialCandidates.get(new Random().nextInt(artificialCandidates.size()));
            LOG.info("Changing planet " + planet.getName() + " in " + planet.getStarSystem().getName() + " to ARTIFICIAL type.");
            ChangePlanetType(planet, "US_artificial");
            planet.setTypeId("US_artificial");
            AddConditionIfNeeded(planet, "US_artificial");
            LOG.info(" ");
            countConditions("Artificial");

            // Add ruins if needed (at least extensive)
            RemoveConditionIfNeeded(planet, Conditions.RUINS_SCATTERED);
            RemoveConditionIfNeeded(planet, Conditions.RUINS_WIDESPREAD);
            if (!planet.getMarket().hasCondition(Conditions.RUINS_EXTENSIVE) && !planet.getMarket().hasCondition(Conditions.RUINS_VAST)) {
                AddConditionIfNeeded(planet, Conditions.RUINS_EXTENSIVE);
            }
        }

        // Print out sector content
        LOG.info("_______________");
        LOG.info("PLANET TYPES:");
        LOG.info(" ");
        for (String p : PLANET_TYPES.keySet()) {
            LOG.info(p + " : " + PLANET_TYPES.get(p));
            LOG.info(PLANET_TYPES.get(p) * 100 / planets + " percent");
            LOG.info(" ");

        }
        LOG.info(" ");
        LOG.info(planets + " planets in total.");
        LOG.info("_______________");

        LOG.info("Special Conditions:");
        LOG.info(" ");
        for (String p : SPECIAL_CONDITIONS.keySet()) {
            LOG.info(p + " : " + SPECIAL_CONDITIONS.get(p));
            LOG.info(" ");
        }
        LOG.info("_______________");

        // Cleanup
        BG_YOUNG.clear();
        BG_AVERAGE.clear();
        BG_OLD.clear();
        NEB_YOUNG.clear();
        NEB_AVERAGE.clear();
        NEB_OLD.clear();
        CRYSTAL_LIST.clear();
        SPORE_LIST.clear();
        SHROOM_LIST.clear();
        VIRUS_LIST.clear();
        ARTIFICIAL_LIST.clear();
        PLANET_TYPES.clear();
        SPECIAL_CONDITIONS.clear();
    }

    private void countConditions(String condition) {
        if (SPECIAL_CONDITIONS.containsKey(condition)) {
            SPECIAL_CONDITIONS.put(condition, SPECIAL_CONDITIONS.get(condition) + 1);
        } else {
            SPECIAL_CONDITIONS.put(condition, 1);
        }
    }

    private void AddRandomConditionIfNeeded(PlanetAPI p, List<String> toCheck, WeightedRandomPicker<String> picker) {
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

    private void AddConditionIfNeeded(PlanetAPI p, String toAdd) {
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

    private void RemoveConditionIfNeeded(PlanetAPI p, String toRemove) {
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

    public void ChangePlanetType(PlanetAPI planet, String newType) {
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

    private void BGReplacement(StarSystemAPI system) {
        if (system.getConstellation().getType() == ConstellationType.NORMAL) {
            // Regular systems get all the backgrounds
            StarAge a = system.getConstellation().getAge();
            switch (a) {
                case YOUNG: {
                    system.setBackgroundTextureFilename(BG_YOUNG.get(new Random().nextInt(BG_YOUNG.size())));
                    break;
                }
                case AVERAGE: {
                    system.setBackgroundTextureFilename(BG_AVERAGE.get(new Random().nextInt(BG_AVERAGE.size())));
                    break;
                }
                case OLD: {
                    system.setBackgroundTextureFilename(BG_OLD.get(new Random().nextInt(BG_OLD.size())));
                    break;
                }
                default:
            }
        } else {
            // Nebula systems only get dense cloudy backgrounds
            StarAge a = system.getConstellation().getAge();
            switch (a) {
                case YOUNG: {
                    system.setBackgroundTextureFilename(NEB_YOUNG.get(new Random().nextInt(NEB_YOUNG.size())));
                    break;
                }
                case AVERAGE: {
                    system.setBackgroundTextureFilename(NEB_AVERAGE.get(new Random().nextInt(NEB_AVERAGE.size())));
                    break;
                }
                case OLD: {
                    system.setBackgroundTextureFilename(NEB_OLD.get(new Random().nextInt(NEB_OLD.size())));
                    break;
                }
                default:
            }
        }
    }

    // Read data from modSettings.json
    private final String YOUNG = "YOUNG", AVERAGE = "AVERAGE", OLD = "OLD", YOUNGAVERAGE = "YOUNGAVERAGE", AVERAGEOLD = "AVERAGEOLD", ALL = "ALL";
    private final String CRYSTAL = "crystal_types", SPORE = "spore_type", SHROOM = "shroom_type", VIRUS = "virus_type", ARTIFICIAL = "artificial_type";

    private void getData() {
        // Merge the modSettings.json files
        JSONObject modSettings = mergeModSettings();
        if (modSettings == null) return;

        String modId = "unknownSkies";
        String bg = "backgrounds";

        // Get the background map of <path> : <age>
        Map<String, String> BGmap = getMap(modSettings, modId, bg);

        // Sort the background paths
        for (Entry<String, String> entry : BGmap.entrySet()) {
            switch (entry.getValue()) {
                case YOUNG:
                    BG_YOUNG.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_YOUNG.add(entry.getKey());
                    }
                    break;
                case AVERAGE:
                    BG_AVERAGE.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_AVERAGE.add(entry.getKey());
                    }
                    break;
                case OLD:
                    BG_OLD.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_OLD.add(entry.getKey());
                    }
                    break;
                case YOUNGAVERAGE:
                    BG_YOUNG.add(entry.getKey());
                    BG_AVERAGE.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_YOUNG.add(entry.getKey());
                        NEB_AVERAGE.add(entry.getKey());
                    }
                    break;
                case AVERAGEOLD:
                    BG_AVERAGE.add(entry.getKey());
                    BG_OLD.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_AVERAGE.add(entry.getKey());
                        NEB_OLD.add(entry.getKey());
                    }
                    break;
                case ALL:
                    BG_YOUNG.add(entry.getKey());
                    BG_AVERAGE.add(entry.getKey());
                    BG_OLD.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_YOUNG.add(entry.getKey());
                        NEB_AVERAGE.add(entry.getKey());
                        NEB_OLD.add(entry.getKey());
                    }
                    break;
            }
        }

        // Get the planet type list for the special condition/planets
        CRYSTAL_LIST = getList(modSettings, modId, CRYSTAL);
        SPORE_LIST = getList(modSettings, modId, SPORE);
        SHROOM_LIST = getList(modSettings, modId, SHROOM);
        VIRUS_LIST = getList(modSettings, modId, VIRUS);
        ARTIFICIAL_LIST = getList(modSettings, modId, ARTIFICIAL);
    }

    private JSONObject mergeModSettings() {
        // Merge the modSettings.json files
        JSONObject modSettings = null;
        try {
            modSettings = Global.getSettings().getMergedJSONForMod("data/config/modSettings.json", "US");
        } catch (IOException | JSONException ex) {
            LOG.fatal("unable to read modSettings.json", ex);
        }
        return modSettings;
    }

    private Map<String, String> getMap(JSONObject modSettings, String modId, String id) {
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
