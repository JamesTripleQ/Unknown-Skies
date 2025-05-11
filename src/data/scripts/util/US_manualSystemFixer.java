package data.scripts.util;

import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;

import java.util.Random;

import static data.scripts.US_modPlugin.FLOATING_CONTINENT_RUINS;
import static data.scripts.US_modPlugin.METHANE_ORGANICS;
import static data.scripts.util.US_hyceanManager.manageHyceanConditions;
import static data.scripts.util.US_utils.addConditionIfNeeded;
import static data.scripts.util.US_utils.removeConditionIfNeeded;

@SuppressWarnings("unused")
public class US_manualSystemFixer {
    // This tag is meant to be used for systems created before or at procgen that have custom designed US planets that don't need fixing
    public static final String US_SKIP_SYSTEM = "US_skipSystem";
    // This tag is meant to prevent individual planets from being fixed, useful if a system is half manually created, half procgen
    public static final String US_SKIP_PLANET = "US_skipPlanet";

    public static void fixSystem(StarSystemAPI system) {
        fixSystem(system, false, false);
    }

    public static void fixSystem(StarSystemAPI system, boolean removeHyceanRuins, boolean removeFloatingContinent) {
        if (system == null) return;
        if (system.getPlanets().isEmpty()) return;

        for (PlanetAPI planet : system.getPlanets()) {
            if (planet.isStar()) continue;
            if (planet.hasTag(US_SKIP_PLANET)) continue;

            // Add Organics to Methane planets
            if (planet.getTypeId().equals("US_purple")) {
                addConditionIfNeeded(planet, METHANE_ORGANICS.pick());
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
                    addConditionIfNeeded(planet, FLOATING_CONTINENT_RUINS.pick());
                    if (new Random().nextInt(15) == 0) {
                        addConditionIfNeeded(planet, Conditions.DECIVILIZED);
                    }
                }
            }
        }
    }
}
