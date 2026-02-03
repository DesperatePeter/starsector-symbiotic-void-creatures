package tecrys.svc.world

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import org.magiclib.kotlin.forEach
import tecrys.svc.MMM_FACTION_ID
import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.utils.isAnyVoidlingFleetInDistanceHyperspace
import tecrys.svc.utils.isMastermindFleet
import tecrys.svc.world.fleets.FleetSpawner
import tecrys.svc.world.fleets.MASTERMIND_FLEET_MEMKEY

class ContextBaseMusicPlayer: EveryFrameScript, EveryFrameCombatPlugin {
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

    val DISTANCE_TO_TRIGGER_MUSIC_SYSTEM = 4000f
    val DISTANCE_TO_TRIGGER_MUSIC_HYPERSPACE = 0.5f

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

    override fun runWhilePaused(): Boolean = true

    override fun advance(amount: Float) {
        timer += amount

        if (Global.getCurrentState() == GameState.TITLE) {
            return
        }

        val currentState = Global.getCurrentState()

        if ((lastCheckState != currentState || Global.getSector().playerFleet.isInHyperspace != isInHyperspace) && Global.getSector().playerFleet != null) {
            lastCheckState = currentState
            isInHyperspace = Global.getSector().playerFleet.isInHyperspace
            timer = 10f // set it to a high number to trigger the checks below
        }

        when(currentState){
            GameState.CAMPAIGN -> {


                if (5f > timer){
                    return
                }

                if (Global.getSector().campaignUI.isShowingDialog){
                    return;
                }

                if(isInHyperspace) {
//                    if (isAnyVoidlingFleetInDistanceHyperspace(DISTANCE_TO_TRIGGER_MUSIC_HYPERSPACE)){
//                        playExplorationTheme()
//                    } else {
//                        stopExplorationTheme()
//                    }
//                    return
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
                    ?.firstOrNull()?.fleetData?.fleet ?: return

                if (fleet.isMastermindFleet()){
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
        val fleet = Global.getCombatEngine()?.getFleetManager(FleetSide.ENEMY)?.deployedCopy?.filterNotNull()
            ?.firstOrNull()?.fleetData?.fleet ?: return
        if (fleet == null)stopBattleTheme()
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
            Global.getSoundPlayer().playCustomMusic(1, 1, MusicID.SVC_VOIDLING_BATTLE_THEME_GLITCHED.toMusicPlayerFormat(), false)
        } else {
            if(music.isMusicPlaying(MusicID.SVC_VOIDLING_BATTLE_THEME)) return
            Global.getSoundPlayer().playCustomMusic(1, 1, MusicID.SVC_VOIDLING_BATTLE_THEME.toMusicPlayerFormat(), false)
        }
    }

    override fun processInputPreCoreControls(
        p0: Float,
        p1: List<InputEventAPI?>?
    ) {
        return
    }

    override fun advance(p0: Float, p1: List<InputEventAPI?>?) {
        this.advance(p0)
    }

    override fun renderInWorldCoords(p0: ViewportAPI?) {
        return
    }

    override fun renderInUICoords(p0: ViewportAPI?) {
        return
    }

    override fun init(p0: CombatEngineAPI?) {
        return
    }
}