package tecrys.svc.weapons.mirror;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponEffectPluginWithInit;
import com.fs.starfarer.api.combat.WeaponAPI;
import tecrys.svc.utils.DecoUtils;

// Original Code by Wyvern
public class BaseAnimateOnFireEffectMirror extends tecrys.svc.weapons.BaseAnimateOnFireEffect implements EveryFrameWeaponEffectPlugin, WeaponEffectPluginWithInit {
    // Mirrors weapons that are on-or-to-the-right of the ship's centerline and face forwards,
// or that are left of the centerline and face backwards-or-exactly-to-the-side.
    public BaseAnimateOnFireEffectMirror() {
        super();
    }

    @Override
    public void advance( float amount, CombatEngineAPI engine, WeaponAPI weapon ) {
        super.advance( amount, engine, weapon );
    }

    @Override
    public void init( WeaponAPI weapon ) {
        if( DecoUtils.isOnLeft( weapon ) != DecoUtils.isFacingForward( weapon )  ) {
            DecoUtils.mirror( weapon, false );
        }
    }
}
