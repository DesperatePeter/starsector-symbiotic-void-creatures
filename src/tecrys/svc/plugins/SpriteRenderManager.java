/*
 * By Tartiflette
 * Plugin managing direct sprite rendering to create new visual effect or add new UI elements with only one line of code.
 * Note that every sprite will be drawn one frame late.
 */
package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SpriteRenderManager extends BaseEveryFrameCombatPlugin {

    private static List<renderData> SINGLEFRAME = new ArrayList<>();
    private static List<battlespaceData> BATTLESPACE = new ArrayList<>();
    private static List<objectspaceData> OBJECTSPACE = new ArrayList<>();
    private static List<screenspaceData> SCREENSPACE = new ArrayList<>();

    // Cache a vector to prevent thousands of objects being instantiated every frame
    private static final Vector2f scratchVector = new Vector2f();

    @Override
    public void init(CombatEngineAPI engine) {
        SINGLEFRAME.clear();
        BATTLESPACE.clear();
        OBJECTSPACE.clear();
        SCREENSPACE.clear();
    }

    public static void singleFrameRender(SpriteAPI sprite, Vector2f loc, Vector2f size, float angle, Color color, boolean additive) {
        sprite.setSize(size.x, size.y);
        sprite.setAngle(angle);
        sprite.setColor(color);
        if(additive){
            sprite.setAdditiveBlend();
        }
        SINGLEFRAME.add(new renderData(sprite, loc));
    }

    public static void battlespaceRender(SpriteAPI sprite, Vector2f loc, Vector2f vel, Vector2f size, Vector2f growth, float angle, float spin, Color color, boolean additive, float fadein, float full, float fadeout) {
        sprite.setSize(size.x, size.y);
        sprite.setAngle(angle);
        sprite.setColor(color);
        if(additive){
            sprite.setAdditiveBlend();
        }
        BATTLESPACE.add(new battlespaceData(sprite, loc, vel, growth, spin, fadein, fadein+full, fadein+full+fadeout, 0));
    }

    public static void objectspaceRender(SpriteAPI sprite, CombatEntityAPI anchor, Vector2f offset, Vector2f vel, Vector2f size, Vector2f growth, float angle, float spin, boolean parent, Color color, boolean additive, float fadein, float full, float fadeout, boolean fadeOnDeath) {
        sprite.setSize(size.x, size.y);
        if(parent){
            sprite.setAngle(anchor.getFacing()+angle+90);
        } else {
            sprite.setAngle(angle+90);
        }
        sprite.setColor(color);
        if(additive){
            sprite.setAdditiveBlend();
        }
        Vector2f loc=new Vector2f(anchor.getLocation());

        OBJECTSPACE.add(new objectspaceData(sprite, anchor, loc, offset, vel, growth, angle, spin, parent, fadein, fadein+full, fadein+full+fadeout, fadeOnDeath, 0));
    }

    public static void screenspaceRender(SpriteAPI sprite, positioning pos, Vector2f loc, Vector2f vel, Vector2f size, Vector2f growth, float angle, float spin, Color color, boolean additive, float fadein, float full, float fadeout) {
        ViewportAPI screen = Global.getCombatEngine().getViewport();

        Vector2f ratio=size;
        Vector2f screenSize= new Vector2f(screen.getVisibleWidth(),screen.getVisibleHeight());
        if(pos == positioning.STRETCH_TO_FULLSCREEN){
            sprite.setSize(screenSize.x, screenSize.y);
        } else if(pos == positioning.FULLSCREEN_MAINTAIN_RATIO) {
            if(size.x/size.y > screenSize.x/screenSize.y){
                ratio = new Vector2f((size.x/size.y)/(screenSize.x/screenSize.y),1);
            } else {
                ratio = new Vector2f(1, (size.y/size.x)/(screenSize.y/screenSize.x));
                sprite.setSize(Global.getCombatEngine().getViewport().getVisibleWidth()*ratio.x,Global.getCombatEngine().getViewport().getVisibleHeight()*ratio.y);
            }
        } else {
            sprite.setSize(size.x*screen.getViewMult(), size.y*screen.getViewMult());
        }
        sprite.setAngle(angle);
        sprite.setColor(color);
        if(additive){
            sprite.setAdditiveBlend();
        }

        SCREENSPACE.add(new screenspaceData(sprite, pos, loc, vel, ratio, growth, spin, fadein, fadein+full, fadein+full+fadeout, 0));
    }

    @Override
    public void renderInWorldCoords(ViewportAPI view){
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null){return;}

        float amount=0;
        if(!engine.isPaused()){
            amount=engine.getElapsedInLastFrame();
        }

        if(!BATTLESPACE.isEmpty()){
            for(Iterator<battlespaceData> iter=BATTLESPACE.iterator(); iter.hasNext(); ){
                battlespaceData entry = iter.next();

                entry.TIME+=amount;
                if(entry.TIME>entry.FADEOUT){
                    iter.remove();
                    continue;
                }

                if(entry.GROWTH!= null && (entry.GROWTH.x != 0 || entry.GROWTH.y != 0)){
                    entry.SPRITE.setSize(entry.SPRITE.getWidth()+(entry.GROWTH.x*amount), entry.SPRITE.getHeight()+(entry.GROWTH.y*amount));
                    if(entry.SPRITE.getHeight()<=0 || entry.SPRITE.getWidth()<=0){
                        iter.remove();
                        continue;
                    }
                }

                // Optimized movement using scratchVector
                if(entry.VEL != null && (entry.VEL.x != 0 || entry.VEL.y != 0)){
                    scratchVector.set(entry.VEL);
                    scratchVector.scale(amount);
                    Vector2f.add(entry.LOC, scratchVector, entry.LOC);
                }

                if(entry.SPIN!=0){
                    entry.SPRITE.setAngle(entry.SPRITE.getAngle()+entry.SPIN*amount);
                }

                if(entry.TIME<entry.FADEIN){
                    entry.SPRITE.setAlphaMult(entry.TIME/entry.FADEIN);
                } else if(entry.TIME>entry.FULL){
                    entry.SPRITE.setAlphaMult(1-((entry.TIME-entry.FULL)/(entry.FADEOUT-entry.FULL)));
                } else {
                    entry.SPRITE.setAlphaMult(1);
                }

                render(new renderData(entry.SPRITE, entry.LOC));
            }
        }

        if(!OBJECTSPACE.isEmpty()){
            for(Iterator<objectspaceData> iter=OBJECTSPACE.iterator(); iter.hasNext(); ){
                objectspaceData entry = iter.next();

                if(!entry.DEATHFADE && !engine.isEntityInPlay(entry.ANCHOR)){
                    iter.remove();
                    continue;
                }

                if(entry.ANCHOR instanceof DamagingProjectileAPI){
                    if (entry.TIME<entry.FULL && (
                            ((DamagingProjectileAPI)entry.ANCHOR).isFading()
                                    || !engine.isEntityInPlay(entry.ANCHOR))
                    ){
                        entry.FADEOUT=(entry.FADEOUT-entry.FULL)+entry.TIME;
                    }
                }

                entry.TIME+=amount;
                if(entry.TIME>entry.FADEOUT){
                    iter.remove();
                    continue;
                }

                if(entry.GROWTH!= null && (entry.GROWTH.x != 0 || entry.GROWTH.y != 0)){
                    entry.SPRITE.setSize(entry.SPRITE.getWidth()+(entry.GROWTH.x*amount), entry.SPRITE.getHeight()+(entry.GROWTH.y*amount));
                    if(entry.SPRITE.getHeight()<=0 || entry.SPRITE.getWidth()<=0){
                        iter.remove();
                        continue;
                    }
                }

                // Optimized movement using scratchVector
                if(entry.VEL != null && (entry.VEL.x != 0 || entry.VEL.y != 0)){
                    scratchVector.set(entry.VEL);
                    scratchVector.scale(amount);
                    Vector2f.add(entry.OFFSET, scratchVector, entry.OFFSET);
                }

                Vector2f location = new Vector2f(entry.OFFSET);

                if(entry.PARENT && engine.isEntityInPlay(entry.ANCHOR)){
                    if(entry.SPIN!=0){
                        entry.ANGLE+=entry.SPIN*amount;
                    }
                    entry.SPRITE.setAngle(entry.ANCHOR.getFacing()+90+entry.ANGLE);
                    VectorUtils.rotate(location, entry.ANCHOR.getFacing(), location);
                } else {
                    if(entry.SPIN!=0){
                        entry.SPRITE.setAngle(entry.SPRITE.getAngle()+entry.SPIN*amount);
                    }
                }

                if(engine.isEntityInPlay(entry.ANCHOR)){
                    Vector2f.add(location, entry.ANCHOR.getLocation(), location);
                    entry.LOCATION=entry.ANCHOR.getLocation();
                } else {
                    Vector2f.add(location, entry.LOCATION, location);
                }

                if(entry.TIME<entry.FADEIN){
                    entry.SPRITE.setAlphaMult(entry.TIME/entry.FADEIN);
                } else if(entry.TIME>entry.FULL){
                    entry.SPRITE.setAlphaMult(1-((entry.TIME-entry.FULL)/(entry.FADEOUT-entry.FULL)));
                } else {
                    entry.SPRITE.setAlphaMult(1);
                }

                render(new renderData(entry.SPRITE, location));
            }
        }

        if(!SCREENSPACE.isEmpty()){
            Vector2f center;
            ViewportAPI screen = Global.getCombatEngine().getViewport();

            for(Iterator<screenspaceData> iter=SCREENSPACE.iterator(); iter.hasNext(); ){
                screenspaceData entry = iter.next();

                if(entry.FADEOUT<0){
                    if(entry.POS == positioning.FULLSCREEN_MAINTAIN_RATIO){
                        center = new Vector2f(screen.getCenter());
                        entry.SPRITE.setSize(entry.SIZE.x*screen.getVisibleWidth(), entry.SIZE.y*screen.getVisibleHeight());
                    } else if(entry.POS == positioning.STRETCH_TO_FULLSCREEN){
                        center = new Vector2f(screen.getCenter());
                        entry.SPRITE.setSize(screen.getVisibleWidth(), screen.getVisibleHeight());
                    } else {
                        Vector2f refPoint=screen.getCenter();
                        switch (entry.POS){
                            case LOW_LEFT:
                                refPoint = new Vector2f(refPoint.x-(screen.getVisibleWidth()/2), refPoint.y-(screen.getVisibleHeight()/2));
                                break;
                            case LOW_RIGHT:
                                refPoint = new Vector2f(refPoint.x-(screen.getVisibleWidth()/2), refPoint.y+(screen.getVisibleHeight()/2));
                                break;
                            case UP_LEFT:
                                refPoint = new Vector2f(refPoint.x+(screen.getVisibleWidth()/2), refPoint.y-(screen.getVisibleHeight()/2));
                                break;
                            case UP_RIGHT:
                                refPoint = new Vector2f(refPoint.x+(screen.getVisibleWidth()/2), refPoint.y+(screen.getVisibleHeight()/2));
                                break;
                            default:
                        }
                        center = new Vector2f(entry.LOC);
                        center.scale(screen.getViewMult());
                        Vector2f.add(center, refPoint, center);
                    }

                    render(new renderData(entry.SPRITE, center));
                    iter.remove();
                } else {
                    entry.TIME+=amount;
                    if(entry.FADEOUT>0 && entry.TIME>entry.FADEOUT){
                        iter.remove();
                        continue;
                    }

                    if(entry.POS == positioning.FULLSCREEN_MAINTAIN_RATIO){
                        center = new Vector2f(screen.getCenter());
                        entry.SPRITE.setSize(entry.SIZE.x*screen.getVisibleWidth(), entry.SIZE.y*screen.getVisibleHeight());
                    } else if(entry.POS == positioning.STRETCH_TO_FULLSCREEN){
                        center = new Vector2f(screen.getCenter());
                        entry.SPRITE.setSize(screen.getVisibleWidth(), screen.getVisibleHeight());
                    } else {
                        Vector2f refPoint=screen.getCenter();
                        switch (entry.POS){
                            case LOW_LEFT:
                                refPoint = new Vector2f(refPoint.x-(screen.getVisibleWidth()/2), refPoint.y-(screen.getVisibleHeight()/2));
                                break;
                            case LOW_RIGHT:
                                refPoint = new Vector2f(refPoint.x-(screen.getVisibleWidth()/2), refPoint.y+(screen.getVisibleHeight()/2));
                                break;
                            case UP_LEFT:
                                refPoint = new Vector2f(refPoint.x+(screen.getVisibleWidth()/2), refPoint.y-(screen.getVisibleHeight()/2));
                                break;
                            case UP_RIGHT:
                                refPoint = new Vector2f(refPoint.x+(screen.getVisibleWidth()/2), refPoint.y+(screen.getVisibleHeight()/2));
                                break;
                            default:
                        }

                        // Optimized movement using scratchVector
                        if(entry.VEL != null && (entry.VEL.x != 0 || entry.VEL.y != 0)){
                            scratchVector.set(entry.VEL);
                            scratchVector.scale(amount);
                            Vector2f.add(entry.LOC, scratchVector, entry.LOC);
                        }

                        center = new Vector2f(entry.LOC);
                        center.scale(screen.getViewMult());
                        Vector2f.add(center, refPoint, center);

                        if(entry.GROWTH!= null && (entry.GROWTH.x != 0 || entry.GROWTH.y != 0)){
                            entry.SIZE = new Vector2f(entry.SIZE.x+(entry.GROWTH.x*amount), entry.SIZE.y+(entry.GROWTH.y*amount));
                            if(entry.SIZE.x<=0 || entry.SIZE.y<=0){
                                iter.remove();
                                continue;
                            }
                        }
                        entry.SPRITE.setSize(entry.SIZE.x*screen.getViewMult(), entry.SIZE.y*screen.getViewMult());

                        if(entry.SPIN!=0){
                            entry.SPRITE.setAngle(entry.SPRITE.getAngle()+entry.SPIN*amount);
                        }
                    }

                    if(entry.TIME<entry.FADEIN){
                        entry.SPRITE.setAlphaMult(entry.TIME/entry.FADEIN);
                    } else if(entry.TIME>entry.FULL){
                        entry.SPRITE.setAlphaMult(1-((entry.TIME-entry.FULL)/(entry.FADEOUT-entry.FULL)));
                    } else {
                        entry.SPRITE.setAlphaMult(1);
                    }

                    render(new renderData(entry.SPRITE, center));
                    if(entry.FADEOUT<0){
                        iter.remove();
                    }
                }
            }
        }

        if(!SINGLEFRAME.isEmpty()){
            for(renderData d : SINGLEFRAME){
                render(d);
            }
            SINGLEFRAME.clear();
        }
    }

    private void render (renderData data){
        SpriteAPI sprite = data.SPRITE;
        sprite.renderAtCenter(data.LOC.x, data.LOC.y);
    }

    private static class renderData {
        private final SpriteAPI SPRITE;
        private final Vector2f LOC;

        public renderData(SpriteAPI sprite, Vector2f loc) {
            this.SPRITE = sprite;
            this.LOC = loc;
        }
    }

    private static class battlespaceData {
        private final SpriteAPI SPRITE;
        private Vector2f LOC;
        private final Vector2f VEL;
        private final Vector2f GROWTH;
        private final float SPIN;
        private final float FADEIN;
        private final float FULL;
        private final float FADEOUT;
        private float TIME;

        public battlespaceData(SpriteAPI sprite, Vector2f loc, Vector2f vel, Vector2f growth, float spin, float fadein, float full, float fadeout, float time) {
            this.SPRITE = sprite;
            this.LOC = loc;
            this.VEL = vel;
            this.GROWTH = growth;
            this.SPIN = spin;
            this.FADEIN = fadein;
            this.FULL = full;
            this.FADEOUT = fadeout;
            this.TIME = time;
        }
    }

    private static class objectspaceData {
        private final SpriteAPI SPRITE;
        private final CombatEntityAPI ANCHOR;
        private Vector2f LOCATION;
        private Vector2f OFFSET;
        private final Vector2f VEL;
        private final Vector2f GROWTH;
        private float ANGLE;
        private final float SPIN;
        private final boolean PARENT;
        private final float FADEIN;
        private float FULL;
        private float FADEOUT;
        private final boolean DEATHFADE;
        private float TIME;

        public objectspaceData(SpriteAPI sprite, CombatEntityAPI anchor, Vector2f loc, Vector2f offset, Vector2f vel, Vector2f growth, float angle, float spin, boolean parent, float fadein, float full, float fadeout, boolean fade, float time) {
            this.SPRITE = sprite;
            this.ANCHOR = anchor;
            this.LOCATION = loc;
            this.OFFSET = offset;
            this.ANGLE = angle;
            this.VEL = vel;
            this.GROWTH = growth;
            this.SPIN = spin;
            this.PARENT = parent;
            this.FADEIN = fadein;
            this.FULL = full;
            this.FADEOUT = fadeout;
            this.DEATHFADE = fade;
            this.TIME = time;
        }
    }

    public static enum positioning{
        CENTER,
        LOW_LEFT,
        LOW_RIGHT,
        UP_LEFT,
        UP_RIGHT,
        STRETCH_TO_FULLSCREEN,
        FULLSCREEN_MAINTAIN_RATIO,
    }

    private static class screenspaceData {
        private final SpriteAPI SPRITE;
        private final positioning POS;
        private Vector2f LOC;
        private final Vector2f VEL;
        private Vector2f SIZE;
        private final Vector2f GROWTH;
        private final float SPIN;
        private final float FADEIN;
        private final float FULL;
        private final float FADEOUT;
        private float TIME;

        public screenspaceData(SpriteAPI sprite, positioning position, Vector2f loc, Vector2f vel, Vector2f size, Vector2f growth, float spin, float fadein, float full, float fadeout, float time) {
            this.SPRITE = sprite;
            this.POS = position;
            this.LOC = loc;
            this.VEL = vel;
            this.SIZE = size;
            this.GROWTH = growth;
            this.SPIN = spin;
            this.FADEIN = fadein;
            this.FULL = full;
            this.FADEOUT = fadeout;
            this.TIME = time;
        }
    }
}