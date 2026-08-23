package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;

import java.awt.*;
import java.util.List;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;

class tentacle_layeredRender extends BaseCombatLayeredRenderingPlugin {
    private tentacle_render parentPlugin;

    @Override
    public float getRenderRadius() {
        return 999999999999999999999f;
    }

    protected tentacle_layeredRender(tentacle_render parentPlugin) {
        this.parentPlugin = parentPlugin;
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI view) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }
        parentPlugin.render(layer, view);
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.allOf(CombatEngineLayers.class);
    }
}

public class tentacle_render extends BaseEveryFrameCombatPlugin {

    private static List<renderData> QUADRENDER = new ArrayList<>();
    private static Map<Integer, SpriteAPI> shieldTextures = new HashMap<>();
    private static SpriteAPI shieldFront;

    public void init(CombatEngineAPI engine) {
        engine.addLayeredRenderingPlugin( new tentacle_layeredRender(this));
        QUADRENDER.clear();
    }

    void render(CombatEngineLayers layer, ViewportAPI view){
        if(!QUADRENDER.isEmpty()){
            List<renderData> toRemove = new ArrayList<>();
            for(renderData data: QUADRENDER){
                if(layer==data.layer) {
                    renderQuadStripSingleframe(data);
                    toRemove.add(data);
                }
            }
            if(!Global.getCombatEngine().isPaused()) QUADRENDER.removeAll(toRemove);
        }
    }

    public static void renderQuadStrip(renderData data){
        QUADRENDER.add(data);
    }

    public static Vector4f add2f (Vector2f a, Vector2f b){
        return new Vector4f(a.x,a.y,b.x,b.y);
    }

    public static List<SegmentData> makeSegmentsBezierCurve(Vector4f start, Vector4f end, Vector2f width, float segmentLength){
        List<SegmentData> result = new ArrayList<>();

        float len = getBezierLength(start,end);
        int numSegments = (int)Math.ceil(len/segmentLength);
        float SL = len/numSegments;
        float LF = 0;
        Vector2f prev = new Vector2f(start.x, start.y);
        for(int i=0; i<=numSegments; i++){
            float angleForward = 0f;
            Vector2f point = getPointOnBezier(start,end, (LF)/len);
            float fract = i/(float)numSegments;
            if(i==0){
                angleForward=VectorUtils.getFacing(new Vector2f(start.z,start.w));
            }
            if(i==numSegments){
                angleForward=VectorUtils.getFacing(new Vector2f(end.z,end.w))+180f;
            }
            if(i!=0 && i!=numSegments){
                Vector2f np = getPointOnBezier(start,end, (i+1f)/numSegments);
                angleForward=VectorUtils.getAngle(prev,np);
            }
            float dist = MathUtils.getDistance(prev, point);

            float w = width.x+(width.y-width.x)*fract;
            Vector2f left = MathUtils.getPointOnCircumference(point,w/2, angleForward+90f);
            Vector2f right = MathUtils.getPointOnCircumference(point,w/2, angleForward-90f);
            result.add(new SegmentData(left,right,dist, new Vector2f()));
        }
        return result;
    }

    public static List<SegmentData> makeSegmentsBezierCurve(
            List<Vector4f> points,
            float segmentLength,
            Vector2f width,
            float offsetWaveformSize,
            List<Vector2f> offsetWaveformParams,
            float widthWaveformSize,
            List<Vector2f> widthWaveformParams
    ){
        List<SegmentData> result = new ArrayList<>();

        float totalLength = getTotalLength(points);
        float filledLength = segmentLength;
        float dist = 0f;
        Vector2f pr = new Vector2f();
        for(int i=1;i<= points.size()-1; i++){
            Vector4f prev =points.get(i-1);
            Vector4f cur = rotate4fButt(points.get(i));
            float len= getBezierLength(prev,cur);
            int NS = (int)Math.ceil(len/segmentLength);

            float LF = 0;

            while(LF<len){
                float angleForward = 0f;
                Vector2f point = getPointOnBezier(prev,cur, (LF)/len);

                Vector2f np = getPointOnBezier(prev,cur, (LF+segmentLength)/len);
                if(LF<=0){
                    angleForward=VectorUtils.getFacing(new Vector2f(prev.z,prev.w));
                } else {
                    Vector2f pp = getPointOnBezier(prev,cur, (LF-segmentLength)/len);
                    angleForward=VectorUtils.getAngle(pp,np);
                }
                float maxAngle = 45f;
                float difAngle = MathUtils.getShortestRotation(angleForward,VectorUtils.getAngle(point,np));
                float shadeAmount = Math.abs(difAngle/maxAngle);
                if(Math.abs(difAngle)>maxAngle){shadeAmount=1;}
                if(Math.abs(difAngle)<0f){shadeAmount=0;}

                int dir = (int)Math.signum(difAngle);
                Vector2f shade = new Vector2f(0f,shadeAmount);
                if(dir<0){shade=new Vector2f(shade.y,shade.x);}
                float totalFract = (filledLength+LF)/totalLength;
                if(totalFract>1f){totalFract=1f;}
                float w = width.x+(width.y-width.x)*totalFract;

                if(widthWaveformParams!=null){
                    float ww = widthWaveformSize*getWaveform(totalFract, widthWaveformParams);
                    w+=ww;
                }
                float o = 0f;
                if(offsetWaveformParams!=null){
                    o=offsetWaveformSize*getWaveform(totalFract, offsetWaveformParams);
                }
                Vector2f left = MathUtils.getPointOnCircumference(point,w/2+o, angleForward+90f);
                Vector2f right = MathUtils.getPointOnCircumference(point,w/2-o, angleForward-90f);

                LF+=segmentLength;
                dist=MathUtils.getDistance(pr, point);
                result.add(new SegmentData(left, right, dist, shade));

                pr=point;
            }
            filledLength+=len;
        }

        return result;
    }

    public static class SegmentData{
        public final Vector2f left;
        public final Vector2f right;
        public final float dist;
        public final Vector2f shadow;
        public SegmentData(
                Vector2f left,
                Vector2f right,
                float dist,
                Vector2f shadow
        ){
            this.left=left;
            this.right=right;
            this.dist=dist;
            this.shadow=shadow;
        }
    }

    public static float getWaveform(float arg, List<Vector2f> params){
        float result = 1f;

        for(Vector2f p: params){
            result = result * (float)FastTrig.sin((p.x*arg+p.y)*Math.PI);
        }
        return result;
    }

    public static float getTotalLength(List<Vector4f> points){
        float result= 0;
        for(int i=1; i<=points.size()-1; i++){
            Vector4f prev =points.get(i-1);
            Vector4f cur = rotate4fButt(points.get(i));
            float len= getBezierLength(prev,cur);
            result+=len;
        }
        return result;
    }

    public static Vector4f rotate4fButt(Vector4f v){
        return new Vector4f(v.x,v.y,-v.z,-v.w);
    }

    private static Vector2f getPointOnBezier(Vector4f start, Vector4f end, float atFraction) {
        Vector2f p1, p2;
        p1 = new Vector2f(start.x + start.z, start.y + start.w);
        p2 = new Vector2f(end.x + end.z, end.y + end.w);

        float antiFraction = 1-atFraction;
        return new Vector2f(
                antiFraction*antiFraction*antiFraction*start.x
                        + 3*atFraction*antiFraction*antiFraction*p1.x
                        + 3*atFraction*atFraction*antiFraction*p2.x
                        + atFraction*atFraction*atFraction*end.x,
                antiFraction*antiFraction*antiFraction*start.y
                        + 3*atFraction*antiFraction*antiFraction*p1.y
                        + 3*atFraction*atFraction*antiFraction*p2.y
                        + atFraction*atFraction*atFraction*end.y);
    }

    private static float getBezierLength(Vector4f start, Vector4f end){
        Vector2f p1 = new Vector2f(start.x + start.z, start.y + start.w);
        Vector2f p2 = new Vector2f(end.x + end.z, end.y + end.w);

        return (1/3f) * (float)Math.sqrt(Math.pow(end.x - start.x,2f) + Math.pow(end.y - start.y, 2f)) + (1/6f) * (float)Math.sqrt(Math.pow(end.x - 3*p2.x + 3*p1.x - start.x,2f) + Math.pow(end.y - 3*p2.y + 3*p1.y - start.y,2f));
    }

    public static class renderData {
        public final Vector2f origin;
        public final List<SegmentData> points;
        public final SpriteAPI texture;
        public final float width;
        public final Vector2f texShift;

        public final Color color;

        public final float in;
        public final float out;
        public final CombatEngineLayers layer;

        public renderData(
                Vector2f origin,
                List<SegmentData> points,
                SpriteAPI texture,
                float width,
                Vector2f texShift,
                Color color,
                float in,
                float out,
                CombatEngineLayers layer
        ) {
            this.origin = origin;
            this.points = points;
            this.width=width;
            this.texShift=texShift;
            this.texture=texture;
            this.color=color;
            this.in = in;
            this.out = out;
            this.layer=layer;
        }
    }

    // Completely re-written for safety and to avoid Vector churn via exception handling
    public static boolean screen(float distance, Vector2f point) {
        ViewportAPI v = getViewport();
        if (v == null) return false;

        Vector2f center = v.getCenter();
        float h1 = v.getVisibleHeight() / 2f + distance;
        float w1 = v.getVisibleWidth() / 2f + distance;

        float difX = Math.abs(point.x - center.x);
        float difY = Math.abs(point.y - center.y);

        return difX <= w1 && difY <= h1;
    }

    public static ViewportAPI getViewport(){
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine != null && engine.getViewport() != null) {
            return engine.getViewport();
        }
        if (Global.getSector() != null && Global.getSector().getViewport() != null) {
            return Global.getSector().getViewport();
        }
        return null;
    }

    private void renderQuadStripSingleframe(renderData data){
        if(!screen(500f, data.origin))return;

        float len = 0f;
        for(SegmentData seg:data.points){
            len+=seg.dist;
        }

        glPushAttrib(GL_ALL_ATTRIB_BITS);

        GL11.glPushMatrix();
        GL11.glTranslatef(data.origin.x, data.origin.y, 0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);

        int blendFunc1 = GL11.GL_SRC_ALPHA;
        int blendFunc2 = GL11.GL_ONE_MINUS_SRC_ALPHA;

        GL11.glBlendFunc(blendFunc1, blendFunc2);
        SpriteAPI texture = data.texture;
        Color color = data.color;

        texture.bindTexture();

        GL11.glBegin(GL11.GL_QUAD_STRIP);

        float texWidth = texture.getTextureWidth();
        float imageWidth = texture.getWidth();

        float texHeight = texture.getTextureHeight();
        float imageHeight = texture.getHeight();

        float leftTX = texWidth*((data.texShift.y+0f)/imageWidth);

        float texProgress = 0f;
        float FL = 0f;
        for(SegmentData seg:data.points){
            FL+= seg.dist;
            float alphaMult = 1f;

            if(data.in>0 && (FL/len)<=(data.in)){
                alphaMult = FL/(data.in*len);
            }

            float fadeout = (len - FL)/len;
            if(data.out>0 && fadeout<=data.out){
                alphaMult = fadeout/data.out;
            }

            float rightTX =  texWidth*((data.texShift.y+data.width)/imageWidth) - 0.001f;
            float texPerSegment = (seg.dist/texWidth)*(texHeight/imageHeight)*(imageWidth/data.width);
            texProgress += texPerSegment;

            GL11.glColor4ub(
                    (byte)(color.getRed()*(1f-seg.shadow.x)),
                    (byte)(color.getGreen()*(1f-seg.shadow.x)),
                    (byte)(color.getBlue()*(1f-seg.shadow.x)),
                    (byte)((float) color.getAlpha()*alphaMult)
            );

            float speedMult=1f;

            GL11.glTexCoord2f(leftTX, texProgress+speedMult*data.texShift.x);
            GL11.glVertex2f(seg.left.x, seg.left.y);GL11.glColor4ub(
                    (byte)(color.getRed()*(1f-seg.shadow.y)),
                    (byte)(color.getGreen()*(1f-seg.shadow.y)),
                    (byte)(color.getBlue()*(1f-seg.shadow.y)),
                    (byte)((float) color.getAlpha()*alphaMult)
            );
            GL11.glTexCoord2f(rightTX, texProgress+speedMult*data.texShift.x);
            GL11.glVertex2f(seg.right.x, seg.right.y);

        }

        GL11.glEnd();
        GL11.glPopMatrix();
        glPopAttrib();
    }
}