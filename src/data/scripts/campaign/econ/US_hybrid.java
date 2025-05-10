package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.util.US_utils.txt;

public class US_hybrid extends BaseMarketConditionPlugin {
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("hybrid_0"),
                10f,
                Misc.getHighlightColor(),
                txt("hybrid_1"),
                txt("hybrid_2")
        );
    }

    @Override
    public boolean showIcon() {
        return !market.getFactionId().equals(Factions.NEUTRAL);
    }
}