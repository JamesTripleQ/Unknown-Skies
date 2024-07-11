package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.util.US_utils.isMarketColonized;
import static data.scripts.util.US_utils.txt;

public class US_cryosanctum extends BaseHazardCondition {
    public final String memkey = "$US_cryosanctumPlaced_";
    private final int INDUSTRY_MALUS = -1;

    @Override
    public void apply(String id) {
        super.apply(id);
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).modifyFlat(id, INDUSTRY_MALUS, txt("cryosanctum"));

        if (isMarketColonized(market) && !isCryosanctumSet()) {
            market.addIndustry(Industries.CRYOSANCTUM);
            market.getIndustry(Industries.CRYOSANCTUM).setDisrupted(180);
            market.getMemoryWithoutUpdate().set(memkey + market.getId(), true);
        }
    }

    @Override
    public void unapply(String id) {
        market.getStats().getDynamic().getMod(Stats.MAX_INDUSTRIES).unmodify(id);
    }

    @Override
    public void advance(float amount) {
        if (isMarketColonized(market) && isCryosanctumSet() && !market.hasIndustry(Industries.CRYOSANCTUM)) {
            market.removeSpecificCondition(condition.getIdForPluginModifications());
        }
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("cryosanctum_0") + market.getName() + txt("cryosanctum_1"),
                10f,
                Misc.getHighlightColor(),
                txt("cryosanctum_2"), txt("cryosanctum_3")
        );

        tooltip.addPara(
                txt("cryosanctum_4"),
                10f,
                Misc.getHighlightColor(),
                "" + INDUSTRY_MALUS
        );
    }

    private boolean isCryosanctumSet() {
        return market.getMemoryWithoutUpdate().getBoolean(memkey + market.getId());
    }
}
