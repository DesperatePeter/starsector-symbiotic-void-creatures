/*    */ package tecrys.svc.plugins;
/*    */ 
/*    */ import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
/*    */ import com.fs.starfarer.api.combat.CombatEngineAPI;
/*    */ import com.fs.starfarer.api.combat.ShipAPI;
/*    */ import com.fs.starfarer.api.combat.WeaponAPI;
/*    */ import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
/*    */ import com.fs.starfarer.api.input.InputEventAPI;
/*    */ import com.fs.starfarer.api.util.IntervalUtil;
/*    */ import java.awt.Color;
/*    */ import java.util.Collection;
/*    */ import java.util.HashMap;
/*    */ import java.util.Iterator;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import org.lwjgl.util.vector.Vector2f;
/*    */ 
/*    */ public class BloodEffect extends BaseEveryFrameCombatPlugin
/*    */ {
/*    */   private CombatEngineAPI engine;
/* 21 */   private static Map mag = new HashMap();
/*    */   
/*    */   static {
/* 24 */     mag.put(WeaponAPI.WeaponSize.SMALL, Float.valueOf(1.0F));
/* 25 */     mag.put(WeaponAPI.WeaponSize.MEDIUM, Float.valueOf(1.5F));
/* 26 */     mag.put(WeaponAPI.WeaponSize.LARGE, Float.valueOf(2.5F));
/*    */   }
/*    */   
/* 29 */   private float smokeSize = 0.2F + 0.1F * (float)Math.random();
/*    */   
/*    */   public void init(CombatEngineAPI engine)
/*    */   {
/* 33 */     this.engine = engine;
/*    */   }
/*    */   
/* 36 */   private IntervalUtil interval = new IntervalUtil(0.1F, 0.3F);
/*    */   
/*    */   public void advance(float amount, List<InputEventAPI> events)
/*    */   {
                   if (engine == null) {
/*  30 */       return;
/*     */     }
/* 40 */     if (this.engine.isPaused()) { return;
/*    */     }
/* 42 */     this.interval.advance(amount);
/*    */     
/* 44 */     if (this.interval.intervalElapsed())
/*    */     {
/* 46 */       this.smokeSize = (0.4F + 0.1F * (float)Math.random());
/* 47 */       List ships = this.engine.getShips();
/* 48 */       Iterator it = ships.iterator();
/* 49 */       while (it.hasNext())
/*    */       {
/* 51 */         ShipAPI ship = (ShipAPI)it.next();
/* 52 */         List weapons = ship.getAllWeapons();
/* 53 */         Iterator it2 = weapons.iterator();
/*    */         
/* 55 */         while (it2.hasNext())
/*    */         {
/* 57 */           WeaponAPI weapon = (WeaponAPI)it2.next();
/* 58 */           if (!ship.getVariant().getHullMods().contains("BGECarapace")) return;
/* 59 */           if ((weapon.isDisabled()) || (ship.isHulk()))
/*    */           {
/* 61 */             float smokeSizeValue = ((Float)mag.get(weapon.getSize())).floatValue();
/*    */             
/* 63 */             float velX = (float)Math.random() * 10.0F - 5.0F;
/* 64 */             float velY = (float)Math.sqrt(25.0F - velX * velX);
/* 65 */             if ((float)Math.random() >= 0.5F)
/*    */             {
/* 67 */               velY = -velY;
/*    */             }
/*    */             
/* 70 */             this.engine.addSmokeParticle(weapon.getLocation(), new Vector2f(velX, velY), 40.0F * this.smokeSize * smokeSizeValue, 0.05F, 4.0F, new Color(200, 0, 0, 200));
/* 71 */             this.engine.addSmokeParticle(weapon.getLocation(), new Vector2f(velX, velY), 20.0F * this.smokeSize * smokeSizeValue, 0.05F, 3.0F, new Color(200, 0, 0, 200));
/*    */           }
/*    */         }
/*    */       }
/*    */     }
/*    */   }
/*    */   
/*    */ 
/*    */   public boolean isDone()
/*    */   {
/* 81 */     return false;
/*    */   }
/*    */   
/*    */ 
/*    */   public boolean runWhilePaused()
/*    */   {
/* 87 */     return false;
/*    */   }
/*    */ }


/* Location:              C:\Games\Starsector08a\mods\BGE\jars\BGEMeleeAI.jar!\data\scripts\plugins\BloodEffect.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */