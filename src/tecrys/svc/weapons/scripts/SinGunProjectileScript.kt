package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.EmpArcEntityAPI
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.randomlyVaried
import tecrys.svc.utils.times
import tecrys.svc.weapons.scripts.SinGunSoundPlayer.Companion.PITCH
import tecrys.svc.weapons.scripts.SinGunSoundPlayer.Companion.SOUND_ID
import tecrys.svc.weapons.scripts.SinGunSoundPlayer.Companion.VOLUME
import java.awt.Color


class SinGunProjectileScript(projs: List<DamagingProjectileAPI>, weaponAngle: Float) : BaseEveryFrameCombatPlugin() {
    companion object {
        const val RUBBER_BAND_STRENGTH = 10f // acceleration per unit distance
        const val DIST_SCALE_CONST = 100f
        const val MULT_NAME = "SvcSinGunDist"
        const val MANAGER_CUSTOM_KEY = "\$SVC_singun_manager"
        const val AMOUNT_TO_SKIP = 0.2f
        const val EMP_ARC_THICKNESS_MULT = 3f
        const val EMP_ARC_FREQUENCY = 0.05f
        const val EMP_ARC_FREQUENCY_RNG = 0.1f // value between 0f and 1f
        const val SPAWN_VARIANT1_ARCS = false
        const val SPAWN_VARIANT2_ARCS = false
        const val SPAWN_VARIANT3_ARCS = true
        private var ARC_COLOR: Color = Color(255, 4, 203, 180)
        private var ARC_GLOW_COLOR: Color = Color(104, 242, 255, 155)
        private val colorVariation: Float = 500f
        protected val color = ARC_COLOR.randomlyVaried(colorVariation / 2f)
        private val shotInterval = IntervalUtil(1f, 1.6f)

        fun computeDamageMult(distance: Float): Float{
            return (2f - distance/ DIST_SCALE_CONST).coerceIn(0.5f, 2f)
        }
        @Suppress("UNCHECKED_CAST")
        class SinGunScriptManager{
            private val scripts: MutableMap<WeaponAPI, MutableList<SinGunProjectileScript>>
                get(){
                    Global.getCombatEngine()?.customData?.let { data ->
                        if(!data.containsKey(MANAGER_CUSTOM_KEY)){
                            data[MANAGER_CUSTOM_KEY] = mutableMapOf<WeaponAPI, MutableList<SinGunProjectileScript>>()
                        }
                        return (data[MANAGER_CUSTOM_KEY] as MutableMap<WeaponAPI, MutableList<SinGunProjectileScript>>)
                    }
                    return mutableMapOf()
                }
            fun addScript(script: SinGunProjectileScript){
                if(!scripts.containsKey(script.outermostProjectile.weapon)){
                    scripts[script.outermostProjectile.weapon] = mutableListOf()
                }
                scripts[script.outermostProjectile.weapon]?.add(script)
                scripts[script.outermostProjectile.weapon]?.sortBy { it.creationTime }
            }
            fun removeScript(script: SinGunProjectileScript){
                scripts[script.outermostProjectile.weapon]?.remove(script)
            }
            fun getNextProjectiles(script: SinGunProjectileScript): List<DamagingProjectileAPI>?{
                return scripts[script.outermostProjectile.weapon]?.find { it.creationTime > script.creationTime }?.projectiles
            }
            fun isFirst(script: SinGunProjectileScript): Boolean{
                return scripts[script.outermostProjectile.weapon]?.last() == script
            }
        }
        val sinGunScriptManager = SinGunScriptManager()
    }

    private val orthVector = Misc.getUnitVectorAtDegreeAngle(weaponAngle + 90f)
    private val normVector = Misc.getUnitVectorAtDegreeAngle(weaponAngle)
    private val outermostProjectile = projs.maxBy { p1 ->
        projs.maxOfOrNull { (it.location - p1.location) * orthVector } ?: 0f
    }
    private val projectiles = projs.sortedBy { p ->
        (p.location - outermostProjectile.location) * orthVector
    }
    // FIXME: Are outer and inner projectiles swapped???
    private val outerProjectiles = Pair(projectiles.first(), projectiles.last())
    private val innerProjectiles = Pair(projectiles[1], projectiles[2])
    private var amountToSkip = AMOUNT_TO_SKIP
    private var timer = 0f
    private val creationTime = Global.getCombatEngine()?.getTotalElapsedTime(false) ?: 0f

    init {
        if (projs.size != 4) {
            Global.getLogger(this.javaClass)
                .error("Spawned SinGunScript with a number of projectiles != 4. Game might crash!")
        }
        sinGunScriptManager.addScript(this)
        projectiles.forEach { spawnEmpArcsToWeapon(it) }
    }


    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine() ?: return
        if(engine.isPaused) return
        if(amountToSkip - amount > 0f){
            amountToSkip -= amount
            return
        }
        shotInterval.advance(amount)
        adjustProjectileVelocity(amount)
        var shouldSpawnArcs = false
        timer -= amount
        if(timer <= 0f){
            shouldSpawnArcs = true
            timer = (1f - EMP_ARC_FREQUENCY_RNG) * EMP_ARC_FREQUENCY + (Math.random().toFloat() * EMP_ARC_FREQUENCY_RNG * EMP_ARC_FREQUENCY)
        }
        listOf(innerProjectiles, outerProjectiles).forEach {
            modifyProjectileDamage(it)
            // VARIANT 1
            if(shouldSpawnArcs && SPAWN_VARIANT1_ARCS) spawnEmpArcsForProjectiles(it)
        }
        // VARIANT 2
        if(shouldSpawnArcs && SPAWN_VARIANT2_ARCS){
            spawnEmpArcsForProjectiles(Pair(innerProjectiles.first, outerProjectiles.first))
            spawnEmpArcsForProjectiles(Pair(innerProjectiles.second, outerProjectiles.second))
        }
        // VARIANT 3
        if(shouldSpawnArcs && SPAWN_VARIANT3_ARCS){
            sinGunScriptManager.getNextProjectiles(this)?.let { otherProjectiles ->
                projectiles.zip(otherProjectiles).forEach {
                    spawnEmpArcsForProjectiles(it)

                }
            }
        }
        if(shouldSpawnArcs && SPAWN_VARIANT3_ARCS && sinGunScriptManager.isFirst(this)){
            projectiles.forEach {
                spawnEmpArcsToWeapon(it)
            }
        }
        if(isDone()){
            engine.removePlugin(this)
            sinGunScriptManager.removeScript(this)
        }
    }

    private fun adjustProjectileVelocity(amount: Float) {
        // NOTE: Projectiles in the starsector combat engine are kind of fucked
        // Their movement is entirely defined by their facing and moveSpeed, their velocity seems to get ignored

        listOf(outerProjectiles, innerProjectiles).forEach {
            val forceToApply = it.second.location - it.first.location
            forceToApply.scale(amount * RUBBER_BAND_STRENGTH)
            listOf(it.second, it.first).forEach { p ->
                // we want to make sure that the lateral speed doesn't become the main velocity component
                forceToApply.scale(-1f)
                val targetSpeed = p.moveSpeed
                val velocity = targetSpeed * Misc.getUnitVectorAtDegreeAngle(p.facing)
                p.velocity.set(velocity.x, velocity.y)
                val normalSpeed = getNormalVelocityComponent(p.velocity)
                val desiredLateralSpeed = getLateralVelocityComponent(p.velocity) + forceToApply
                if(desiredLateralSpeed.length() > targetSpeed * 0.5f){
                    desiredLateralSpeed.scale(targetSpeed * 0.5f / desiredLateralSpeed.length())
                }
                Vector2f.add(normalSpeed, desiredLateralSpeed, p.velocity)
            }

        }
        projectiles.forEach {
            it.facing = Misc.getAngleInDegrees(it.velocity)
        }
    }

    private fun spawnEmpArcsForProjectiles(projectilePair: Pair<DamagingProjectileAPI, DamagingProjectileAPI>){
        val dist = (projectilePair.first.location - projectilePair.second.location).length()
        val mult = computeDamageMult(dist)
        val (p0, p1) = projectilePair
        val thickness = EMP_ARC_THICKNESS_MULT * (mult + 0.1f)
        val params = createEmpParams()
        if(dist > 300f) return
        if(Math.random() > 0.5f){
            Global.getCombatEngine()?.spawnEmpArcVisual(p0.location, p0, p1.location, p1, thickness, ARC_GLOW_COLOR, ARC_COLOR,
                params)
        }else{
            Global.getCombatEngine()?.spawnEmpArcVisual(p1.location, p1, p0.location, p0, thickness, ARC_GLOW_COLOR, ARC_COLOR,
                params)
        }
        if (shotInterval.intervalElapsed()){
            Global.getSoundPlayer()?.playSound(SOUND_ID, PITCH, VOLUME, p1.location, p1.velocity)

        }
    }

    private fun spawnEmpArcsToWeapon(projectile: DamagingProjectileAPI){
        val thickness = EMP_ARC_THICKNESS_MULT
        val params = createEmpParams()
        if((projectile.location - projectile.weapon.location).length() > 150f) return
        Global.getCombatEngine()?.spawnEmpArcVisual(projectile.location, projectile, projectile.weapon.location, projectile.weapon.ship,
            thickness, ARC_GLOW_COLOR, ARC_COLOR,  params) //?.setFadedOutAtStart(true)


    }

    private fun createEmpParams(): EmpArcParams {
        val params = EmpArcParams()
        params.fadeOutDist = 1f
        params.glowAlphaMult = 0f
        //params.glowColorOverride = Color(0, 0, 0, 0)
        params.maxZigZagMult = 2f
        params.segmentLengthMult = 0.1f
        //params.flickerRateMult = 3f
        params.flickerRateMult = 2f
        params.minFadeOutMult = 100f
        params.movementDurOverride = -1f
        params.movementDurMax = 0.1f
        params.movementDurMin = 0f
        return params
    }


    private fun modifyProjectileDamage(projectilePair: Pair<DamagingProjectileAPI, DamagingProjectileAPI>){
        val dist = (projectilePair.first.location - projectilePair.second.location).length()
        val mult = computeDamageMult(dist)
        listOf(projectilePair.first, projectilePair.second).forEach {
            it.damage.modifier.modifyMult(MULT_NAME, mult)
        }
    }

    private fun getNormalVelocityComponent(velocity: Vector2f): Vector2f{
        return (normVector * velocity) * normVector
    }

    private fun getLateralVelocityComponent(velocity: Vector2f): Vector2f{
        return velocity - getNormalVelocityComponent(velocity)
    }

    private fun isDone(): Boolean{
        return projectiles.all { it.isFading || it.isExpired }
    }
}