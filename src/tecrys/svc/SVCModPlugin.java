/*     */ package tecrys.svc;
/*     */ 
import tecrys.svc.world.SVCGen;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class SVCModPlugin
/*     */   extends BaseModPlugin
/*     */ {
/*     */   private static void initSVC()
/*     */   {
/*     */     try
/*     */     {
/*  35 */       Global.getSettings().getScriptClassLoader().loadClass("data.scripts.world.ExerelinGen");
/*     */ 
/*     */     }
/*     */     catch (ClassNotFoundException ex)
/*     */     {
/*     */ 
/*  41 */       new SVCGen().generate(Global.getSector());
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 

/*     */ }


/* Location:              C:\Games\Starsector08a\mods\BGE\jars\JARCreatorbefore.jar!\bge\data\scripts\BGEModPlugin.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */