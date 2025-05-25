package tecrys.svc.plugins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin

class MeatEntityPlugin: BaseCustomEntityPlugin() {
    companion object{
        const val DURATION_IN_DAYS = 30f
    }
    private val creationTimestamp = Global.getSector().clock.timestamp
    private var stableLocation: SectorEntityToken? = null
    override fun init(entity: SectorEntityToken?, pluginParams: Any?) {
        this.entity = entity
        stableLocation = pluginParams as? SectorEntityToken
    }

    override fun getRenderRange(): Float = 1000f

    override fun advance(amount: Float) {
        super.advance(amount)
        stableLocation?.let { l ->
            entity.setLocation(l.location.x, l.location.y)
        }
        if(Global.getSector().clock.getElapsedDaysSince(creationTimestamp) > DURATION_IN_DAYS){
            entity.containingLocation.removeEntity(entity)
        }
    }
}