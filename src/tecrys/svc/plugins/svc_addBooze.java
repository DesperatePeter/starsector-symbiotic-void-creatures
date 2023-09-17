package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.industry.Brewery;
import com.fs.starfarer.api.alcoholism.itemPlugins.RecipeInstallableItemEffect;
import com.fs.starfarer.api.alcoholism.memory.AddictionMemory;
import com.fs.starfarer.api.alcoholism.memory.Alcohol;
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo;
import com.fs.starfarer.api.alcoholism.memory.FactionAlcoholHandler;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import static com.fs.starfarer.api.alcoholism.memory.AlcoholRepo.ALCOHOL_MAP;
import static tecrys.svc.ConstantsKt.WHALE_OIL_ITEM_ID;

public class svc_addBooze {

    public static void addBooze () {
        Global.getSettings().getHullModSpec("svc_cocktail").setEffectClass("tecrys.svc.hullmods.svc_cocktail");
        ALCOHOL_MAP.put("svc_cocktail", new Alcohol("svc_cocktail", 2.5f, "pirates", -3, -2, Commodities.ORGANICS, WHALE_OIL_ITEM_ID));

        ItemEffectsRepo.ITEM_EFFECTS.put("svc_cocktail_item", new RecipeInstallableItemEffect("svc_cocktail"));
    }

    public static void addBoozeToFaction(){
        AddictionMemory.getInstanceOrRegister().refresh();
        FactionAlcoholHandler.setFactionAlcoholTypes("pirates", "svc_cocktail");
    }

    public static void addBrewery(MarketAPI market){
        market.addIndustry(Brewery.INDUSTRY_ID);
        market.getIndustry(Brewery.INDUSTRY_ID).setSpecialItem(new SpecialItemData(AlcoholRepo.get("svc_cocktail").getIndustryItemId(), null));

    }
}