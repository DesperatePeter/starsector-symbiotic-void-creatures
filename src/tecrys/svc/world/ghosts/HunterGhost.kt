package tecrys.svc.world.ghosts

import com.fs.starfarer.api.Script
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost
import com.fs.starfarer.api.impl.campaign.ghosts.GBIRunScript
import com.fs.starfarer.api.impl.campaign.ghosts.GBIntercept
import com.fs.starfarer.api.impl.campaign.ghosts.GBStayInPlace
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.notifications.NotificationShower

class HunterGhost(manager: SensorGhostManager, fleet: CampaignFleetAPI) : BaseSensorGhost(manager, 0), Script {
    companion object{
        const val BURN_SPEED = 20
    }
    init {
        initEntity(genLargeSensorProfile(), genMediumRadius())
        if(!placeNearPlayer(2000f, 2500f)){
            setCreationFailed()
        }else{
            setDespawnRange(0f)
            addBehavior(GBIntercept(fleet, 10f, BURN_SPEED, 250f, true))
            addBehavior(GBStayInPlace(0.1f))
            addInterrupt(GBIRunScript(0f, this, true))
            NotificationShower.showNotificationOnce(NotificationShower.HUNTER_FLEET_APPROACHING_ID)
        }

    }

    override fun run() {
        FleetManager().spawnHunterFleet(entity.location, true)
    }
}