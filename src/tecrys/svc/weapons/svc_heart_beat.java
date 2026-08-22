package tecrys.svc.weapons; // Change this to your mod's package

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPluginWithAdvanceAfter;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponEffectPluginWithInit;
import com.fs.starfarer.api.graphics.SpriteAPI;
import tecrys.svc.utils.DecoUtils; // Importing the provided utility

import java.util.HashSet;
import java.util.Set;

public class svc_heart_beat implements EveryFrameWeaponEffectPluginWithAdvanceAfter, WeaponEffectPluginWithInit {

    // Configuration
    private static final float MIN_CYCLES = 1.8f;       // Resting heart rate (0.8 cycles/sec = ~48 BPM)
    private static final float MAX_CYCLES_HP = 2.5f;    // Near death heart rate (150 BPM)
    private static final float SYSTEM_CYCLES = 4.0f;    // Adrenaline rush during system activation
    private static final float PULSE_MAGNITUDE = 0.15f; // Growth factor (0.15 = +15% size)

    // State Variables
    private float phase = 0f;
    private boolean initializedDimensions = false;
    private float baseWidth = -1f;
    private float baseHeight = -1f;
    private float baseCenterX = -1f;
    private float baseCenterY = -1f;

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
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;

        ShipAPI ship = weapon.getShip();
        if (ship == null) return;

        SpriteAPI sprite = weapon.getSprite();
        if (sprite == null) return;

        // 1. Capture base dimensions
        // If init() flipped the sprite, baseWidth and baseCenterX will correctly be captured as negative values.
        if (!initializedDimensions) {
            baseWidth = sprite.getWidth();
            baseHeight = sprite.getHeight();
            baseCenterX = sprite.getCenterX();
            baseCenterY = sprite.getCenterY();
            initializedDimensions = true;
        }

        // Reset if destroyed
        if (!ship.isAlive()) {
            sprite.setSize(baseWidth, baseHeight);
            sprite.setCenter(baseCenterX, baseCenterY);
            return;
        }

        // 2. Calculate dynamic Cycle Rate
        float hpFraction = Math.max(0f, Math.min(1f, ship.getHitpoints() / ship.getMaxHitpoints()));
        float currentBPS = MIN_CYCLES + ((1f - hpFraction) * (MAX_CYCLES_HP - MIN_CYCLES));

        ShipSystemAPI system = ship.getSystem();
        if (system != null) {
            float effectLevel = system.getEffectLevel();
            currentBPS = currentBPS + (effectLevel * (SYSTEM_CYCLES - currentBPS));
        }

        // 3. Advance the normalized phase (0.0 to 1.0 represents one full Lub-Dub-Pause cycle)
        phase += amount * currentBPS;
        if (phase > 1f) {
            phase -= 1f;
        }

        // 4. Calculate the Lub-Dub shape
        float pulse = 0f;

        // "Lub": 0% to 15% of the cycle
        if (phase < 0.15f) {
            pulse = (float) Math.sin(phase * (Math.PI / 0.15));
        }
        // Short Pause: 15% to 25% of the cycle
        // "Dub": 25% to 40% of the cycle
        else if (phase >= 0.25f && phase < 0.40f) {
            pulse = (float) Math.sin((phase - 0.25f) * (Math.PI / 0.15));
        }
        // Long Pause: 40% to 100% of the cycle (pulse remains 0)

        // Square the output to make the beats feel sharper and more muscular
        pulse = (float) Math.pow(pulse, 2);

        float scale = 1f + (pulse * PULSE_MAGNITUDE);

        // 5. Apply to Sprite
        // Multiplying the (potentially negative) base dimensions by a positive scale maintains mirroring
        sprite.setSize(baseWidth * scale, baseHeight * scale);
        sprite.setCenter(baseCenterX * scale, baseCenterY * scale);
    }
}