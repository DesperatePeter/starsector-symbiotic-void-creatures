package tecrys.svc.items

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import org.lwjgl.input.Keyboard

class StjarwhaleOilItemPlugin : BaseSpecialItemPlugin() {
    companion object{
        const val FUEL_PER_OIL = 50f
        val isCtrl get() = (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RMETA) || Keyboard.isKeyDown(Keyboard.KEY_LMETA))
    }
    override fun hasRightClickAction(): Boolean = true
    override fun shouldRemoveOnRightClickAction(): Boolean = false

    override fun performRightClickAction(helper: SpecialItemPlugin.RightClickActionHelper?) {
        helper?.run {
            val countOil = { getNumItems(CargoAPI.CargoItemType.SPECIAL, stack.specialDataIfSpecial) }
            do{
                if(countOil() <= 0) break
                removeFromClickedStackFirst(1)
                addItems(CargoAPI.CargoItemType.RESOURCES, "fuel", FUEL_PER_OIL)
            }while (isCtrl && Global.getSector().playerFleet.cargo.freeFuelSpace.toFloat() >= FUEL_PER_OIL)
        }
    }
}