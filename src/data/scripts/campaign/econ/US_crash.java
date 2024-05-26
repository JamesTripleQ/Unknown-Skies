package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.util.US_utils.*;

public class US_crash extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        market.getHazard().modifyFlat(id, getHazardBonus(market.getSize()), condition.getName());
    }

    @Override
    public void unapply(String id) {
        market.getHazard().unmodify(id);
    }

    private float getHazardBonus(int size) {
        return -0.5f + Math.min(0.5f, Math.max(0, getFixedMarketSize(size) - 4) / 10f);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("drone_0"),
                10f,
                Misc.getHighlightColor(),
                (int) (getHazardBonus(market.getSize()) * 100) + txt("%")
        );

        addScalingTable(
                market,
                tooltip,
                expanded,
                0,
                (int) (getHazardBonus(3) * 100) + txt("%"),
                (int) (getHazardBonus(4) * 100) + txt("%"),
                (int) (getHazardBonus(5) * 100) + txt("%"),
                (int) (getHazardBonus(6) * 100) + txt("%")
        );
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }
}
