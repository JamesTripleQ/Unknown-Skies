
package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.US_utils.txt;

public class US_elevator extends BaseHazardCondition {
    private final float ACCESSIBILITY_BONUS = 50f;

    @Override
    public void apply(String id) {
        market.getAccessibilityMod().modifyFlat(id, getAccessibilityBonus() / 100, txt("accelerator"));
    }

    @Override
    public void unapply(String id) {
        market.getAccessibilityMod().unmodify(id);
    }

    private float getAccessibilityBonus() {
        return Math.max(0, ACCESSIBILITY_BONUS - (Math.max(0, market.getSize() - 3)));
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("accelerator_0"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) ACCESSIBILITY_BONUS + txt("%")
        );
    }
}
