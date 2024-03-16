/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.econ;

//import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
//import com.fs.starfarer.api.impl.campaign.procgen.ConditionGenDataSpec;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.US_txt.txt;


public class US_virus extends BaseHazardCondition implements MarketImmigrationModifier {

    private final float DEFENSE_MALUS = 0.25f;
    //private float DEFENSE_BONUS = 1.5f;
    private final int WEAPON_BONUS = 3;
    private final float STABILITY_MALUS = -2f;
    private final float IMMIGRATION_MALUS = -3f;
    
    @Override
    public void apply(String id) {
        
        super.apply(id);
        
        //massive defense debuf due to the virus presence
        market.getStats().getDynamic().getMod(Stats.GROUND_DEFENSES_MOD).modifyMult(id, DEFENSE_MALUS, txt("virus"));
        //massive growth debuff too
        market.addTransientImmigrationModifier(this);
        //stability  hit
        market.getStability().modifyFlat(id, STABILITY_MALUS, txt("virus"));
        
        //raise weapon production by 3 (knowing the population growth will be quite slow)
        Industry industry = market.getIndustry(Industries.HEAVYINDUSTRY);
        if(industry!=null){
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
        if(industry!=null){
            industry.getSupply(Commodities.HAND_WEAPONS).getQuantity().unmodifyFlat(id + "_0");
        }
    }

    @Override
    public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {
        incoming.add(Factions.POOR, 10f);
        incoming.getWeight().modifyFlat(getModId(), getThisImmigrationBonus(), Misc.ucFirst(condition.getName().toLowerCase()));
    }
    
    private float getThisImmigrationBonus() {
        return IMMIGRATION_MALUS*market.getSize();
    }
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        tooltip.addPara(
                "%s defense rating.",
                10f,
                Misc.getHighlightColor(),
                "" + (int)((DEFENSE_MALUS-1)*100) + "%"
        );
        tooltip.addPara(
                "%s population growth (based on market size).",
                10f, 
                Misc.getHighlightColor(),
                "" + (int) getThisImmigrationBonus()
        );
        tooltip.addPara(
                "%s weapon production.",
                10f, 
                Misc.getHighlightColor(),
                txt("+") + WEAPON_BONUS
        );
    }
}
