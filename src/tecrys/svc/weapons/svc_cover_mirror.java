package tecrys.svc.weapons; // Change this to your mod's package

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import tecrys.svc.utils.DecoUtils;

import java.util.HashSet;
import java.util.Set;

public class svc_cover_mirror implements EveryFrameWeaponEffectPluginWithAdvanceAfter, WeaponEffectPluginWithInit {


    // Mirroring Variables[cite: 1]
    protected Set<Integer> frames = null;
    protected boolean isMirrored = false;

    @Override
    public void init(WeaponAPI weapon) {
        // BUGFIX: We MUST clone the spec for ALL instances of this weapon.
        // If we don't, the unmirrored heart modifies the global sprite cache,
        // permanently altering the center point for the ship editor and future battles!
        weapon.ensureClonedSpec();

        // Check if the weapon slot is on the left side of the ship (+Y axis)[cite: 2]
        if (DecoUtils.isOnLeft(weapon)) {
            isMirrored = true;
            DecoUtils.mirror(weapon, false); // Mirror the weapon and its offsets[cite: 1, 2]

            // If the weapon is animated, track the initial frame[cite: 1]
            if (weapon.getAnimation() != null) {
                frames = new HashSet<>();
                frames.add(weapon.getAnimation().getFrame());
            }
        }
    }

    @Override
    public void advanceAfter(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        // Ensure that new animation frames loaded by the engine are also mirrored[cite: 1]
        if (isMirrored && weapon.getAnimation() != null) {
            if (frames == null) {
                frames = new HashSet<>();
            }
            int frame = weapon.getAnimation().getFrame();
            if (!frames.contains(frame)) {
                DecoUtils.mirror(weapon, true); // Mirror just the sprites of the new frame[cite: 1, 2]
                frames.add(frame);
            }
        }
    }

    @Override
    public void advance(float v, CombatEngineAPI combatEngineAPI, WeaponAPI weaponAPI) {

    }
}