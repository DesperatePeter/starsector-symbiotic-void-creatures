package tecrys.svc.items

import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin

class StjarwhaleOilItemPlugin : BaseSpecialItemPlugin() {
    companion object{
        const val FUEL_PER_OIL = 100f
    }
    override fun hasRightClickAction(): Boolean = true
    override fun shouldRemoveOnRightClickAction(): Boolean = false
    override fun performRightClickAction() {
        stack.subtract(1f)
        stack.cargo.addFuel(FUEL_PER_OIL)
    }
}