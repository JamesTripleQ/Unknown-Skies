package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.US_utils.txt;

public class US_mind extends BaseHazardCondition implements MarketImmigrationModifier {
    private final int PRODUCTION_MALUS = -1;

    @Override
    public void apply(String id) {
        // Remove drug demand
        Industry industry = market.getIndustry(Industries.POPULATION);
        if (industry != null) {
            industry.getDemand(Commodities.DRUGS).getQuantity().modifyMult(id + "_0", 0);
        }

        industry = market.getIndustry(Industries.MINING);
        if (industry != null) {
            industry.getDemand(Commodities.DRUGS).getQuantity().modifyMult(id + "_0", 0);
        }

        // Reduce production
        for (Industry i : market.getIndustries()) {
            for (MutableCommodityQuantity c : i.getAllSupply()) {
                i.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, PRODUCTION_MALUS, txt("spore"));
            }
        }

        // Stability buff
        market.getStability().modifyFlat(id, getStabilityFloor(), txt("spore"));
        market.addTransientImmigrationModifier(this);
    }

    @Override
    public void unapply(String id) {
        market.getStability().unmodify(id);
        market.removeTransientImmigrationModifier(this);

        for (Industry i : market.getIndustries()) {
            for (MutableCommodityQuantity c : i.getAllSupply()) {
                i.getSupply(c.getCommodityId()).getQuantity().unmodify(id);
            }
        }

        Industry industry = market.getIndustry(Industries.POPULATION);
        if (industry != null) {
            industry.getDemand(Commodities.DRUGS).getQuantity().unmodify(id + "_0");
        }

        industry = market.getIndustry(Industries.MINING);
        if (industry != null) {
            industry.getDemand(Commodities.DRUGS).getQuantity().unmodify(id + "_0");
        }
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.PLAYER, 10f);
        incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
    }

    private float getThisImmigrationBonus() {
        return 10 * market.getSize();
    }

    private float getStabilityFloor() {
        return Math.max(0, 5 - market.getStabilityValue());
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("spore_3"),
                10f,
                Misc.getHighlightColor(),
                txt("spore_4")
        );

        tooltip.addPara(
                txt("spore_2"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) getThisImmigrationBonus()
        );

        tooltip.addPara(
                txt("spore_5"),
                10f,
                Misc.getHighlightColor(),
                "" + PRODUCTION_MALUS
        );

        tooltip.addPara(
                txt("spore_0"),
                10f,
                Misc.getHighlightColor(),
                txt("spore_1")
        );
    }
}
