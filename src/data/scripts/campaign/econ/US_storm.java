/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.US_txt.txt;


public class US_storm extends BaseHazardCondition {
    
    private final float ACCESS_PENALTY = 10;
    private final float DEFENSE_BONUS = 1.5f;

    @Override
    public void apply(String id) {
        super.apply(id);
        
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, DEFENSE_BONUS, txt("storm"));
        
	market.getAccessibilityMod().modifyFlat(id, -ACCESS_PENALTY/100f, txt("storm"));        
    }

    @Override
    public void unapply(String id) {        
        super.unapply(id);
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).unmodify(id);
        market.getAccessibilityMod().unmodifyFlat(id);
    }
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);

        tooltip.addPara(
                txt("storm_0"),
                10f,
                Misc.getHighlightColor(),
                txt("+") + (int)((DEFENSE_BONUS-1)*100) + txt("%")
        );
        
        tooltip.addPara(
                txt("storm_1"),
                10f, 
                Misc.getHighlightColor(),
                txt("-") + (int) ACCESS_PENALTY + txt("%")
        );
    }
}
