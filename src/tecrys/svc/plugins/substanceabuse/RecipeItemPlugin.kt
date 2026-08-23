package tecrys.svc.plugins.substanceabuse

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import tecrys.svc.modintegration.isSubstanceAbuseEnabled
import kotlin.jvm.internal.Intrinsics


class RecipeItemPlugin

    : GenericSpecialItemPlugin() {
    var plugin: GenericSpecialItemPlugin? = null

    override fun init(stack: CargoStackAPI) {
        Intrinsics.checkNotNullParameter(stack, "stack")
        super.init(stack)
        if (isSubstanceAbuseEnabled()) {
            this.plugin = makeRecipeItem()
            val genericSpecialItemPlugin = this.plugin
            Intrinsics.checkNotNull(genericSpecialItemPlugin)
            genericSpecialItemPlugin!!.init(stack)
            val genericSpecialItemPlugin2 = this.plugin
            Intrinsics.checkNotNull(genericSpecialItemPlugin2)
            genericSpecialItemPlugin2!!.id = itemId
        }
    }

    override fun hasRightClickAction(): Boolean {
        return true
    }

    override fun performRightClickAction() {
        if (this.plugin != null) {
            val genericSpecialItemPlugin = plugin!!
            Intrinsics.checkNotNull(genericSpecialItemPlugin)
            genericSpecialItemPlugin.performRightClickAction()
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
        if (this.plugin != null) {
            plugin!!.createTooltip(tooltip, expanded, transferHandler, stackSource)
        }
    }
}