package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import static data.scripts.US_utils.txt;

public class US_artificial extends BaseHazardCondition {
    private final float DEFENSE_BONUS = 2.0f;

    @Override
    public void apply(String id) {
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, DEFENSE_BONUS, condition.getName());
    }

    @Override
    public void unapply(String id) {
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("artificial_0"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int) ((DEFENSE_BONUS - 1) * 100) + txt("%")
        );
    }
}
