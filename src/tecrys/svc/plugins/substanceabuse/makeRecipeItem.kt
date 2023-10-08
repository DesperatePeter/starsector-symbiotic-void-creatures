package tecrys.svc.plugins.substanceabuse

import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin
import tecrys.svc.modintegration.isSubstanceAbuseEnabled

fun makeRecipeItem(): GenericSpecialItemPlugin?{
    if (isSubstanceAbuseEnabled()) {
        return try {
            com.fs.starfarer.api.alcoholism.itemPlugins.RecipeItemPlugin()
        } catch (e: NoClassDefFoundError) {
            null
        }
    }
    return null
}