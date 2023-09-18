package tecrys.svc.utils;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPluginWithAdvanceAfter;
import com.fs.starfarer.api.combat.WeaponEffectPluginWithInit;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.util.Set;
import java.util.HashSet;

// Original Code by Wyvern
// Contains all of the mechanics of mirroring weapons, but nothing that would actually set isMirrored to true.
// Will not work properly if the value of isMirrored gets changed after init.
public class Mirror implements EveryFrameWeaponEffectPluginWithAdvanceAfter, WeaponEffectPluginWithInit {
	protected Set frames = null; //TODO: change to Set<Integer> if compiling things into a jar.
	protected boolean isMirrored = false;

	public Mirror() {
	}
	
	@Override
	public void advance( float amount, CombatEngineAPI engine, WeaponAPI weapon ) {
	}
	
	@Override
	public void advanceAfter( float amount, CombatEngineAPI engine, WeaponAPI weapon ) {
		if( isMirrored && weapon.getAnimation() != null ) {
			if( frames == null ) { // shouldn't ever be true, unless someone has somehow added a weapon animation during combat.
				frames = new HashSet();
			}
			Integer frame = new Integer( weapon.getAnimation().getFrame() );
			if( !frames.contains( frame ) ) {
				DecoUtils.mirror( weapon, true );
				frames.add( frame );
			}
		}
	}
	
	@Override
	public void init( WeaponAPI weapon ) {
		if( isMirrored ) {
			DecoUtils.mirror( weapon, false );
			if( weapon.getAnimation() != null ) {
				frames = new HashSet();
				frames.add( new Integer( weapon.getAnimation().getFrame() ) );
			}
		}
	}
}
