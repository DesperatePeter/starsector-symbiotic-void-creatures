package tecrys.svc.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.util.HashMap;
import java.util.Map;

public class BaseAnimateOnReloadEffect implements EveryFrameWeaponEffectPlugin {
    // Default to 15 frames per second
    private float timeSinceLastFrame, timeBetweenFrames = 1.0f / 20f;
    private final Map pauseFrames = new HashMap();
    private int curFrame = 0, pausedFor = 0;
    private boolean isReloading = false;

    protected void setFramesPerSecond(float fps) {
        timeBetweenFrames = 1.0f / fps;
    }

    protected void pauseOnFrame(int frame, int pauseFor) {
        pauseFrames.put(frame, pauseFor);
    }

    private void incFrame(AnimationAPI anim) {
        if (pauseFrames.containsKey(curFrame)) {
            if (pausedFor < (Integer) pauseFrames.get(curFrame)) {
                pausedFor++;
                return;
            } else {
                pausedFor = 0;
            }
        }

        curFrame = Math.min(curFrame + 1, anim.getNumFrames() - 1);
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon.getAnimation() == null) {
            return;
        }

        AnimationAPI anim = weapon.getAnimation();
        anim.setFrame(curFrame);

        if (isReloading) {
            timeSinceLastFrame += amount;

            while (timeSinceLastFrame >= timeBetweenFrames) {
                timeSinceLastFrame -= timeBetweenFrames;
                incFrame(anim);
            }

            anim.setFrame(curFrame);
            if (curFrame == anim.getNumFrames() - 1) {
                isReloading = false;
            }
        } else {
            if (weapon.getCooldownRemaining() < 1 && weapon.getAmmoTracker().getReloadProgress() >= 0.93) {
                isReloading = true;
                incFrame(anim);
                anim.setFrame(curFrame);
            } else {
                curFrame = 0;
                anim.setFrame(curFrame);
            }
        }
    }
}