package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.US_utils.*;

public class US_magnetic extends BaseHazardCondition implements MarketImmigrationModifier {
    private final float DEFENSE_BONUS = 3000;

    @Override
    public void apply(String id) {
        super.apply(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyFlat(id, DEFENSE_BONUS, txt("magnet"));
        market.addTransientImmigrationModifier(this);
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.removeTransientImmigrationModifier(this);
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.POOR, 10f);
        incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(market.getSize()), Misc.ucFirst(condition.getName().toLowerCase()));
    }

    private float getThisImmigrationBonus(int size) {
        return -2 * getFixedMarketSize(size);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("magnet_1"),
                10f,
                Misc.getHighlightColor(),
                "" + (int) getThisImmigrationBonus(market.getSize())
        );

        tooltip.addPara(
                txt("magnet_0"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) DEFENSE_BONUS
        );

        addScalingTable(
                market,
                tooltip,
                expanded,
                "" + (int) getThisImmigrationBonus(3),
                "" + (int) getThisImmigrationBonus(4),
                "" + (int) getThisImmigrationBonus(5),
                "" + (int) getThisImmigrationBonus(6)
        );
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }
}
