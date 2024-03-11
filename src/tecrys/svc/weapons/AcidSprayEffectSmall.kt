package tecrys.svc.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.ext.plus
import tecrys.svc.utils.times
import tecrys.svc.utils.vectorFromAngleDeg
import tecrys.svc.weapons.scripts.AcidSprayScript
import java.awt.Color

class AcidSprayEffectSmall: AcidSprayEffectImpl() {
    override val effectDuration: Float
        get() = 1.3f
    override val initialEffectRadius: Float
        get() =  1f
    override val effectRadiusGrowth: Float
        get() = 25f
    override val effectSpeed: Float
        get() = 200f
    override val effectColor: Color
        get() = Color(104, 128, 0, 20)
}


