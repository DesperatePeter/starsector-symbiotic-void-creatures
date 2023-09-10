package tecrys.svc.items

import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import org.lwjgl.input.Keyboard

class StjarwhaleOilItemPlugin : BaseSpecialItemPlugin() {
    companion object{
        const val FUEL_PER_OIL = 100f
    }
    override fun hasRightClickAction(): Boolean = true
    override fun shouldRemoveOnRightClickAction(): Boolean = false
    override fun performRightClickAction() {
        do {
            stack.subtract(1f)
            stack.cargo.addFuel(FUEL_PER_OIL)
        }while ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RMETA) || Keyboard.isKeyDown(Keyboard.KEY_LMETA))
            && stack.size >= 1f && stack.cargo.freeFuelSpace.toFloat() >= FUEL_PER_OIL )

    }
}