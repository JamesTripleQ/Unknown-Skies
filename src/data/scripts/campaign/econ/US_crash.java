package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.US_txt.txt;

public class US_crash extends BaseHazardCondition {

    @Override
    public void apply(String id) {
        market.getHazard().modifyFlat(id, getHazardBonus(), condition.getName());
    }

    @Override
    public void unapply(String id) {
        market.getHazard().unmodify(id);
    }

    private float getHazardBonus() {
        return -0.5f + Math.min(0.5f, Math.max(0, market.getSize() - 4) / 10f);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("drone_0"),
                10f,
                Misc.getHighlightColor(),
                (int) (getHazardBonus() * 100) + txt("%")
        );
    }
}
