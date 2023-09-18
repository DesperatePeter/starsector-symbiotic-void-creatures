package tecrys.svc.utils;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.util.List;
//import org.lazywizard.lazylib.FastTrig;
//import org.lazywizard.lazylib.MathUtils;
//import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class DecoUtils {
	// Original Code by Wyvern
	public static boolean isOnLeft( WeaponAPI weapon ) {
		// slot API are in sideways coordinates, the same as ship creation specs;
		// +X is to the front of the ship, and +Y is to the left.
		if( weapon != null ) {
			WeaponSlotAPI slot = weapon.getSlot();
			if( slot != null ) {
				return slot.getLocation().getY() > 0f;
			}
		}
		return false;
	}
	
	public static boolean isFacingForward( WeaponAPI weapon ) {
		if( weapon != null ) {
			WeaponSlotAPI slot = weapon.getSlot();
			if( slot != null ) {
				float theta = slot.getAngle();
				if( theta > 90.1 || theta < -90.1 ) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static void mirror( WeaponAPI weapon, boolean justSprites ) {
		if( weapon == null ) return;
		Global.getLogger( DecoUtils.class ).debug( "DecoUtils mirroring weapon " + weapon.getDisplayName() );
		
		weapon.ensureClonedSpec();

		mirror( weapon.getBarrelSpriteAPI() );
		mirror( weapon.getSprite() );
		mirror( weapon.getUnderSpriteAPI() );
		mirror( weapon.getGlowSpriteAPI() );
		
		if( justSprites ) return;

		List<Vector2f> fireOffsets = weapon.getSpec().getHardpointFireOffsets();
		List<Float> angleOffsets = weapon.getSpec().getHardpointAngleOffsets();
		mirrorOffsets( fireOffsets, angleOffsets );
		
		fireOffsets = weapon.getSpec().getTurretFireOffsets();
		angleOffsets = weapon.getSpec().getTurretAngleOffsets();
		mirrorOffsets( fireOffsets, angleOffsets );

		fireOffsets = weapon.getSpec().getHiddenFireOffsets();
		angleOffsets = weapon.getSpec().getHiddenAngleOffsets();
		mirrorOffsets( fireOffsets, angleOffsets );
	}
	
	public static void mirror( SpriteAPI s ) {
		if( s == null ) return;
		s.setWidth( -s.getWidth() );
		s.setCenter( -s.getCenterX(), s.getCenterY() );
	}
	
	public static void mirrorOffsets( List<Vector2f> fireOffsets, List<Float> angleOffsets ) {
		for( int i = 0; i < fireOffsets.size(); i++ ) {
			// ensureClonedSpec does -not- clone the individual Vector2f objects, so we have to make new ones.
			// Fortunately, it does clone the lists, so we can actually make changes here.
			Vector2f v = (Vector2f)fireOffsets.get( i );
			fireOffsets.set( i, new Vector2f( v.getX(), -1f * v.getY() ) );
		}
		for( int i = 0; i < angleOffsets.size(); i++ ) {
			angleOffsets.set( i, -1f * (Float)angleOffsets.get( i ) );
		}
	}
}
