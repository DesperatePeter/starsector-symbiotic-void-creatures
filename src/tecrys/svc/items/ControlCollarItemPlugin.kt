package tecrys.svc.items

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import tecrys.svc.canRecoverVoidlings
import tecrys.svc.utils.unlockVoidlingRecovery

class ControlCollarItemPlugin: BaseSpecialItemPlugin() {
    override fun hasRightClickAction(): Boolean = true
    override fun shouldRemoveOnRightClickAction(): Boolean = true
    override fun performRightClickAction() {
        canRecoverVoidlings = true
        unlockVoidlingRecovery()
        Global.getSector()?.campaignUI?.addMessage("You are now able to control regular voidlings!")
        Global.getSector()?.campaignUI?.addMessage("You will be able to salvage them after defeating them in combat!")
    }
}