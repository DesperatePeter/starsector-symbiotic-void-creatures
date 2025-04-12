package tecrys.svc.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import tecrys.svc.utils.getSpecialQuantity

class SvcDoesPlayerHaveLureItems: BaseCommandPlugin() {
    companion object{
        const val FOOD_REQUIRED = 1000f
        const val OIL_REQUIRED = 50f
        const val POISON_REQUIRED = 1f
    }
    override fun execute(
        s: String?,
        interactionDialogAPI: InteractionDialogAPI?,
        list: MutableList<Misc.Token>?,
        map: MutableMap<String, MemoryAPI>?
    ): Boolean {
        Global.getSector().playerFleet?.cargo?.let { cargo ->
            //val poisonQuantity = cargo.getSpecialQuantity("svc_poison")
            val oilQuantity = cargo.getSpecialQuantity("svc_whale_oil")
            val foodQuantity = cargo.getCommodityQuantity("food")
            return oilQuantity >= OIL_REQUIRED && foodQuantity >= FOOD_REQUIRED
        }
        return false
    }
}