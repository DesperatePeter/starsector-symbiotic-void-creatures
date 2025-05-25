package tecrys.svc.world.ghosts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator
import com.fs.starfarer.api.impl.campaign.ghosts.GhostFrequencies
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhost
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager
import tecrys.svc.utils.DelayedMusicPlayer
import tecrys.svc.world.fleets.hunterFleetsThatCanSpawn

class HunterGhostCreator : BaseSensorGhostCreator() {
    companion object{
        const val BASE_FREQUENCY = 1000f
    }
    override fun createGhost(manager: SensorGhostManager): MutableList<SensorGhost>? {
        val pf = Global.getSector().playerFleet ?: return null
        val ghosts = mutableListOf(HunterGhost(manager, pf) as SensorGhost)
        return if (ghosts.firstOrNull()?.isCreationFailed == false){
            ghosts
        } else null
    }

    override fun getFrequency(manager: SensorGhostManager?): Float {
        return if(hunterFleetsThatCanSpawn.isNotEmpty()){
            GhostFrequencies.getNotInCoreFactor() * BASE_FREQUENCY
        } else 0f
    }

}