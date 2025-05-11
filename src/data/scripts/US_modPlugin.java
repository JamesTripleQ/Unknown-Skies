package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.abilities.GenerateSlipsurgeAbility;
import com.fs.starfarer.api.impl.campaign.econ.impl.Farming;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation.ConstellationType;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.terrain.MagneticFieldTerrainPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicSettings;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

import static com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator.PK_PLANET_KEY;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator.PLANETARY_SHIELD_PLANET;
import static com.fs.starfarer.api.impl.codex.CodexDataV2.*;
import static data.scripts.util.US_hyceanManager.manageHyceanConditions;
import static data.scripts.util.US_manualSystemFixer.US_SKIP_SYSTEM;
import static data.scripts.util.US_manualSystemFixer.fixSystem;
import static data.scripts.util.US_utils.*;

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
    private List<String> CRYOSANCTUM_LIST = new ArrayList<>();
    private List<String> ARTIFICIAL_LIST = new ArrayList<>();
    private List<String> FLUORESCENT_LIST = new ArrayList<>();
    private List<String> SAKURA_LIST = new ArrayList<>();

    private boolean ALWAYS_SPAWN_SAKURA = false;

    private static final List<Color> artificialLights = new ArrayList<>();

    public static final WeightedRandomPicker<String> FLOATING_CONTINENT_RUINS = new WeightedRandomPicker<>();
    public static final WeightedRandomPicker<String> METHANE_ORGANICS = new WeightedRandomPicker<>();

    static {
        artificialLights.add(new Color(35, 194, 236));
        artificialLights.add(new Color(53, 233, 49));
        artificialLights.add(new Color(233, 66, 49));
        artificialLights.add(new Color(162, 37, 211));
        artificialLights.add(new Color(233, 228, 84));

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
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_gas_giant", 0f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_gas_giantB", 0f);
        GenerateSlipsurgeAbility.SLIPSURGE_STRENGTH.put("US_fluorescent", 0f);
    }

    @Override
    public void onCodexDataGenerated() {
        // Chemical Crystals
        makeRelated(getConditionEntryId("US_crystals"), getPlanetEntryId("toxic"));
        makeRelated(getConditionEntryId("US_crystals"), getPlanetEntryId("toxic_cold"));
        makeRelated(getConditionEntryId("US_crystals"), getPlanetEntryId("US_acid"));
        makeRelated(getConditionEntryId("US_crystals"), getPlanetEntryId("US_acidRain"));
        makeRelated(getConditionEntryId("US_crystals"), getPlanetEntryId("US_acidWind"));
        makeRelated(getConditionEntryId("US_crystals"), getPlanetEntryId("US_green"));
        makeRelated(getConditionEntryId("US_crystals"), getIndustryEntryId(Industries.LIGHTINDUSTRY));
        makeRelated(getConditionEntryId("US_crystals"), getCommodityEntryId(Commodities.LUXURY_GOODS));
        // Military Virus
        makeRelated(getConditionEntryId("US_virus"), getPlanetEntryId("barren-desert"));
        makeRelated(getConditionEntryId("US_virus"), getPlanetEntryId("desert"));
        makeRelated(getConditionEntryId("US_virus"), getPlanetEntryId("desert1"));
        makeRelated(getConditionEntryId("US_virus"), getPlanetEntryId("US_desertA"));
        makeRelated(getConditionEntryId("US_virus"), getPlanetEntryId("US_desertB"));
        makeRelated(getConditionEntryId("US_virus"), getPlanetEntryId("US_desertC"));
        // Parasitic Spores
        makeRelated(getConditionEntryId("US_mind"), getPlanetEntryId("jungle"));
        makeRelated(getConditionEntryId("US_mind"), getPlanetEntryId("US_jungle"));
        makeRelated(getConditionEntryId("US_mind"), getPlanetEntryId("US_auric"));
        makeRelated(getConditionEntryId("US_mind"), getPlanetEntryId("US_auricCloudy"));
        makeRelated(getConditionEntryId("US_mind"), getPlanetEntryId("US_savannah"));
        // Psychoactive Fungus
        makeRelated(getConditionEntryId("US_shrooms"), getPlanetEntryId("arid"));
        makeRelated(getConditionEntryId("US_shrooms"), getPlanetEntryId("US_arid"));
        makeRelated(getConditionEntryId("US_shrooms"), getPlanetEntryId("tundra"));
        makeRelated(getConditionEntryId("US_shrooms"), getPlanetEntryId("US_alkali"));
        makeRelated(getConditionEntryId("US_shrooms"), getPlanetEntryId("US_alpine"));
        // Pre-Collapse Cryosanctum
        makeRelated(getConditionEntryId("US_cryosanctum"), getPlanetEntryId("frozen"));
        makeRelated(getConditionEntryId("US_cryosanctum"), getPlanetEntryId("frozen1"));
        makeRelated(getConditionEntryId("US_cryosanctum"), getPlanetEntryId("frozen2"));
        makeRelated(getConditionEntryId("US_cryosanctum"), getPlanetEntryId("frozen3"));
        makeRelated(getConditionEntryId("US_cryosanctum"), getPlanetEntryId("US_iceA"));
        makeRelated(getConditionEntryId("US_cryosanctum"), getPlanetEntryId("US_iceB"));
        // Unique planets
        makeRelated(getConditionEntryId("US_magnetic"), getPlanetEntryId("US_magnetic"));
        makeRelated(getConditionEntryId("US_artificial"), getPlanetEntryId("US_artificial"));
        makeRelated(getConditionEntryId("US_storm"), getPlanetEntryId("US_storm"));
        makeRelated(getConditionEntryId("US_fluorescent"), getPlanetEntryId("US_fluorescent"));
        makeRelated(getConditionEntryId("US_sakura"), getPlanetEntryId("US_sakura"));
        // Water planets
        makeRelated(getConditionEntryId("US_hybrid"), getPlanetEntryId("US_water"));
        makeRelated(getConditionEntryId("US_hybrid"), getPlanetEntryId("US_waterB"));
        makeRelated(getConditionEntryId(Conditions.WATER_SURFACE), getPlanetEntryId("US_water"));
        makeRelated(getIndustryEntryId(Industries.AQUACULTURE), getPlanetEntryId("US_water"));
        makeRelated(getConditionEntryId(Conditions.WATER_SURFACE), getPlanetEntryId("US_waterB"));
        makeRelated(getIndustryEntryId(Industries.AQUACULTURE), getPlanetEntryId("US_waterB"));
        makeRelated(getConditionEntryId(Conditions.WATER_SURFACE), getPlanetEntryId("US_waterHycean"));
        makeRelated(getIndustryEntryId(Industries.AQUACULTURE), getPlanetEntryId("US_waterHycean"));
    }

    @Override
    public void onNewGameAfterProcGen() {
        LOG.info("Unknown Skies onNewGameAfterProcGen() START");

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

        List<PlanetAPI> sporeCandidates = new ArrayList<>();
        List<PlanetAPI> shroomCandidates = new ArrayList<>();
        List<PlanetAPI> virusCandidates = new ArrayList<>();
        List<PlanetAPI> cryosanctumCandidates = new ArrayList<>();
        List<PlanetAPI> artificialCandidates = new ArrayList<>();
        List<PlanetAPI> fluorescentCandidates = new ArrayList<>();
        List<PlanetAPI> sakuraCandidates = new ArrayList<>();

        // Seed planetary conditions
        for (StarSystemAPI s : Global.getSector().getStarSystems()) {
            if (s == null) continue;
            if (!s.isProcgen()) {
                if (!s.hasTag(US_SKIP_SYSTEM)) {
                    fixSystem(s, true, true);
                }
                continue;
            }
            if (s.getPlanets().isEmpty()) continue;

            swapStar(s.getStar());
            swapStar(s.getSecondary());
            swapStar(s.getTertiary());

            for (PlanetAPI p : s.getPlanets()) {
                if (p.isStar()) continue;

                // Swap lava to US_lava
                if (p.getTypeId().equals("lava")) {
                    if (new Random().nextBoolean()) {
                        changePlanetType(p, "US_lava");
                    }
                }

                // Swap lava_minor to US_volcanic
                if (p.getTypeId().equals("lava_minor")) {
                    if (new Random().nextBoolean()) {
                        changePlanetType(p, "US_volcanic");
                    }
                }

                // Swap terran to US_continental or US_terran
                if (p.getTypeId().equals("terran")) {
                    switch (new Random().nextInt(3)) {
                        case 0:
                            changePlanetType(p, "US_continent");
                            break;
                        case 1:
                            changePlanetType(p, "US_terran");
                            break;
                    }
                }

                // Swap tundra to US_alkali or US_alpine (except Sentinel)
                if (p.getTypeId().equals("tundra") && !p.getMemoryWithoutUpdate().getBoolean(PK_PLANET_KEY)) {
                    switch (new Random().nextInt(3)) {
                        case 0:
                            changePlanetType(p, "US_alkali");
                            break;
                        case 1:
                            changePlanetType(p, "US_alpine");
                            break;
                    }
                }

                // Swap jungle to US_jungle or US_savannah
                if (p.getTypeId().equals("jungle")) {
                    switch (new Random().nextInt(3)) {
                        case 0:
                            changePlanetType(p, "US_jungle");
                            break;
                        case 1:
                            changePlanetType(p, "US_savannah");
                            break;
                    }
                }

                // Swap arid to US_auric or US_auricCloudy (balanced out to have 50% arid and 50% US_auric/US_auricCloudy)
                if (p.getTypeId().equals("arid")) {
                    if (new Random().nextBoolean()) {
                        if (new Random().nextBoolean()) {
                            changePlanetType(p, "US_auric");
                        } else {
                            changePlanetType(p, "US_auricCloudy");
                        }
                    }
                }

                // Add Ruins to planets with Floating Continent
                if (p.getMarket().hasCondition("US_floating")) {
                    addConditionIfNeeded(p, FLOATING_CONTINENT_RUINS.pick());
                    if (new Random().nextInt(15) == 0) {
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

                // Add Thin Atmosphere to Dust planets
                if (p.getTypeId().equals("US_dust")) {
                    addConditionIfNeeded(p, Conditions.THIN_ATMOSPHERE);
                }

                // Remove Inimical Biosphere from Lifeless and Lifeless-Bombarded planets
                if (p.getTypeId().equals("US_lifelessArid") || p.getTypeId().equals("US_lifeless") || p.getTypeId().equals("US_crimson")) {
                    removeConditionIfNeeded(p, Conditions.INIMICAL_BIOSPHERE);
                }

                // Add Hybrid Production to Archipelago planets
                if (p.getTypeId().equals("US_water") || p.getTypeId().equals("US_waterB")) {
                    addConditionIfNeeded(p, "US_hybrid");
                }

                // Hycean planets are handled in US_hyceanManager.java
                if (p.getTypeId().equals("US_waterHycean")) {
                    manageHyceanConditions(p);
                }

                // Add Chemical Crystals to the appropriate planets
                if (CRYSTAL_LIST.contains(p.getTypeId())) {
                    if (new Random().nextInt(4) == 0) {
                        addConditionIfNeeded(p, "US_crystals");
                    }
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
                    } else if (CRYOSANCTUM_LIST.contains(p.getTypeId())) {
                        cryosanctumCandidates.add(p);
                    } else if (ARTIFICIAL_LIST.contains(p.getTypeId())) {
                        if (p.getStarSystem().hasTag(Tags.THEME_DERELICT) || p.getStarSystem().hasTag(Tags.THEME_RUINS)) {
                            artificialCandidates.add(p);
                        }
                    } else if (FLUORESCENT_LIST.contains(p.getTypeId())) {
                        fluorescentCandidates.add(p);
                    } else if (SAKURA_LIST.contains(p.getTypeId())) {
                        sakuraCandidates.add(p);
                    }
                }
            }
        }

        // Spore placement
        if (!sporeCandidates.isEmpty()) {
            PlanetAPI planet = sporeCandidates.get(new Random().nextInt(sporeCandidates.size()));
            LOG.info("Adding Parasitic Spores to " + planet.getName() + " in " + planet.getStarSystem().getName());
            addConditionIfNeeded(planet, "US_mind");
            addConditionIfNeeded(planet, "US_unique_filter");

            // Setup for future picks
            sporeCandidates.remove(planet);
        }

        // Fungus placement
        if (!shroomCandidates.isEmpty()) {
            PlanetAPI planet = shroomCandidates.get(new Random().nextInt(shroomCandidates.size()));
            LOG.info("Adding Psychoactive Fungus to " + planet.getName() + " in " + planet.getStarSystem().getName());
            addConditionIfNeeded(planet, "US_shrooms");
            addConditionIfNeeded(planet, "US_unique_filter");

            // Setup for future picks
            shroomCandidates.remove(planet);
        }

        // Virus placement
        if (!virusCandidates.isEmpty()) {
            PlanetAPI planet = virusCandidates.get(new Random().nextInt(virusCandidates.size()));
            LOG.info("Adding Military Virus to " + planet.getName() + " in " + planet.getStarSystem().getName());
            addConditionIfNeeded(planet, "US_virus");
            addConditionIfNeeded(planet, "US_unique_filter");

            // Add ruins if needed (at least widespread)
            if (!planet.getMarket().hasCondition(Conditions.RUINS_VAST) && !planet.getMarket().hasCondition(Conditions.RUINS_WIDESPREAD)) {
                addConditionIfNeeded(planet, Conditions.RUINS_EXTENSIVE);
            }

            // Setup for future picks
            virusCandidates.remove(planet);
        }

        // Cryosanctum placement
        if (!cryosanctumCandidates.isEmpty()) {
            PlanetAPI planet = cryosanctumCandidates.get(new Random().nextInt(cryosanctumCandidates.size()));
            LOG.info("Adding Pre-Collapse Cryosanctum to " + planet.getName() + " in " + planet.getStarSystem().getName());
            addConditionIfNeeded(planet, "US_cryosanctum");
            addConditionIfNeeded(planet, "US_unique_filter");
            addConditionIfNeeded(planet, Conditions.POLLUTION);

            // Nerf ores if needed (at most abundant)
            if (planet.getMarket().hasCondition(Conditions.ORE_ULTRARICH)) {
                addConditionIfNeeded(planet, Conditions.ORE_ABUNDANT);
            } else if (planet.getMarket().hasCondition(Conditions.ORE_RICH)) {
                addConditionIfNeeded(planet, Conditions.ORE_MODERATE);
            }

            // Nerf rare ores if needed (at most moderate)
            removeConditionIfNeeded(planet, Conditions.RARE_ORE_SPARSE);
            if (planet.getMarket().hasCondition(Conditions.RARE_ORE_ULTRARICH) || planet.getMarket().hasCondition(Conditions.RARE_ORE_RICH)) {
                addConditionIfNeeded(planet, Conditions.RARE_ORE_MODERATE);
            } else if (planet.getMarket().hasCondition(Conditions.RARE_ORE_ABUNDANT) || planet.getMarket().hasCondition(Conditions.RARE_ORE_MODERATE)) {
                addConditionIfNeeded(planet, Conditions.RARE_ORE_SPARSE);
            }

            // Add ruins if needed (at least extensive)
            if (!planet.getMarket().hasCondition(Conditions.RUINS_VAST)) {
                addConditionIfNeeded(planet, Conditions.RUINS_EXTENSIVE);
            }

            // Setup for future picks
            cryosanctumCandidates.remove(planet);
        }

        // Windswept swap
        if (!shroomCandidates.isEmpty()) {
            PlanetAPI planet = shroomCandidates.get(new Random().nextInt(shroomCandidates.size()));
            LOG.info("Changing " + planet.getName() + " in " + planet.getStarSystem().getName() + " to Windswept");
            changePlanetType(planet, "US_storm", true);
            addConditionIfNeeded(planet, "US_storm");
            addConditionIfNeeded(planet, "US_unique_filter");
            removeConditionIfNeeded(planet, Conditions.NO_ATMOSPHERE);
            removeConditionIfNeeded(planet, Conditions.EXTREME_WEATHER);
            removeConditionIfNeeded(planet, Conditions.MILD_CLIMATE);

            // Setup for future picks
            shroomCandidates.remove(planet);
        }

        // Magnetic swap
        if (!sporeCandidates.isEmpty()) {
            PlanetAPI planet = sporeCandidates.get(new Random().nextInt(sporeCandidates.size()));
            LOG.info("Changing " + planet.getName() + " in " + planet.getStarSystem().getName() + " to Magnetic");
            changePlanetType(planet, "US_magnetic", true);
            addConditionIfNeeded(planet, "US_magnetic");
            addConditionIfNeeded(planet, "US_unique_filter");
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
            LOG.info("Changing " + planet.getName() + " in " + planet.getStarSystem().getName() + " to Artificial");
            changePlanetType(planet, "US_artificial", true);
            planet.getSpec().setGlowColor(artificialLights.get(new Random().nextInt(artificialLights.size())));
            planet.applySpecChanges();
            addConditionIfNeeded(planet, "US_artificial");
            addConditionIfNeeded(planet, "US_unique_filter");

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
            LOG.info("Changing " + planet.getName() + " in " + planet.getStarSystem().getName() + " to Fluorescent Giant");
            changePlanetType(planet, "US_fluorescent", true);
            addConditionIfNeeded(planet, "US_fluorescent");
            addConditionIfNeeded(planet, "US_unique_filter");
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

        // Sakura swap
        if (!sakuraCandidates.isEmpty() && (sakuraCandidates.size() >= 3 || ALWAYS_SPAWN_SAKURA)) {
            PlanetAPI planet = sakuraCandidates.get(new Random().nextInt(sakuraCandidates.size()));
            LOG.info("Changing " + planet.getName() + " in " + planet.getStarSystem().getName() + " to Sakura");
            changePlanetType(planet, "US_sakura", true);
            addConditionIfNeeded(planet, "US_sakura");
            addConditionIfNeeded(planet, "US_unique_filter");

            // Setup for future picks
            sakuraCandidates.remove(planet);
        }

        cleanupSettings();
        LOG.info("Unknown Skies onNewGameAfterProcGen() END");
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
        CRYOSANCTUM_LIST = MagicSettings.getList(modId, "cryosanctum_type");
        ARTIFICIAL_LIST = MagicSettings.getList(modId, "artificial_type");
        FLUORESCENT_LIST = MagicSettings.getList(modId, "fluorescent_type");
        SAKURA_LIST = MagicSettings.getList(modId, "sakura_type");

        ALWAYS_SPAWN_SAKURA = MagicSettings.getBoolean(modId, "alwaysSpawnSakura");
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
        CRYOSANCTUM_LIST.clear();
        ARTIFICIAL_LIST.clear();
        FLUORESCENT_LIST.clear();
        SAKURA_LIST.clear();
    }
}
