package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.getEffectiveShipTarget

class BerserkerFrenzyAI: ShipSystemAIScript {
    companion object{
        const val NUMBER_WEAPONS_TO_CONSIDER = 4
    }
    private var engine: CombatEngineAPI? = null
    private var ship: ShipAPI? = null
    private var system: ShipSystemAPI? = null
    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.engine = engine
        this.ship = ship
        this.system = system
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        val tgt = target ?: return
        ship?.run {
            var weapons = allWeapons.toMutableList()
            weapons.sortBy { - it.damage.damage }
            if(weapons.size > NUMBER_WEAPONS_TO_CONSIDER) weapons = weapons.subList(0, NUMBER_WEAPONS_TO_CONSIDER)
            if(weapons.all {
                tgt.exactBounds?.update(tgt.location, tgt.facing)
                it.range > (it.location - CollisionUtils.getNearestPointOnBounds(it.location, tgt)).length()
                }){
                useSystem()
            }
        }
    }
}