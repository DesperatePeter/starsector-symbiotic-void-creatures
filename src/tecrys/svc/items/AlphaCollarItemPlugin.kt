package tecrys.svc.items

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import tecrys.svc.canRecoverAlphas
import tecrys.svc.utils.unlockVoidlingRecovery

class AlphaCollarItemPlugin: BaseSpecialItemPlugin() {
    override fun hasRightClickAction(): Boolean = true
    override fun shouldRemoveOnRightClickAction(): Boolean = true
    override fun performRightClickAction() {
        canRecoverAlphas = true
        unlockVoidlingRecovery()
        Global.getSector()?.campaignUI?.addMessage("You are now able to control even alpha voidlings!")
    }
}