package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.abilities.GenerateSlipsurgeAbility;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation.ConstellationType;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator.PK_PLANET_KEY;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator.PLANETARY_SHIELD_PLANET;
import static data.scripts.util.US_hyceanManager.manageHyceanConditions;
import static data.scripts.util.US_utils.addConditionIfNeeded;
import static data.scripts.util.US_utils.removeConditionIfNeeded;

@SuppressWarnings("unused")
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
    private List<String> FLUORESCENT_LIST = new ArrayList<>();

    private static final WeightedRandomPicker<String> FLOATING_CONTINENT_RUINS = new WeightedRandomPicker<>();
    private static final WeightedRandomPicker<String> METHANE_ORGANICS = new WeightedRandomPicker<>();

    static {
        FLOATING_CONTINENT_RUINS.add(Conditions.RUINS_SCATTERED, 1);
        FLOATING_CONTINENT_RUINS.add(Conditions.RUINS_WIDESPREAD, 2);
        FLOATING_CONTINENT_RUINS.add(Conditions.RUINS_EXTENSIVE, 3);
        FLOATING_CONTINENT_RUINS.add(Conditions.RUINS_VAST, 1.5f);

        METHANE_ORGANICS.add(Conditions.ORGANICS_COMMON, 5f);
        METHANE_ORGANICS.add(Conditions.ORGANICS_ABUNDANT, 20f);
        METHANE_ORGANICS.add(Conditions.ORGANICS_PLENTIFUL, 10f);
    }

    @Override
    public void onApplicationLoad() {
        // Set aquaculture planets
        Farming.AQUA_PLANETS.add("US_water");
        Farming.AQUA_PLANETS.add("US_waterB");
        Farming.AQUA_PLANETS.add("US_waterHycean");

        // Set Slipsurge strength
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_star_red_giant", 0.6f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_star_blue_giant", 0.6f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_star_orange", 0.4f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_star_yellow", 0.4f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_star_white", 0.25f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_star_browndwarf", 0.1f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_gas_giant", 0f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_gas_giantB", 0f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_fluorescent", 0f);
    }

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
                    loadSettings();
                    replaceBackground(s);
                    cleanupSettings();
                }
            }
        }
    }

    @Override
    public void onNewGameAfterProcGen() {
        // Read the background list
        if (BG_YOUNG.isEmpty()) {
            loadSettings();
        }

        // Replace backgrounds
        for (StarSystemAPI s : Global.getSector().getStarSystems()) {
            if (s != null && s.isProcgen() && s.getConstellation() != null) {
                replaceBackground(s);
            }
        }

        List<PlanetAPI> crystalCandidates = new ArrayList<>();
        List<PlanetAPI> sporeCandidates = new ArrayList<>();
        List<PlanetAPI> shroomCandidates = new ArrayList<>();
        List<PlanetAPI> virusCandidates = new ArrayList<>();
        List<PlanetAPI> artificialCandidates = new ArrayList<>();
        List<PlanetAPI> fluorescentCandidates = new ArrayList<>();

        // Seed planetary conditions
        for (StarSystemAPI s : Global.getSector().getStarSystems()) {
            if (s == null) continue;
            if (!s.isProcgen()) continue;
            if (s.getPlanets().isEmpty()) continue;

            for (PlanetAPI p : s.getPlanets()) {
                if (p.isStar()) continue;

                // Add Ruins to planets with Floating Continent
                if (p.getMarket().hasCondition("US_floating")) {
                    addConditionIfNeeded(p, FLOATING_CONTINENT_RUINS.pick());
                    if (Math.random() > 0.875f) {
                        addConditionIfNeeded(p, Conditions.DECIVILIZED);
                    }
                }

                // Add Organics to Methane planets
                if (p.getTypeId().equals("US_purple")) {
                    addConditionIfNeeded(p, METHANE_ORGANICS.pick());
                }

                // Add Irradiated to Burnt planets
                if (p.getTypeId().equals("US_burnt")) {
                    addConditionIfNeeded(p, Conditions.IRRADIATED);
                }

                // Hycean planets are handled in US_hyceanManager.java
                if (p.getTypeId().equals("US_waterHycean")) {
                    manageHyceanConditions(p);
                }

                // Find Chemical Crystals candidates
                if (CRYSTAL_LIST.contains(p.getTypeId())) {
                    crystalCandidates.add(p);
                }

                // Find unique condition candidates
                if (!p.getStarSystem().isDeepSpace() && !(p.getMemoryWithoutUpdate().getBoolean(PLANETARY_SHIELD_PLANET) ||
                        p.getMemoryWithoutUpdate().getBoolean(PK_PLANET_KEY) || p.hasCondition(Conditions.SOLAR_ARRAY))) {
                    if (SPORE_LIST.contains(p.getTypeId())) {
                        sporeCandidates.add(p);
                    } else if (SHROOM_LIST.contains(p.getTypeId())) {
                        shroomCandidates.add(p);
                    } else if (VIRUS_LIST.contains(p.getTypeId())) {
                        virusCandidates.add(p);
                    } else if (ARTIFICIAL_LIST.contains(p.getTypeId())) {
                        if (p.getStarSystem().hasTag(Tags.THEME_DERELICT) || p.getStarSystem().hasTag(Tags.THEME_RUINS)) {
                            artificialCandidates.add(p);
                        }
                    } else if (FLUORESCENT_LIST.contains(p.getTypeId())) {
                        fluorescentCandidates.add(p);
                    }
                }
            }
        }

        // Crystal placement
        if (!crystalCandidates.isEmpty()) {
            for (PlanetAPI planet : crystalCandidates) {
                if (Math.random() > 0.75f) {
                    LOG.info("Adding Chemical Crystals to " + planet.getName() + " in " + planet.getStarSystem().getName());
                    addConditionIfNeeded(planet, "US_crystals");
                }
            }
        }

        // Spore placement
        if (!sporeCandidates.isEmpty()) {
            PlanetAPI planet = sporeCandidates.get(new Random().nextInt(sporeCandidates.size()));
            LOG.info("Adding Parasitic Spores to " + planet.getName() + " in " + planet.getStarSystem().getName());
            addConditionIfNeeded(planet, "US_mind");

            // Setup for future picks
            sporeCandidates.remove(planet);
        }

        // Fungus placement
        if (!shroomCandidates.isEmpty()) {
            PlanetAPI planet = shroomCandidates.get(new Random().nextInt(shroomCandidates.size()));
            LOG.info("Adding Psychoactive Fungus to " + planet.getName() + " in " + planet.getStarSystem().getName());
            addConditionIfNeeded(planet, "US_shrooms");

            // Setup for future picks
            shroomCandidates.remove(planet);
        }

        // Virus placement
        if (!virusCandidates.isEmpty()) {
            PlanetAPI planet = virusCandidates.get(new Random().nextInt(virusCandidates.size()));
            LOG.info("Adding Military Virus to " + planet.getName() + " in " + planet.getStarSystem().getName());
            addConditionIfNeeded(planet, "US_virus");

            // Add ruins if needed (at least widespread)
            if (!planet.getMarket().hasCondition(Conditions.RUINS_VAST) && !planet.getMarket().hasCondition(Conditions.RUINS_WIDESPREAD)) {
                addConditionIfNeeded(planet, Conditions.RUINS_EXTENSIVE);
            }

            // Setup for future picks
            virusCandidates.remove(planet);
        }

        // Windswept swap
        if (!shroomCandidates.isEmpty()) {
            PlanetAPI planet = shroomCandidates.get(new Random().nextInt(shroomCandidates.size()));
            LOG.info("Changing planet " + planet.getName() + " in " + planet.getStarSystem().getName() + " to Windswept");
            planet.changeType("US_storm", StarSystemGenerator.random);
            addConditionIfNeeded(planet, "US_storm");
            removeConditionIfNeeded(planet, Conditions.NO_ATMOSPHERE);
            removeConditionIfNeeded(planet, Conditions.EXTREME_WEATHER);
            removeConditionIfNeeded(planet, Conditions.MILD_CLIMATE);

            // Setup for future picks
            shroomCandidates.remove(planet);
        }

        // Magnetic swap
        if (!sporeCandidates.isEmpty()) {
            PlanetAPI planet = sporeCandidates.get(new Random().nextInt(sporeCandidates.size()));
            LOG.info("Changing planet " + planet.getName() + " in " + planet.getStarSystem().getName() + " to Magnetic");
            planet.changeType("US_magnetic", StarSystemGenerator.random);
            addConditionIfNeeded(planet, "US_magnetic");
            SectorEntityToken magField = planet.getStarSystem().addTerrain(
                    Terrain.MAGNETIC_FIELD,
                    new MagneticFieldTerrainPlugin.MagneticFieldParams(
                            80,
                            planet.getRadius() + 50,
                            planet,
                            planet.getRadius(),
                            planet.getRadius() + 110,
                            new Color(50, 175, 200, 100),
                            0.25f,
                            new Color(25, 250, 100, 150)
                    )
            );
            magField.setCircularOrbit(planet, 0, 0, 100);

            // Setup for future picks
            sporeCandidates.remove(planet);
        }

        // Artificial swap
        if (!artificialCandidates.isEmpty()) {
            PlanetAPI planet = artificialCandidates.get(new Random().nextInt(artificialCandidates.size()));
            LOG.info("Changing planet " + planet.getName() + " in " + planet.getStarSystem().getName() + " to Artificial");
            planet.changeType("US_artificial", StarSystemGenerator.random);
            addConditionIfNeeded(planet, "US_artificial");

            // Add ruins if needed (at least extensive)
            if (!planet.getMarket().hasCondition(Conditions.RUINS_VAST)) {
                addConditionIfNeeded(planet, Conditions.RUINS_EXTENSIVE);
            }

            // Setup for future picks
            artificialCandidates.remove(planet);
        }

        // Fluorescent swap
        if (!fluorescentCandidates.isEmpty()) {
            PlanetAPI planet = fluorescentCandidates.get(new Random().nextInt(fluorescentCandidates.size()));
            LOG.info("Changing planet " + planet.getName() + " in " + planet.getStarSystem().getName() + " to Fluorescent Giant");
            planet.changeType("US_fluorescent", StarSystemGenerator.random);
            addConditionIfNeeded(planet, "US_fluorescent");
            addConditionIfNeeded(planet, Conditions.EXTREME_WEATHER);
            removeConditionIfNeeded(planet, Conditions.POOR_LIGHT);
            removeConditionIfNeeded(planet, Conditions.DARK);

            // Decrease darkness level of moons by 1
            for (PlanetAPI moon : planet.getStarSystem().getPlanets()) {
                if (!moon.isMoon()) continue;

                if (moon.getOrbitFocus().getId().equals(planet.getId())) {
                    if (moon.hasCondition(Conditions.DARK)) {
                        addConditionIfNeeded(moon, Conditions.POOR_LIGHT);
                    } else {
                        removeConditionIfNeeded(moon, Conditions.POOR_LIGHT);
                    }
                }
            }

            // Upgrade volatiles if needed (at least diffuse)
            if (!planet.getMarket().hasCondition(Conditions.VOLATILES_PLENTIFUL) && !planet.getMarket().hasCondition(Conditions.VOLATILES_ABUNDANT)) {
                addConditionIfNeeded(planet, Conditions.VOLATILES_DIFFUSE);
            }

            // Setup for future picks
            fluorescentCandidates.remove(planet);
        }

        cleanupSettings();
    }

    private void replaceBackground(StarSystemAPI system) {
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
    private void loadSettings() {
        String modId = "unknownSkies";

        // Get the background map of <path> : <age>
        Map<String, String> BGmap = MagicSettings.getStringMap(modId, "backgrounds");

        // Sort the background paths
        for (Entry<String, String> entry : BGmap.entrySet()) {
            switch (entry.getValue()) {
                case "YOUNG":
                    BG_YOUNG.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_YOUNG.add(entry.getKey());
                    }
                    break;
                case "AVERAGE":
                    BG_AVERAGE.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_AVERAGE.add(entry.getKey());
                    }
                    break;
                case "OLD":
                    BG_OLD.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_OLD.add(entry.getKey());
                    }
                    break;
                case "YOUNGAVERAGE":
                    BG_YOUNG.add(entry.getKey());
                    BG_AVERAGE.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_YOUNG.add(entry.getKey());
                        NEB_AVERAGE.add(entry.getKey());
                    }
                    break;
                case "AVERAGEOLD":
                    BG_AVERAGE.add(entry.getKey());
                    BG_OLD.add(entry.getKey());
                    if (entry.getKey().endsWith("n.jpg")) {
                        NEB_AVERAGE.add(entry.getKey());
                        NEB_OLD.add(entry.getKey());
                    }
                    break;
                case "ALL":
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
        CRYSTAL_LIST = MagicSettings.getList(modId, "crystal_types");
        SPORE_LIST = MagicSettings.getList(modId, "spore_type");
        SHROOM_LIST = MagicSettings.getList(modId, "shroom_type");
        VIRUS_LIST = MagicSettings.getList(modId, "virus_type");
        ARTIFICIAL_LIST = MagicSettings.getList(modId, "artificial_type");
        FLUORESCENT_LIST = MagicSettings.getList(modId, "fluorescent_type");
    }

    // Cleanup
    private void cleanupSettings() {
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
        FLUORESCENT_LIST.clear();
    }
}
