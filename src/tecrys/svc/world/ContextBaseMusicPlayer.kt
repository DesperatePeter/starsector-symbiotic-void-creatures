package tecrys.svc.world

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import org.magiclib.kotlin.forEach
import tecrys.svc.MMM_FACTION_ID
import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.utils.isAnyVoidlingFleetInDistanceHyperspace
import tecrys.svc.world.fleets.FleetSpawner
import tecrys.svc.world.fleets.MASTERMIND_FLEET_MEMKEY

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
            }
        }

        fun isMusicPlaying(musicID: MusicID): Boolean {
            return _music.getOrElse(musicID) { arrayListOf("") }.contains(Global.getSoundPlayer().currentMusicId)
        }

    }

    val DISTANCE_TO_TRIGGER_MUSIC_SYSTEM = 2000f
    val DISTANCE_TO_TRIGGER_MUSIC_HYPERSPACE = 1.5f

    private val music: Music = try {
        Music(Global.getSettings().loadJSON("data/config/sounds.json", "symbiotic_void_creatures").getJSONObject("music"));
    } catch (e: Exception){
        println(e)
        throw Exception("Failed to load music files for Symbiotic Void Creatures")
    }

    var lastCheckState: GameState = GameState.TITLE
    var isInHyperspace = false
    var timer = 0f;

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        timer += amount

        if (Global.getCurrentState() == GameState.TITLE) {
            return
        }

        val currentState = Global.getCurrentState()

        if (lastCheckState != currentState || Global.getSector().playerFleet.isInHyperspace != isInHyperspace) {
            lastCheckState = currentState
            isInHyperspace = Global.getSector().playerFleet.isInHyperspace
            timer = 10f // set it to a high number to trigger the checks below
        }

        when(currentState){
            GameState.CAMPAIGN -> {
                stopBattleTheme()

                if (music.isMusicPlaying(MusicID.SVC_VOIDLING_EXPLORATION_THEME) || 5f > timer){
                    return
                }

                if(isInHyperspace) {
                    if (isAnyVoidlingFleetInDistanceHyperspace(DISTANCE_TO_TRIGGER_MUSIC_HYPERSPACE)){
                        playExplorationTheme()
                    } else {
                        stopExplorationTheme()
                    }
                    return
                }

                if(FleetSpawner.getFactionFleets(SVC_FACTION_ID).any {
                    it.containingLocation == Global.getSector().playerFleet.containingLocation && Misc.getDistance(it, Global.getSector().playerFleet) <= DISTANCE_TO_TRIGGER_MUSIC_SYSTEM
                }){
                    playExplorationTheme()
                } else if(music.isMusicPlaying(MusicID.SVC_VOIDLING_EXPLORATION_THEME)){
                    stopExplorationTheme()
                }

                timer = 0f
            }
            GameState.COMBAT -> {
                if (5f > timer){
                    return
                }

                val fleet = Global.getCombatEngine()?.getFleetManager(FleetSide.ENEMY)?.deployedCopy?.filterNotNull()
                    ?.firstOrNull()?.fleetData?.fleet

                if (fleet == null) {
                    return
                }

                if (fleet.memoryWithoutUpdate[MASTERMIND_FLEET_MEMKEY] == true){
                    playBattleTheme(true)
                } else if (fleet.faction == Global.getSector().getFaction(SVC_FACTION_ID)){
                    playBattleTheme()
                } else if (fleet.faction == Global.getSector().getFaction(MMM_FACTION_ID)) {
                    playBattleTheme(false)
                }else {
                        stopBattleTheme()
                }

                timer = 0f
            }
            else -> return
        }
    }

    private fun playExplorationTheme(){
        if (!music.isMusicPlaying(MusicID.SVC_VOIDLING_EXPLORATION_THEME)){
            Global.getSoundPlayer().playCustomMusic(1, 1, MusicID.SVC_VOIDLING_EXPLORATION_THEME.toMusicPlayerFormat(), false)
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
            Global.getSoundPlayer().playCustomMusic(1, 1, MusicID.SVC_VOIDLING_BATTLE_THEME_GLITCHED.toMusicPlayerFormat(), true)
        } else {
            if(music.isMusicPlaying(MusicID.SVC_VOIDLING_BATTLE_THEME)) return
            Global.getSoundPlayer().playCustomMusic(1, 1, MusicID.SVC_VOIDLING_BATTLE_THEME.toMusicPlayerFormat(), true)
        }
    }
}