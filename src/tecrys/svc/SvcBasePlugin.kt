package tecrys.svc

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.econ.impl.ItemEffectsRepo
import com.thoughtworks.xstream.XStream
import org.dark.shaders.light.LightData
import org.dark.shaders.util.ShaderLib
import org.dark.shaders.util.TextureData
import tecrys.svc.plugins.svc_addBooze.addBooze
import tecrys.svc.world.SectorGen
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.notifications.NotificationShower

import tecrys.svc.plugins.svc_addBooze.addBooze
import tecrys.svc.plugins.svc_addBooze.addBoozeToFaction

/**
 * A Kotlin version of ExampleModPlugin.java.
 * Purely for comparison and convenience; this will not be used by the game
 * unless mod_info.json is edited to use it
 * (or it is renamed to "ExampleModPlugin" in order to replace the Java version).
 */
class SvcBasePlugin : BaseModPlugin() {

    private fun initSVC() {
        try {

            Global.getSettings().scriptClassLoader.loadClass("data.scripts.world.ExerelinGen")


        } catch (ex: ClassNotFoundException) {

            SectorGen().generate(Global.getSector())

        }

    }

    override fun onGameLoad(newGame: Boolean) {
        Global.getSector().addTransientScript(FleetManager())
        Global.getSector().addTransientScript(NotificationShower())
        if(newGame){
            initSVC()
        }
    }
    override fun onApplicationLoad() {


        //add special items

        if (Global.getSettings().modManager.isModEnabled("alcoholism")) {
            addBooze()
        } else {
            Global.getSettings().getCommoditySpec("svc_cocktail_c").basePrice = 0f
            Global.getSettings().getCommoditySpec("svc_cocktail_c").exportValue = 0f
            Global.getSettings().getCommoditySpec("svc_cocktail_c").tags.add("nonecon")
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
}