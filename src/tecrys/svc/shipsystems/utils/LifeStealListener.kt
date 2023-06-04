package tecrys.svc.shipsystems.utils

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import org.lwjgl.util.vector.Vector2f
import kotlin.math.min

class LifeStealListener(private val ship: ShipAPI, private val lifeLink: Float) : DamageDealtModifier {
    override fun modifyDamageDealt(
        param: Any?,
        target: CombatEntityAPI?,
        damage: DamageAPI?,
        point: Vector2f?,
        shieldHit: Boolean
    ): String? {
        ship.hitpoints = min(ship.hitpoints + lifeLink * (damage?.damage ?: 0f), ship.maxHitpoints)
        return null
    }
}