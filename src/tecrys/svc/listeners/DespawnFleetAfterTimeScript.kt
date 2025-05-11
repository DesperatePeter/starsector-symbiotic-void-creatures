package tecrys.svc.listeners

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI

class DespawnFleetAfterTimeScript(private val fleet: CampaignFleetAPI, private val durationInDays: Int): EveryFrameScript {
    private var isDone = false
    private val creationTimestamp = Global.getSector().clock.timestamp

    override fun isDone(): Boolean = isDone

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if(Global.getSector().clock.getElapsedDaysSince(creationTimestamp) >= durationInDays){
            isDone = true
            fleet.despawn()
        }
    }
}