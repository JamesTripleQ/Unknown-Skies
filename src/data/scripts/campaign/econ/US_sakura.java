package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.util.US_utils.*;

public class US_sakura extends BaseHazardCondition implements MarketImmigrationModifier {
    private final int FOOD_MALUS = -2;
    private final float INCOME_BONUS = 10f;

    @Override
    public void apply(String id) {
        // Reduce food production
        Industry industry = market.getIndustry(Industries.FARMING);
        if (industry != null) {
            if (industry.isFunctional()) {
                industry.supply(id + "_0", Commodities.FOOD, FOOD_MALUS, Misc.ucFirst(condition.getName().toLowerCase()));
            } else {
                industry.getSupply(Commodities.FOOD).getQuantity().unmodifyFlat(id + "_0");
            }
        }

        market.getIncomeMult().modifyPercent(id, INCOME_BONUS, Misc.ucFirst(condition.getName().toLowerCase()));
    }

    @Override
    public void unapply(String id) {
        Industry industry = market.getIndustry(Industries.FARMING);
        if (industry != null) {
            industry.getSupply(Commodities.FOOD).getQuantity().unmodifyFlat(id + "_0");
        }

        market.getIncomeMult().unmodify(id);
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.getWeight().modifyFlat(getModId(), getImmigrationBonus(market.getSize()), Misc.ucFirst(condition.getName().toLowerCase()));
    }

    private float getImmigrationBonus(int size) {
        return getFixedMarketSize(size) + 2;
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("sakura_0"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) getImmigrationBonus(market.getSize())
        );

        tooltip.addPara(
                txt("sakura_1"),
                10f,
                Misc.getHighlightColor(),
                FOOD_MALUS + ""
        );

        tooltip.addPara(
                txt("sakura_2"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) INCOME_BONUS + txt("%")
        );

        addScalingTable(
                market,
                tooltip,
                expanded,
                2,
                txt("+") + (int) getImmigrationBonus(3),
                txt("+") + (int) getImmigrationBonus(4),
                txt("+") + (int) getImmigrationBonus(5),
                txt("+") + (int) getImmigrationBonus(6)
        );
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }
}
