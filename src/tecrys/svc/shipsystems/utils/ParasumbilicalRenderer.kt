package tecrys.svc.shipsystems.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.CombatPlugin
import tecrys.svc.utils.postRender
import tecrys.svc.utils.preRender
import java.util.*

class ParasumbilicalRenderer: BaseCombatLayeredRenderingPlugin() {

    companion object{
        const val TEX_HEIGHT = 1f
        const val TEX_WIDTH = 0.25f
        const val TEX_SCROLL_SPEED = 0.01f
    }

    val spritesToRenderOneFrame = mutableListOf<RenderableSprite>()

    data class RenderableSprite(val sprite: SpriteAPI, val alpha: Float, val width: Float, val height: Float, val angleDeg: Float, val pos: Vector2f)

    private var counter = 0f

    private fun renderSprites(viewport: ViewportAPI){
        if(Global.getCombatEngine()?.isPaused != true){
            counter += TEX_SCROLL_SPEED
            if(counter + TEX_WIDTH > 1f) counter = 0f
        }
        val viewMult = 1f //
        spritesToRenderOneFrame.forEach { s->
            s.sprite.run {
                alphaMult = s.alpha
                setSize(s.width / viewMult, (s.height + 60f)  / viewMult)
                setAdditiveBlend()
                angle = s.angleDeg - 90f
                setTexHeight(1f)
                setTexWidth(1f)
                setTexY(0f)
                setTexX(counter)
                renderAtCenter(s.pos.x, s.pos.y)


                // 32 x 256, lower 128 empty
                // renderRegionAtCenter()
                // renderAtCenter(viewport.convertWorldXtoScreenX(s.pos.x), viewport.convertWorldYtoScreenY(s.pos.y))
            }
        }
        if(Global.getCombatEngine()?.isPaused != true)
            spritesToRenderOneFrame.clear()
    }

    override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
        if(layer != CombatEngineLayers.BELOW_SHIPS_LAYER) return
        viewport?.let {
            renderSprites(viewport)
        }

    }

    override fun getRenderRadius(): Float = 10000.0f

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> = EnumSet.of(CombatEngineLayers.BELOW_SHIPS_LAYER)
}