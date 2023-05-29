/*     */ package tecrys.svc.weapons;
/*     */ 
/*     */ import com.fs.starfarer.api.AnimationAPI;
/*     */ import com.fs.starfarer.api.combat.ShipAPI;
/*     */ import com.fs.starfarer.api.combat.WeaponAPI;
/*     */ import com.fs.starfarer.api.graphics.SpriteAPI;
/*     */ import java.util.Map;
/*     */ import org.lwjgl.util.vector.Vector2f;
/*     */ 
/*     */ public class BaseAnimateOnFireEffect2 implements com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
/*     */ {
/*     */   private float timeSinceLastFrame;
/*  13 */   private float timeBetweenFrames = 0.007142857F;
/*  14 */   private Map pauseFrames = new java.util.HashMap();
/*  15 */   private int curFrame = 0; private int pausedFor = 0;
/*  16 */   private boolean isFiring = true;
/*  17 */   private boolean runOnce = false;
/*  18 */   private boolean runOnce2 = false;
/*  19 */   private boolean runOnce3 = false;
/*     */   
/*     */   protected void setFramesPerSecond(float fps)
/*     */   {
/*  23 */     this.timeBetweenFrames = (1.0F / fps);
/*     */   }
/*     */   
/*     */   protected void pauseOnFrame(int frame, int pauseFor)
/*     */   {
/*  28 */     this.pauseFrames.put(Integer.valueOf(frame), Integer.valueOf(pauseFor));
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */   private void incFrame(AnimationAPI anim)
/*     */   {
/*  35 */     if (this.pauseFrames.containsKey(Integer.valueOf(this.curFrame)))
/*     */     {
/*  37 */       if (this.pausedFor < ((Integer)this.pauseFrames.get(Integer.valueOf(this.curFrame))).intValue())
/*     */       {
/*  39 */         this.pausedFor += 1;
/*  40 */         return;
/*     */       }
/*     */       
/*     */ 
/*  44 */       this.pausedFor = 0;
/*     */     }
/*     */     
/*     */ 
/*  48 */     this.curFrame = Math.min(this.curFrame + 1, anim.getNumFrames() - 1);
/*     */   }
/*     */   
/*     */ 
/*     */   public void advance(float amount, com.fs.starfarer.api.combat.CombatEngineAPI engine, WeaponAPI weapon)
/*     */   {
/*  54 */     if (engine.isPaused())
/*     */     {
/*  56 */       return;
/*     */     }
/*  58 */     if (!this.runOnce) {
/*  59 */       if ((weapon.getShip().getOriginalOwner() == -1) && (weapon.getLocation().getY() > weapon.getShip().getLocation().getY())) {
/*  60 */         SpriteAPI theSprite = weapon.getSprite();
/*  61 */         theSprite.setWidth(-theSprite.getWidth());
/*  62 */         theSprite.setCenter(-theSprite.getCenterX(), theSprite.getCenterY());
/*     */       }
/*  64 */       this.runOnce = true;
/*     */     }
/*  66 */     AnimationAPI anim = weapon.getAnimation();
/*  67 */     anim.setFrame(this.curFrame);
/*     */   
/*  69 */     if (this.isFiring)
/*     */     {
/*  71 */       this.timeSinceLastFrame += amount;
/*     */       
/*  73 */       if (this.timeSinceLastFrame >= this.timeBetweenFrames)
/*     */       {
/*  75 */         this.timeSinceLastFrame = 0.0F;
/*     */         
/*  77 */         anim.setFrame(this.curFrame + 1);
/*  78 */         if ((!this.runOnce2) && (((weapon.getShip().getOwner() == 0) && (weapon.getLocation().getX() < weapon.getShip().getLocation().getX())) || ((weapon.getShip().getOwner() == 1) && (weapon.getLocation().getX() > weapon.getShip().getLocation().getX()))))
/*     */         {
/*     */ 
/*  81 */           SpriteAPI theSprite = weapon.getSprite();
/*  82 */           theSprite.setWidth(-theSprite.getWidth());
/*  83 */           theSprite.setCenter(-theSprite.getCenterX(), theSprite.getCenterY());
/*     */         }
/*     */ 
/*     */ 
/*  87 */         incFrame(anim);
/*     */         
/*  89 */         if (this.curFrame == anim.getNumFrames() - 1)
/*     */         {
/*  91 */           this.isFiring = false;
/*  92 */           this.runOnce2 = true;
/*     */         }
/*     */         
/*     */       }
/*     */       
/*     */     }
/*  98 */     else if ((weapon.isFiring()) && (weapon.getChargeLevel() == 1.0F))
/*     */     { 
/* 100 */       this.isFiring = true;
/* 101 */       incFrame(anim);
/* 102 */       anim.setFrame(this.curFrame);
/*     */     }
/*     */     else
/*     */     {
/* 106 */       this.curFrame = 0;
/* 107 */       anim.setFrame(this.curFrame);
/* 108 */       if (!this.runOnce3) {
/* 109 */         if (((weapon.getShip().getOwner() == 0) && (weapon.getLocation().getX() < weapon.getShip().getLocation().getX())) || ((weapon.getShip().getOwner() == 1) && (weapon.getLocation().getX() > weapon.getShip().getLocation().getX())))
/*     */         {
/*     */ 
/* 112 */           SpriteAPI theSprite = weapon.getSprite();
/* 113 */           theSprite.setWidth(-theSprite.getWidth());
/* 114 */           theSprite.setCenter(-theSprite.getCenterX(), theSprite.getCenterY());
/*     */         }
/* 116 */         this.runOnce3 = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ }


/* Location:              C:\Games\Starsector08a\mods\BGE\jars\BGEMeleeAI.jar!\data\scripts\weapons\BaseAnimateOnFireEffect2.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */