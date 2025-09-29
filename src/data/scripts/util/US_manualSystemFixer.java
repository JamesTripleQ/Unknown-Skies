package data.scripts.util;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import static data.scripts.util.US_hyceanManager.manageHyceanConditions;
import static data.scripts.util.US_utils.*;

public class US_manualSystemFixer {
    // This memKey is meant to be used for systems created before or at procgen that have custom designed US planets that don't need fixing
    public static final String US_SKIP_SYSTEM_KEY = "$US_skipSystem";
    // This memKey is meant to prevent individual planets/stars from being fixed, useful if a system is half manually created, half procgen
    public static final String US_SKIP_PLANET_KEY = "$US_skipPlanet";
    // This memKey allows for swapping star variants:
    // 0 (default): no change
    // 1/2/3: swap to the respective variant if possible, else no change
    // -1: swap to random variant
    public static final String US_STAR_VARIANT_KEY = "$US_starVariant";

    public static void fixSystem(StarSystemAPI system, boolean removeHyceanRuins, boolean removeFloatingContinent) {
        if (system == null) return;
        if (system.getPlanets().isEmpty()) return;
        if (system.getMemoryWithoutUpdate().getBoolean(US_SKIP_SYSTEM_KEY)) return;

        for (PlanetAPI planet : system.getPlanets()) {
            if (planet.getMemoryWithoutUpdate().getBoolean(US_SKIP_PLANET_KEY)) continue;

            if (planet.isStar()) {
                swapStarToVariant(planet);
                continue;
            }

            // Add Organics to Methane planets
            if (planet.getTypeId().equals("US_purple")) {
                applyMethaneOrganics(planet);
            }

            // Add Irradiated to Burnt planets
            if (planet.getTypeId().equals("US_burnt")) {
                addConditionIfNeeded(planet, Conditions.IRRADIATED);
            }

            // Add Thin Atmosphere to Dust planets
            if (planet.getTypeId().equals("US_dust")) {
                addConditionIfNeeded(planet, Conditions.THIN_ATMOSPHERE);
            }

            // Remove Inimical Biosphere from Lifeless and Lifeless-Bombarded planets
            if (planet.getTypeId().equals("US_lifelessArid") || planet.getTypeId().equals("US_lifeless") || planet.getTypeId().equals("US_crimson") || planet.getTypeId().equals("US_crimsonB")) {
                removeConditionIfNeeded(planet, Conditions.INIMICAL_BIOSPHERE);
            }

            // Add Hybrid Production to Archipelago planets
            if (planet.getTypeId().equals("US_water") || planet.getTypeId().equals("US_waterB")) {
                addConditionIfNeeded(planet, "US_hybrid");
            }

            // Hycean planets are handled in US_hyceanManager.java
            if (planet.getTypeId().equals("US_waterHycean")) {
                manageHyceanConditions(planet);

                // Remove possible Ruins if required
                if (removeHyceanRuins) {
                    removeConditionIfNeeded(planet, Conditions.RUINS_SCATTERED);
                    removeConditionIfNeeded(planet, Conditions.RUINS_WIDESPREAD);
                    removeConditionIfNeeded(planet, Conditions.RUINS_EXTENSIVE);
                    removeConditionIfNeeded(planet, Conditions.RUINS_VAST);
                    removeConditionIfNeeded(planet, Conditions.DECIVILIZED);
                    removeConditionIfNeeded(planet, "US_religious");
                }
            }

            // Handle Ruins on planets with Floating Continent
            if (planet.getMarket().hasCondition("US_floating")) {
                if (removeFloatingContinent) {
                    removeConditionIfNeeded(planet, "US_floating");
                } else {
                    applyFloatingContinentRuins(planet);
                }
            }
        }

        // Set the memKey to avoid editing the system in US_modPlugin (only affects systems that call this method manually)
        system.getMemoryWithoutUpdate().set(US_SKIP_SYSTEM_KEY, true);
    }
}
