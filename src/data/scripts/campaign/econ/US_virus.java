package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.util.US_utils.*;

public class US_virus extends BaseHazardCondition implements MarketImmigrationModifier {
    private final float DEFENSE_MALUS = 0.6f;
    private final int WEAPON_BONUS = 3;
    private final float STABILITY_MALUS = -2f;

    @Override
    public void apply(String id) {
        super.apply(id);
        // Defense debuff
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, DEFENSE_MALUS, txt("virus"));
        // Growth debuff
        market.addTransientImmigrationModifier(this);
        // Stability debuff
        market.getStability().modifyFlat(id, STABILITY_MALUS, txt("virus"));

        // Raise weapon production
        Industry industry = market.getIndustry(Industries.HEAVYINDUSTRY);
        if (industry != null) {
            if (industry.isFunctional()) {
                industry.supply(id + "_0", Commodities.HAND_WEAPONS, WEAPON_BONUS, txt("virus"));
            } else {
                industry.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id + "_0");
            }
        }

        industry = market.getIndustry(Industries.ORBITALWORKS);
        if (industry != null) {
            if (industry.isFunctional()) {
                industry.supply(id + "_0", Commodities.HAND_WEAPONS, WEAPON_BONUS, txt("virus"));
            } else {
                industry.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id + "_0");
            }
        }
    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.removeTransientImmigrationModifier(this);
        market.getStability().unmodify(id);

        Industry industry = market.getIndustry(Industries.HEAVYINDUSTRY);
        if (industry != null) {
            industry.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id + "_0");
        }

        industry = market.getIndustry(Industries.ORBITALWORKS);
        if (industry != null) {
            industry.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id + "_0");
        }
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.POOR, 10f);
        incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(market.getSize()), Misc.ucFirst(condition.getName().toLowerCase()));
    }

    private float getThisImmigrationBonus(int size) {
        return -2f * (getFixedMarketSize(size) + 1);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("virus_3"),
                10f,
                Misc.getHighlightColor(),
                "" + (int) STABILITY_MALUS
        );

        tooltip.addPara(
                txt("virus_1"),
                10f,
                Misc.getHighlightColor(),
                "" + (int) getThisImmigrationBonus(market.getSize())
        );

        tooltip.addPara(
                txt("virus_2"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + WEAPON_BONUS
        );

        tooltip.addPara(
                txt("virus_0"),
                10f,
                Misc.getHighlightColor(),
                Strings.X + DEFENSE_MALUS + ""
        );

        addScalingTable(
                market,
                tooltip,
                expanded,
                2,
                "" + (int) getThisImmigrationBonus(3),
                "" + (int) getThisImmigrationBonus(4),
                "" + (int) getThisImmigrationBonus(5),
                "" + (int) getThisImmigrationBonus(6)
        );
    }

    @Override
    public boolean isTooltipExpandable() {
        return true;
    }
}
