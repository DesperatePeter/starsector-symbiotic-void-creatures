package tecrys.svc.shipsystems.spooky

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import java.awt.Color

private fun floatyText(txt: String, targetShip: ShipAPI?){
    Global.getCombatEngine()?.addFloatingText(targetShip?.location, txt,
        28f, Color.RED, targetShip, 2f, 4.0f)
}

fun sabotageDrive(targetShip: ShipAPI?, chance: Float = 1.0f) {
    targetShip?.engineController?.shipEngines?.filter{Math.random() > chance}?.forEach { e -> targetShip.applyCriticalMalfunction(e, false) }
    floatyText("Rogue Engineer", targetShip)
}

fun sabotageCrew(targetShip: ShipAPI?, amount: Float = 0.25f){
    targetShip?.currentCR = targetShip.currentCR.minus(amount).coerceIn(0f, 100f)
    floatyText("Violent Mutiny", targetShip)
}

fun sabotageWeapons(targetShip: ShipAPI?, chance: Float = 0.5f){
    targetShip?.allWeapons?.filter { Math.random() > chance }?.forEach { w -> targetShip.applyCriticalMalfunction(w, false) }
    floatyText("Weapons Crew Compromised", targetShip)
}

fun mindControl(targetShip: ShipAPI?, durationMultiplier: Float = 1f) {
    targetShip ?: return
    Global.getCombatEngine()?.addPlugin(SpookyMindControl(targetShip, durationMultiplier))
    floatyText("Insane Captain", targetShip)
}
