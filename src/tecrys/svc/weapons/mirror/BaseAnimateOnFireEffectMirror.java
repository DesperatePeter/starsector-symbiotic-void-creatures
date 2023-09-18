package tecrys.svc.weapons.mirror;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponEffectPluginWithInit;
import tecrys.svc.utils.DecoUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Original Code by Wyvern
public class BaseAnimateOnFireEffectMirror extends tecrys.svc.weapons.BaseAnimateOnFireEffect implements EveryFrameWeaponEffectPlugin, WeaponEffectPluginWithInit {
    // Mirrors weapons that are on-or-to-the-right of the ship's centerline and face forwards,
// or that are left of the centerline and face backwards-or-exactly-to-the-side.
    public BaseAnimateOnFireEffectMirror() {
        super();
    }

    private Integer lastFrame = 0;
    private Set<Integer> mirroredFrames = new HashSet<>();

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        super.advance(amount, engine, weapon);
        int frame = weapon.getAnimation().getFrame();
        if(frame != lastFrame && !mirroredFrames.contains(frame)){
            mirrorIfNecessary(weapon);
            lastFrame = frame;
            mirroredFrames.add(frame);
        }
    }

    @Override
    public void init(WeaponAPI weapon) {
        mirrorIfNecessary(weapon);
        lastFrame = 0;
        mirroredFrames.add(0);
    }

    private void mirrorIfNecessary(WeaponAPI weapon) {
        if (DecoUtils.isOnLeft(weapon) && DecoUtils.isFacingForward(weapon)) {
            DecoUtils.mirror(weapon, false);
        }
    }
}
