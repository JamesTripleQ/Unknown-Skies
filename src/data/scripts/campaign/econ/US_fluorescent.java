package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.util.US_utils.txt;

public class US_fluorescent extends BaseHazardCondition {
    private final int VOLATILE_BONUS = 2;

    @Override
    public void apply(String id) {
        // Raise volatiles production
        Industry industry = market.getIndustry(Industries.MINING);
        if (industry != null) {
            if (industry.isFunctional()) {
                industry.supply(id + "_0", Commodities.VOLATILES, VOLATILE_BONUS, txt("fluorescent"));
            } else {
                industry.getSupply(Commodities.VOLATILES).getQuantity().unmodifyFlat(id + "_0");
            }
        }

        // Reduce volatile demand
        industry = market.getIndustry(Industries.FUELPROD);
        if (industry != null) {
            industry.getDemand(Commodities.VOLATILES).getQuantity().modifyMult(id + "_0", 0);
        }
    }

    @Override
    public void unapply(String id) {
        Industry industry = market.getIndustry(Industries.MINING);
        if (industry != null) {
            industry.getSupply(Commodities.VOLATILES).getQuantity().unmodifyFlat(id + "_0");
        }

        industry = market.getIndustry(Industries.FUELPROD);
        if (industry != null) {
            industry.getDemand(Commodities.VOLATILES).getQuantity().unmodify(id + "_0");
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("fluorescent_0"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + VOLATILE_BONUS
        );

        tooltip.addPara(
                txt("fluorescent_1"),
                10f,
                Misc.getHighlightColor(),
                txt("fluorescent_2")
        );
    }
}
