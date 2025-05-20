package tecrys.svc.world

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.mission.FleetSide
import org.json.JSONObject
import org.magiclib.kotlin.forEach
import tecrys.svc.MMM_FACTION_ID
import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.utils.DelayedMusicPlayer
import tecrys.svc.utils.isAnyVoidlingFleetInDistanceHyperspace
import tecrys.svc.world.fleets.FleetSpawner

class ContextBaseMusicPlayer: EveryFrameScript {
    enum class MusicID(val id: String) {
        SVC_WHALE_THEME("svc_whale_theme"),
        SVC_VOIDLING_EXPLORATION_THEME("svc_voidling_exploration_theme"),
        SVC_VOIDLING_BATTLE_THEME("svc_voidling_battle_theme"),
        SVC_VOIDLING_BATTLE_THEME_GLITCHED("svc_voidling_battle_theme_glitched");

        fun toMusicPlayerFormat():String = this.id.lowercase()
    }

    private data class Music(val json: JSONObject) {
        private val _music: MutableMap<MusicID, ArrayList<String>> = mutableMapOf()

        init {
            this.json.keys().forEach { key ->
                val arr = arrayListOf<String>()

                this.json.getJSONArray(key as String?).forEach { it: JSONObject ->
                    arr.add(it.getString("file"))
                }

                _music[MusicID.valueOf((key as String).uppercase())] = arr
            }`
        }

        fun isMusicPlaying(musicID: MusicID): Boolean {
            return _music.getOrElse(musicID) { arrayListOf("") }.contains(Global.getSoundPlayer().currentMusicId)
        }

    }

    private val music: Music = try {
        Music(Global.getSettings().loadJSON("data/config/sounds.json", "symbiotic_void_creatures").getJSONObject("music"));
    } catch (e: Exception){
        println(e)
        throw Exception("Failed to load music files for Symbiotic Void Creatures")
    }

    var lastCheckTime = -10f;
    var lastCheckState: GameState = GameState.TITLE

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if (Global.getCurrentState() == GameState.TITLE) {
            return
        }

        val currentTime = Global.getCombatEngine().getTotalElapsedTime(true)
        val currentState = Global.getCurrentState()

        if (lastCheckState != currentState) {
            lastCheckTime = currentTime - 10f
            lastCheckState = currentState
        }

        when(currentState){
            GameState.CAMPAIGN -> {
                stopBattleTheme()

                if(Global.getSector().playerFleet.isInHyperspace) {
                    if (isAnyVoidlingFleetInDistanceHyperspace(10f)){
                        playExplorationTheme()
                    } else {
                        stopExplorationTheme()
                    }
                    return
                }

                if (music.isMusicPlaying(MusicID.SVC_VOIDLING_EXPLORATION_THEME) && lastCheckTime + 5f > currentTime){
                    return
                }

                if(FleetSpawner.getFactionFleets(SVC_FACTION_ID).any {
                    it.containingLocation == Global.getSector().playerFleet.containingLocation
                }){
                    playExplorationTheme()
                } else if(music.isMusicPlaying(MusicID.SVC_VOIDLING_EXPLORATION_THEME)){
                    stopExplorationTheme()
                }

                lastCheckTime = currentTime
            }
            GameState.COMBAT -> {
                if (lastCheckTime + 5f > currentTime){
                    return
                }

                if (Global.getCombatEngine()?.getFleetManager(FleetSide.ENEMY)?.deployedCopy?.filterNotNull()
                    ?.firstOrNull()?.fleetData?.fleet?.faction == Global.getSector().getFaction(MMM_FACTION_ID)){
                    if (!music.isMusicPlaying(MusicID.SVC_VOIDLING_BATTLE_THEME_GLITCHED)) {
                        playBattleTheme(true)
                    }
                }else if (Global.getCombatEngine()?.getFleetManager(FleetSide.ENEMY)?.deployedCopy?.filterNotNull()
                        ?.firstOrNull()?.fleetData?.fleet?.faction == Global.getSector().getFaction(SVC_FACTION_ID)){
                    if (!music.isMusicPlaying(MusicID.SVC_VOIDLING_BATTLE_THEME)) {
                        playBattleTheme()
                    }
                } else {
                    stopBattleTheme()
                }

                lastCheckTime = currentTime
            }
            else -> return
        }
    }

    private fun playExplorationTheme(){
        if (!music.isMusicPlaying(MusicID.SVC_VOIDLING_EXPLORATION_THEME)){
            DelayedMusicPlayer.playDelayedMusic(1, 1, MusicID.SVC_VOIDLING_EXPLORATION_THEME.toMusicPlayerFormat(), false)
        }
    }

    private fun stopExplorationTheme(){
        if(music.isMusicPlaying(MusicID.SVC_VOIDLING_EXPLORATION_THEME)){
            Global.getSoundPlayer().pauseCustomMusic()
        }
    }

    private fun stopBattleTheme(){
        if(music.isMusicPlaying(MusicID.SVC_VOIDLING_BATTLE_THEME) || music.isMusicPlaying(MusicID.SVC_VOIDLING_BATTLE_THEME_GLITCHED)){
            Global.getSoundPlayer().pauseCustomMusic()
        }
    }

    private fun playBattleTheme(glitched: Boolean = false){
        if(glitched){
            if(music.isMusicPlaying(MusicID.SVC_VOIDLING_BATTLE_THEME_GLITCHED)) return
            DelayedMusicPlayer.playDelayedMusic(1, 1, MusicID.SVC_VOIDLING_BATTLE_THEME_GLITCHED.toMusicPlayerFormat(), true)
        } else {
            if(music.isMusicPlaying(MusicID.SVC_VOIDLING_BATTLE_THEME)) return
            DelayedMusicPlayer.playDelayedMusic(1, 1, MusicID.SVC_VOIDLING_BATTLE_THEME.toMusicPlayerFormat(), true)
        }
    }
}