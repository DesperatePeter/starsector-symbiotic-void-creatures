package tecrys.svc.sfx;
import com.fs.starfarer.api.combat.CombatEngineAPI;  
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;  
import com.fs.starfarer.api.combat.WeaponAPI;  
import com.fs.starfarer.api.graphics.SpriteAPI;  
  
public class weaponSide implements EveryFrameWeaponEffectPlugin {  
    private boolean runOnce = false;  
      
    @Override  
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {  
          
        if (engine.isPaused()) return;  
        if(runOnce == false){  
            if (weapon.getShip().getOwner() == 0 && weapon.getLocation().getX() < weapon.getShip().getLocation().getX()   
                || weapon.getShip().getOwner() == 1 && weapon.getLocation().getX() > weapon.getShip().getLocation().getX()){  
                SpriteAPI theSprite = weapon.getSprite();  
                theSprite.setWidth(-theSprite.getWidth());  
                theSprite.setCenter(-theSprite.getCenterX(),theSprite.getCenterY());  
            }  
            runOnce = true;  
        }  
    }  
} 