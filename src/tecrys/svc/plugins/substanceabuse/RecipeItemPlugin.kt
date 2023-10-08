package tecrys.svc.plugins.substanceabuse

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import tecrys.svc.modintegration.isSubstanceAbuseEnabled

class RecipeItemPlugin : GenericSpecialItemPlugin() {
    var plugin: GenericSpecialItemPlugin? = null
    override fun init(stack: CargoStackAPI) {
        super.init(stack)
        if (isSubstanceAbuseEnabled()) {
                plugin = makeRecipeItem()
                plugin!!.init(stack)
                plugin!!.setId(itemId)
        }
    }

    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun performRightClickAction() {
        if (plugin != null) {
            plugin!!.performRightClickAction()
        }
    }

    override fun shouldRemoveOnRightClickAction(): Boolean {
        return true
    }

    override fun createTooltip(
        tooltip: TooltipMakerAPI,
        expanded: Boolean,
        transferHandler: CargoTransferHandlerAPI,
        stackSource: Any
    ) {
        if (plugin != null) {
            plugin!!.createTooltip(tooltip, expanded, transferHandler, stackSource)
        }
    }
}