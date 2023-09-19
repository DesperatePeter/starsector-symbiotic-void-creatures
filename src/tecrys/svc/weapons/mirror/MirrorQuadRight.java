package tecrys.svc.weapons.mirror;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponEffectPluginWithInit;
import com.fs.starfarer.api.combat.WeaponAPI;
import tecrys.svc.utils.DecoUtils;
import tecrys.svc.utils.Mirror;

// Original Code by Wyvern
// Mirrors weapons that are on-or-to-the-right of the ship's centerline and face forwards,
// or that are left of the centerline and face backwards-or-exactly-to-the-side.
public class MirrorQuadRight extends Mirror {

    @Override
    public void init( WeaponAPI weapon ) {
        if( DecoUtils.isOnLeft( weapon ) == DecoUtils.isFacingForward( weapon )) {
            isMirrored = true;
            super.init( weapon );
        }
    }
}
