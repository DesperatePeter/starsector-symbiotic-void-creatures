package tecrys.svc.world

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.IntervalUtil
import tecrys.svc.CombatPlugin
import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.utils.DelayedMusicPlayer
import tecrys.svc.world.fleets.FleetSpawner

class ContextBaseMusicPlayer: EveryFrameScript {

    private val timer = IntervalUtil(5f, 5f) // check every 5 seconds as to not waste performance
    private var cooldown = 0f

    companion object{
        const val COOLDOWN_SECS = 500f // don't play the theme again if it was recently played
    }

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if(Global.getCurrentState() != GameState.CAMPAIGN) return
        stopBattleThemeIfApplicable()
        if(Global.getSector().playerFleet.isInHyperspace) return
        timer.advance(amount)
        if(timer.intervalElapsed()){
            cooldown -= timer.intervalDuration
            if(cooldown >= 0f) return
            if(FleetSpawner.getFactionFleets(SVC_FACTION_ID).any {
                it.containingLocation == Global.getSector().playerFleet.containingLocation
                }){
                playSvcTheme()
                cooldown = COOLDOWN_SECS
            }
        }
    }

    private fun playSvcTheme(){
        DelayedMusicPlayer.playDelayedMusic("svc_voidling_exploration_theme", 4, 1, false)
    }

    private fun stopBattleThemeIfApplicable(){
        if(Global.getSector().memoryWithoutUpdate.getBoolean(CombatPlugin.IS_BATTLE_THEME_PLAYING_MEM_KEY)){
            Global.getSoundPlayer().pauseCustomMusic()
            Global.getSector().memoryWithoutUpdate.unset(CombatPlugin.IS_BATTLE_THEME_PLAYING_MEM_KEY)
        }
    }
}