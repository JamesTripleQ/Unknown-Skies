package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.US_utils.getFixedMarketSize;
import static data.scripts.US_utils.txt;

public class US_religious extends BaseHazardCondition implements MarketImmigrationModifier {
    public static float STABILITY_BONUS = 1;
    public static float ACCESSIBILITY_BONUS = 10f;

    @Override
    public void apply(String id) {
        market.getAccessibilityMod().modifyFlat(id, ACCESSIBILITY_BONUS / 100, txt("landmark"));
        market.getStability().modifyFlat(id, STABILITY_BONUS, txt("landmark"));
        market.addTransientImmigrationModifier(this);
    }

    @Override
    public void unapply(String id) {
        market.getAccessibilityMod().unmodify(id);
        market.getStability().unmodify(id);
        market.removeTransientImmigrationModifier(this);
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.LUDDIC_CHURCH, 10f);
        incoming.add(Factions.LUDDIC_PATH, 5f);
        incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(market.getSize()), Misc.ucFirst(condition.getName().toLowerCase()));
    }

    private float getThisImmigrationBonus(int size) {
        return 20 - 2 * getFixedMarketSize(size);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("landmark_0"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) STABILITY_BONUS
        );

        tooltip.addPara(
                txt("landmark_1"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) ACCESSIBILITY_BONUS + txt("%")
        );

        tooltip.addPara(
                txt("landmark_2"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) getThisImmigrationBonus(market.getSize())
        );
    }
}
