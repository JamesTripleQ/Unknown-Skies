
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

public class US_tunnels extends BaseHazardCondition implements MarketImmigrationModifier {

    @Override
    public void apply(String id) {
        market.getHazard().modifyFlat(id, getHazardBonus(), condition.getName());
        market.addTransientImmigrationModifier(this);
    }

    @Override
    public void unapply(String id) {
        market.getHazard().unmodify(id);
        market.removeTransientImmigrationModifier(this);

    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.POOR, 10f);
        incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(market.getSize()), Misc.ucFirst(condition.getName().toLowerCase()));
    }

    private float getThisImmigrationBonus(int size) {
        return getFixedMarketSize(size) * -2;
    }

    private float getHazardBonus() {
        return 1 - market.getHazardValue();
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("tunnel_0"),
                10f,
                Misc.getHighlightColor(),
                txt("tunnel_1")
        );

        tooltip.addPara(
                txt("tunnel_2"),
                10f,
                Misc.getHighlightColor(),
                "" + (int) getThisImmigrationBonus(market.getSize())
        );
    }
}
