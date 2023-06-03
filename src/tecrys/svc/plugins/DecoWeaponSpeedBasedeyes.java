    package tecrys.svc.plugins;
      
    import com.fs.starfarer.api.AnimationAPI;  
    import com.fs.starfarer.api.combat.CombatEngineAPI;  
    import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;  
    import com.fs.starfarer.api.combat.ShipAPI;  
    import com.fs.starfarer.api.combat.WeaponAPI;  
      
    public class DecoWeaponSpeedBasedeyes implements EveryFrameWeaponEffectPlugin  
    {  
        // Frames per second at 0 speed  
        private static final float MIN_FPS = 0.5f;  
        // Frames per second at max speed (without stat boosts)  
        private static final float MAX_FPS = 2f;  
      
        // Local variables, you don't need to mess with these  
        private float curFPS = 15f;  
        private int curFrame = 0;  
        private float timeSinceLastFrame = 0f;  
        private float timeSinceLastUpdate = 0f;  
      
        // This is only recalculated once per second  
        private static float getCurrentFramesPerSecond(ShipAPI ship)  
        {  
            // Even with speed boosts, don't go above 100% animation speed  
            float maxSpeed = ship.getMutableStats().getMaxSpeed().getBaseValue();  
            return Math.max(MIN_FPS, Math.min(MAX_FPS,  
                    (MIN_FPS + (ship.getVelocity().length())  
                    * (MAX_FPS - MIN_FPS) / maxSpeed)));  
        }  
      
        @Override  
        public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon)  
        {  
            if (engine.isPaused())  
            {  
                return;  
            }  
      
            AnimationAPI anim = weapon.getAnimation();  
            ShipAPI ship = weapon.getShip();  
            /*            if (ship.isHulk())
            {
            anim.setFrame(0);
            return;
            }  */
      
            // Minor optimization: only update FPS once every tenth a second  
            timeSinceLastUpdate += amount;  
            if (timeSinceLastUpdate >= .1f)  
            {  
                timeSinceLastUpdate = 0f;  
                curFPS = getCurrentFramesPerSecond(ship);  
            }  
      
            // Animation framerate is based on current speed  
            float timeBetweenFrames = 1f / curFPS;  
            timeSinceLastFrame += amount;  
            while (timeSinceLastFrame >= timeBetweenFrames)  
            {  
                timeSinceLastFrame -= timeBetweenFrames;  
                if (++curFrame >= anim.getNumFrames())  
                {  
                    curFrame = 0;  
                }  
            }  
      
            anim.setFrame(curFrame);  
        }  
    }  