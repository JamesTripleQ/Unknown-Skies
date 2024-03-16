/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.US_txt.txt;


public class US_crystals extends BaseHazardCondition{

    private final int CRYSTAL_BONUS=1;
    
    @Override
    public void apply(String id) {        
        
        //raise luxury goods production by one
        Industry industry = market.getIndustry(Industries.LIGHTINDUSTRY);
        if(industry!=null){
            if (industry.isFunctional()) {
               industry.supply(id + "_0", Commodities.LUXURY_GOODS, CRYSTAL_BONUS, txt("crystal"));
            } else {
               industry.getSupply(Commodities.LUXURY_GOODS).getQuantity().unmodifyFlat(id + "_0");
            }
        }
    }
    
    @Override
    public void unapply(String id) {
        Industry industry = market.getIndustry(Industries.LIGHTINDUSTRY);
        if(industry!=null){
            industry.getSupply(Commodities.LUXURY_GOODS).getQuantity().unmodifyFlat(id + "_0");
        }
    }
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        
        tooltip.addPara(
                txt("crystal_0"),
                10f, 
                Misc.getHighlightColor(),
                txt("+") + CRYSTAL_BONUS
        );
    }
}
