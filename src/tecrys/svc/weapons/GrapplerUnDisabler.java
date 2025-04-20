package tecrys.svc.weapons;

import com.fs.starfarer.api.combat.*;

public class GrapplerUnDisabler implements EveryFrameWeaponEffectPlugin {

    @Override
    public void advance(float v, CombatEngineAPI combatEngineAPI, WeaponAPI weapon) {
        if (weapon.getCurrHealth() < weapon.getMaxHealth()) {
            weapon.setCurrHealth(weapon.getMaxHealth());
        }
        if (weapon.isDisabled()) {
            weapon.repair();
        }
    }
}