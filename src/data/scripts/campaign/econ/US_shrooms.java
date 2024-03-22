package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.US_utils.txt;

public class US_shrooms extends BaseHazardCondition {
    private final float STABILITY_MALUS = -3;
    private final int DRUG_BONUS = 2;

    @Override
    public void apply(String id) {
        // Reduce drug demand
        Industry industry = market.getIndustry(Industries.POPULATION);
        if (industry != null) {
            industry.getDemand(Commodities.DRUGS).getQuantity().modifyMult(id + "_0", 0);
        }

        industry = market.getIndustry(Industries.MINING);
        if (industry != null) {
            industry.getDemand(Commodities.DRUGS).getQuantity().modifyMult(id + "_0", 0);
        }

        // Raise drug production
        industry = market.getIndustry(Industries.LIGHTINDUSTRY);
        if (industry != null) {
            if (industry.isFunctional()) {
                industry.supply(id + "_0", Commodities.DRUGS, DRUG_BONUS, condition.getName());
            } else {
                industry.getSupply(Commodities.DRUGS).getQuantity().unmodifyFlat(id + "_0");
            }
        }

        // Stability debuff
        market.getStability().modifyFlat(id, STABILITY_MALUS, condition.getName());
    }

    @Override
    public void unapply(String id) {
        market.getStability().unmodify(id);

        Industry industry = market.getIndustry(Industries.POPULATION);
        if (industry != null) {
            industry.getDemand(Commodities.DRUGS).getQuantity().unmodify(id + "_0");
        }

        industry = market.getIndustry(Industries.MINING);
        if (industry != null) {
            industry.getDemand(Commodities.DRUGS).getQuantity().unmodify(id + "_0");
        }

        industry = market.getIndustry(Industries.LIGHTINDUSTRY);
        if (industry != null) {
            industry.getSupply(Commodities.DRUGS).getQuantity().unmodifyFlat(id + "_0");
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("shroom_0"),
                10f,
                Misc.getHighlightColor(),
                txt("shroom_1")
        );

        tooltip.addPara(
                txt("shroom_2"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + DRUG_BONUS
        );

        tooltip.addPara(
                txt("shroom_3"),
                10f,
                Misc.getHighlightColor(),
                "" + (int) STABILITY_MALUS
        );
    }
}
