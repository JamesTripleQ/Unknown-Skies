package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.US_utils.getFixedMarketSize;
import static data.scripts.US_utils.txt;

public class US_elevator extends BaseHazardCondition {
    private final float ACCESSIBILITY_BONUS = 50f;

    @Override
    public void apply(String id) {
        market.getAccessibilityMod().modifyFlat(id, getAccessibilityBonus(market.getSize()) / 100, txt("accelerator"));
    }

    @Override
    public void unapply(String id) {
        market.getAccessibilityMod().unmodify(id);
    }

    private float getAccessibilityBonus(int size) {
        return Math.max(0, ACCESSIBILITY_BONUS - (Math.max(0, getFixedMarketSize(size) - 3) * 10));
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
    }
}
