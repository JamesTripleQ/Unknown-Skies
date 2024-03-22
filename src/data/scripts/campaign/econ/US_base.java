package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.US_utils.txt;

public class US_base extends BaseHazardCondition {
    private final float PRODUCTION_BONUS = 0.25f;
    private final String sharedID = "milBase";

    @Override
    public void apply(String id) {
        // Ignore uncolonized
        if (market.getFaction() == null || market.getFaction().getId().equals(Factions.NEUTRAL)) return;

        // Raise ship quality
        Industry industry = market.getIndustry(Industries.HEAVYINDUSTRY);
        if (industry != null) {
            if (industry.isFunctional()) {
                modifyAllFactionMarkets(sharedID, market.getFaction());
            } else {
                unmodifyAllFactionMarkets(sharedID, market.getFaction());
            }
        }

        industry = market.getIndustry(Industries.ORBITALWORKS);
        if (industry != null) {
            if (industry.isFunctional()) {
                modifyAllFactionMarkets(sharedID, market.getFaction());
            } else {
                unmodifyAllFactionMarkets(sharedID, market.getFaction());
            }
        }
    }

    @Override
    public void unapply(String id) {
        unmodifyAllFactionMarkets(sharedID, market.getFaction());
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("base_0"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) ((PRODUCTION_BONUS) * 100) + txt("%")
        );
    }

    private void modifyAllFactionMarkets(String id, FactionAPI faction) {
        for (MarketAPI thisMarket : Misc.getFactionMarkets(faction)) {
            thisMarket.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).modifyFlat(id, PRODUCTION_BONUS, txt("base"));
            super.apply(id);
        }
    }

    private void unmodifyAllFactionMarkets(String id, FactionAPI faction) {
        for (MarketAPI thisMarket : Misc.getFactionMarkets(faction)) {
            thisMarket.getStats().getDynamic().getMod(Stats.PRODUCTION_QUALITY_MOD).unmodify(id);
            super.apply(id);
        }
    }
}
