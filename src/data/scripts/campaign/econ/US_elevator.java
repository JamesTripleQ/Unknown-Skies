package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.util.US_utils.*;

public class US_elevator extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        market.getAccessibilityMod().modifyFlat(id, getAccessibilityBonus(market.getSize()) / 100, txt("accelerator"));
    }

    @Override
    public void unapply(String id) {
        market.getAccessibilityMod().unmodify(id);
    }

    private float getAccessibilityBonus(int size) {
        return (getFixedMarketSize(size) - 8) * -10f;
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("accelerator_0"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) getAccessibilityBonus(market.getSize()) + txt("%")
        );

        addScalingTable(
                market,
                tooltip,
                expanded,
                1,
                txt("+") + (int) getAccessibilityBonus(3) + txt("%"),
                txt("+") + (int) getAccessibilityBonus(4) + txt("%"),
                txt("+") + (int) getAccessibilityBonus(5) + txt("%"),
                txt("+") + (int) getAccessibilityBonus(6) + txt("%")
        );
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }
}
