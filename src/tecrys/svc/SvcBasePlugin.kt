package tecrys.svc

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.combat.MissileAIPlugin
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager
import com.thoughtworks.xstream.XStream
import org.dark.shaders.util.ShaderLib
import org.magiclib.util.MagicSettings
import tecrys.svc.colonycrisis.SymbioticCrisisCause
import tecrys.svc.listeners.SvcCargoListener
import tecrys.svc.modintegration.*
import tecrys.svc.plugins.exotica.VoidChitinPlatingListener
import tecrys.svc.plugins.substanceabuse.addCocktailBreweryToRelevantMarkets
import tecrys.svc.plugins.substanceabuse.disableSubstanceAbuse
import tecrys.svc.plugins.substanceabuse.giveCocktailToPirates
import tecrys.svc.plugins.substanceabuse.loadSubstanceAbuse
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.utils.unlockVoidlingRecovery
import tecrys.svc.weapons.scripts.pWormAI
import tecrys.svc.world.ContextBaseMusicPlayer
import tecrys.svc.world.SectorGen
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.ghosts.HunterGhostCreator
import tecrys.svc.world.notifications.NotificationShower
import kotlin.collections.plus

/**
 * A Kotlin version of ExampleModPlugin.java.
 * Purely for comparison and convenience; this will not be used by the game
 * unless mod_info.json is edited to use it
 * (or it is renamed to "ExampleModPlugin" in order to replace the Java version).
 */
class SvcBasePlugin : BaseModPlugin() {

    companion object{
        const val WORMS_LARGE_ID: String = "svc_worm_shot"
        var relationInitIsDone: Boolean by CampaignSettingDelegate("$${SVC_MOD_ID}isRelationshipInitDone", false)
    }


    private fun initSVC() {
        try {
            Global.getSettings().scriptClassLoader.loadClass("data.scripts.world.ExerelinGen")
        } catch (_: ClassNotFoundException) {
            SectorGen().initializeRelationships()
        }
        if(isSubstanceAbuseEnabled()){
            addCocktailBreweryToRelevantMarkets()
            giveCocktailToPirates()
        }
    }

    override fun onGameLoad(newGame: Boolean) {
        if(SensorGhostManager.CREATORS.none { it.javaClass == HunterGhostCreator::class.java }){
            SensorGhostManager.CREATORS.add(HunterGhostCreator())
        }
        Global.getSector().registerPlugin(SvcCampaignPlugin())
        Global.getSector().addTransientScript(FleetManager())
        Global.getSector().addTransientScript(NotificationShower())
        Global.getSector().addTransientScript(ContextBaseMusicPlayer())
        if(isExoticaEnabled()){
            Global.getSector().addTransientScript(VoidChitinPlatingListener())
        }
        Global.getSector().listenerManager.addListener(SvcCargoListener)
        unlockVoidlingRecovery()
        if(!relationInitIsDone){
            initSVC()
            relationInitIsDone = true
        }
        SymbioticCrisisCause.initializeEvent()
    }

    override fun onApplicationLoad() {
        //add special items

        ShaderLib.init();

        if (isSubstanceAbuseEnabled()) {
            loadSubstanceAbuse()
        } else {
            disableSubstanceAbuse()
        }

        if(isStarshipLegendsEnabled()){
            flagHullsAsBiologicalForLegends(getAllBiologicalHullIds())
        }

        if(isExoticaEnabled()){
            Global.getSettings().getSpecialItemSpec(VOID_CHITIN_ID).desc += " Can be used to install Void Chitin Plating."
            // TODO set special item plugin
        // Global.getSettings().getSpecialItemSpec(VOID_CHITIN_PLATING_EXOTICA_CHIP_ID).
            //exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
        }
    }
    /**
     * Tell the XML serializer to use custom naming, so that moving or renaming classes doesn't break saves.
     */
    override fun configureXStream(x: XStream) {
        super.configureXStream(x)
        // This will make it so that whenever "ExampleEveryFrameScript" is put into the save game xml file,
        // it will have an xml node called "ExampleEveryFrameScript" (even if you rename the class!).
        // This is a way to prevent refactoring from breaking saves, but is not required to do.

        // x.alias("ExampleEveryFrameScript", ExampleEveryFrameScript::class.java)
    }
    override fun pickMissileAI(missile: MissileAPI, launchingShip: ShipAPI?): PluginPick<MissileAIPlugin>? {
        when (missile.projectileSpecId) {
            WORMS_LARGE_ID -> return PluginPick<MissileAIPlugin>(
                pWormAI(
                    missile,
                    launchingShip
                ), CampaignPlugin.PickPriority.MOD_SPECIFIC
            )
            else -> {}
        }
        return null
    }
}