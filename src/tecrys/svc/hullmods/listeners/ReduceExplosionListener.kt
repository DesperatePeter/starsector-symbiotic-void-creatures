package tecrys.svc.hullmods.listeners

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import org.lwjgl.util.vector.Vector2f

class ReduceExplosionListener : DamageTakenModifier {

    companion object{
        private const val EXPLOSION_CLASS_NAME = "DamagingExplosion"
        private const val REDUCTION_SOURCE_NAME = "SVC_ANTI_SHIP_EXPLOSION"
        private const val DAMAGE_MODIFIER = 0.01f
    }
    override fun modifyDamageTaken(
        param: Any?, target: CombatEntityAPI?, damage: DamageAPI?,
        point: Vector2f?, shieldHit: Boolean
    ): String? {
        if(param?.javaClass?.name?.contains(EXPLOSION_CLASS_NAME) == true){
            damage?.modifier?.modifyMult(REDUCTION_SOURCE_NAME, DAMAGE_MODIFIER)
            return REDUCTION_SOURCE_NAME
        }
        return null
    }
}