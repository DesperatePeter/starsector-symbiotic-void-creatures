package tecrys.svc.plugins.substanceabuse

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.alcoholism.industry.Brewery
import com.fs.starfarer.api.alcoholism.itemPlugins.RecipeInstallableItemEffect
import com.fs.starfarer.api.alcoholism.memory.AddictionMemory
import com.fs.starfarer.api.alcoholism.memory.Alcohol
import com.fs.starfarer.api.alcoholism.memory.AlcoholRepo
import com.fs.starfarer.api.alcoholism.memory.FactionAlcoholHandler
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import tecrys.svc.WHALE_OIL_ITEM_ID

fun loadSubstanceAbuse(){
    Global.getSettings().getHullModSpec(SVC_COCKTAIL_HULLMOD_ID).effectClass = SVC_COCKTAIL_HULLMOD_REF
    AlcoholRepo.ALCOHOL_MAP[SVC_COCKTAIL_ALCOHOL_ID] =
        Alcohol(SVC_COCKTAIL_ALCOHOL_ID, 2.5f, "pirates", -3, -2, Commodities.ORGANICS) // , WHALE_OIL_ITEM_ID

    ItemEffectsRepo.ITEM_EFFECTS[SVC_COCKTAIL_RECIPE_ID] = RecipeInstallableItemEffect(SVC_COCKTAIL_ALCOHOL_ID)
}

fun disableSubstanceAbuse(){
    Global.getSettings().getCommoditySpec(SVC_COCKTAIL_COMMODITY_ID).basePrice = 0f
    Global.getSettings().getCommoditySpec(SVC_COCKTAIL_COMMODITY_ID).exportValue = 0f
    Global.getSettings().getCommoditySpec(SVC_COCKTAIL_COMMODITY_ID).tags.add("nonecon")
}

fun giveCocktailToPirates(){
    AddictionMemory.getInstanceOrRegister().refresh()
    FactionAlcoholHandler.setFactionAlcoholTypes("pirates", SVC_COCKTAIL_ALCOHOL_ID)
}

private fun addCocktailBreweryToMarket(market: MarketAPI){
    market.addIndustry(Brewery.INDUSTRY_ID)
    market.getIndustry(Brewery.INDUSTRY_ID)?.specialItem =
        SpecialItemData(AlcoholRepo.get(SVC_COCKTAIL_ALCOHOL_ID).industryItemId, null)
}

fun addCocktailBreweryToRelevantMarkets(){
    SVC_MARKETS_WITH_BREWERIES.forEach {id ->
        Global.getSector().economy.getMarket(id)?.let {
            addCocktailBreweryToMarket(it)
        }
    }
}