/*
Code by Xaiier
 */

package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicTrailPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class svcBaseKinematicRopePlugin extends BaseEveryFrameCombatPlugin {


    public boolean is_pd;

    public static class RopeParams {
        float damping = 0.85f; //can tweak this to affect how aggressively they move into optimal position
        float scale = 1f; //according to the algorithm this needs to be tiny like 0.001 but that makes it comically slow, does not seem to oscillate much even with high values
        int neighbors = 3; //higher numbers increase stiffness by adding additional "springs" between neighboring elements, interacts with damping & scale values to result in a maximum resting curvature
        float segmentGoalLength; //in SU
    }

    public class SegmentPoint {
        public Vector2f getLocation() {
            return location;
        }

        public Vector2f location = new Vector2f();

        public Vector2f getVelocity() {
            return velocity;
        }

        public Vector2f velocity = new Vector2f();
        public Vector2f accel = new Vector2f();

        public void advance(float amount) {
            Vector2f vel = new Vector2f(velocity);
            vel.scale(amount);
            location = Vector2f.add(location, vel, null);
        }
    }

    protected int pluginID;

    public ArrayList<SegmentPoint> getSegmentPoints() {
        return segmentPoints;
    }

    protected ArrayList<SegmentPoint> segmentPoints = new ArrayList<>();
    protected ArrayList<Vector2f> renderPoints = new ArrayList<>();
    protected int elementCount;
    protected RopeParams ropeParams;
    protected SpriteAPI sprite;
    protected float trailWidth;
    protected Color[] trailColor = new Color[]{Color.white};

    public boolean debug = false;
    protected boolean initialSetupDone = false;



    @Override
    public void processInputPreCoreControls(float amount, List<InputEventAPI> events) {
        for (InputEventAPI event : events) {
            if (event.isKeyDownEvent()) {
                if (event.isConsumed()) continue;
                if (event.getEventValue() == Keyboard.KEY_F9) {
                    debug = !debug;
                }
            }
        }
    }

    public svcBaseKinematicRopePlugin(int pluginID, int elementCount, float trailWidth, boolean is_pd) {
        this.pluginID = pluginID;
        this.elementCount = elementCount;
        this.trailWidth = trailWidth;

        ropeParams = new RopeParams();

        sprite = Global.getSettings().getSprite("fx", "svc_tentacle_trail");

        //Global.getLogger(this.getClass()).info("constructor " + pluginID);
    }

    //runs on first advance
    protected void initialSetup() {
        //NOTE: subclasses responsible for the creation & placement of segments
        //this is because initial placement depends on application
        initialSetupDone = true;
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        //Global.getLogger(this.getClass()).info("advance " + pluginID);
        if (!initialSetupDone) initialSetup();

        if (Global.getCombatEngine().isPaused()) return;

        computeStringPhysics(amount);
        //apply computed velocity from previous frame
        //NOTE: you can override the computed motion to attach points in place by doing so in a subclass advance,
        // you generally want to set the velocity to the same as whatever you're attaching it to
        for (SegmentPoint segmentPoint : segmentPoints) {
            segmentPoint.advance(amount);
        }

        drawTrail(); //draw a single-frame trail
    }

    protected void drawTrail() {
        //curve smoothing method
        //TODO: look at Chaikin's algorithm as alternate
        //first double the number of points by getting the midpoint of each segment
        ArrayList<Vector2f> extraPoints = new ArrayList<>();
        for (int i = 0; i < segmentPoints.size() - 1; i++) {
            extraPoints.add(segmentPoints.get(i).getLocation());
            Vector2f v = Misc.interpolateVector(segmentPoints.get(i).getLocation(), segmentPoints.get(i + 1).getLocation(), 0.5f);
            extraPoints.add(v);
        }
        extraPoints.add(segmentPoints.get(segmentPoints.size() - 1).getLocation());

        //next interpolate each point halfway between its original location and the midpoint of its neighbors
        renderPoints = new ArrayList<>();
        renderPoints.add(extraPoints.get(0));
        for (int i = 1; i < extraPoints.size() - 1; i++) {
            Vector2f v = Misc.interpolateVector(extraPoints.get(i - 1), extraPoints.get(i + 1), 0.5f);
            v = Misc.interpolateVector(extraPoints.get(i), v, 0.5f);
            renderPoints.add(v);
        }
        renderPoints.add(extraPoints.get(extraPoints.size() - 1));

        //now draw
        float id = org.magiclib.plugins.MagicTrailPlugin.getUniqueID();
        for (int i = 0; i < renderPoints.size(); i++) {

            ArrayList<Double> angles = new ArrayList<>();

            if (i - 1 >= 0) { //if previous exists
                Vector2f point1 = new Vector2f(renderPoints.get(i - 1));
                Vector2f point2 = new Vector2f(renderPoints.get(i));
                Vector2f delta = Vector2f.sub(point2, point1, null);
                float angle = VectorUtils.getFacing(delta);
                angles.add((double) angle);
            }
            if (i + 1 < renderPoints.size()) { //if next exists
                Vector2f point1 = new Vector2f(renderPoints.get(i));
                Vector2f point2 = new Vector2f(renderPoints.get(i + 1));
                Vector2f delta = Vector2f.sub(point2, point1, null);
                float angle = VectorUtils.getFacing(delta);
                angles.add((double) angle);
            }

            //TODO: replace with simple toArray?
            //switch to unboxed doubles because I borrowed this method from elsewhere
            double[] d = new double[angles.size()];
            for (int j = 0; j < angles.size(); j++) {
                d[j] = angles.get(j);
            }
            float meanAngle = (float) getMeanAngle(d);

//            if (debug) {
//                if (i % 2 == 0)
//                    Global.getCombatEngine().addFloatingTextAlways(segmentPoints.get(i / 2).getLocation(), String.valueOf(i / 2), 10f, Color.WHITE, null, 1f, 1f, 0.01f, 0f, 0f, 1f);
//                //Global.getCombatEngine().addSmoothParticle(renderPoints.get(i), new Vector2f(), 10f, 10f, 0.01f, Color.MAGENTA);
//
//                //debug render angle markers
//                Vector2f v = new Vector2f(10f, 0f);
//                v = VectorUtils.rotate(v, meanAngle);
//                v = Vector2f.add(v, renderPoints.get(i), null);
//                //Global.getCombatEngine().addSmoothParticle(v, new Vector2f(), 10f, 10f, 0.01f, Color.GREEN);
//            }

            Color color = computeColorForSegment(i);
     if (is_pd)       {
            MagicTrailPlugin.addTrailMemberAdvanced(
                    null,
                    id,
                    sprite,
                    renderPoints.get(i),
                    0f,
                    0f,
                    meanAngle,
                    0f,
                    0f,
                    trailWidth,
                    0,
                    color,
                    color,
                    (float) color.getAlpha() / 255f,
                    0f,
                    0f,
                    0f,
                    false,
                    25f,
                    0f,
                    0f,
                    null,
                    null,
                    CombatEngineLayers.CONTRAILS_LAYER,
                    0f
            );
     }
     else {            MagicTrailPlugin.addTrailMemberAdvanced(
             null,
             id,
             sprite,
             renderPoints.get(i),
             0f,
             0f,
             meanAngle,
             0f,
             0f,
             trailWidth,
             0,
             color,
             color,
             (float) color.getAlpha() / 255f,
             0f,
             0f,
             0f,
             false,
             25f,
             0f,
             0f,
             null,
             null,
             CombatEngineLayers.UNDER_SHIPS_LAYER,
             0f
     );
     }
        }
    }

    protected Color computeColorForSegment(int renderPointIndex) {
        float indexF = Misc.interpolate(0, trailColor.length - 1, (float) renderPointIndex / (renderPoints.size() - 1));
        Color lower = trailColor[(int) Math.floor(indexF)];
        Color higher = trailColor[(int) Math.ceil(indexF)];
        float amount = indexF - (float) Math.floor(indexF);
        return Misc.interpolateColor(lower, higher, amount);
    }

    //String physics ported from: http://web.archive.org/web/20160418004153/http://freespace.virgin.net/hugo.elias/models/m_string.htm
    protected void computeStringPhysics(float amount) {
        for (int i = 0; i < segmentPoints.size(); i++) {
            Vector2f result = new Vector2f();
            for (int j = 1; j <= ropeParams.neighbors; j++) {
                Vector2f previous;
                if (i - j >= 0) {
                    previous = Vector2f.sub(segmentPoints.get(i - j).getLocation(), segmentPoints.get(i).getLocation(), null);
                    float prevMagnitude = previous.length();
                    float prevExtension = prevMagnitude - (ropeParams.segmentGoalLength * j);
                    previous.scale(1f / prevMagnitude);
                    previous.scale(prevExtension);
                } else {
                    previous = new Vector2f();
                }
                result = Vector2f.add(result, previous, null);

                Vector2f next;
                if (i + j < segmentPoints.size()) {
                    next = Vector2f.sub(segmentPoints.get(i + j).getLocation(), segmentPoints.get(i).getLocation(), null);
                    float nextMagnitude = next.length();
                    float nextExtension = nextMagnitude - (ropeParams.segmentGoalLength * j);
                    next.scale(1f / nextMagnitude);
                    next.scale(nextExtension);
                } else { //end segment
                    next = new Vector2f();
                }
                result = Vector2f.add(result, next, null);
            }

            Vector2f vel = segmentPoints.get(i).getVelocity();
            vel.scale(ropeParams.damping);
            result.scale(ropeParams.scale);
            segmentPoints.get(i).accel = result; //for usage elsewhere
            vel = Vector2f.add(vel, result, null);
            segmentPoints.get(i).getVelocity().set(vel); //I thought I had a direct reference, but it doesn't work without this

            //update positions based on velocity (will handle this next tick)
        }

        //apply positions (again will handle this next tick)
    }

    protected double getMeanAngle(double[] anglesDeg) {
        double x = 0.0;
        double y = 0.0;

        for (double angleD : anglesDeg) {
            double angleR = Math.toRadians(angleD);
            x += Math.cos(angleR);
            y += Math.sin(angleR);
        }
        double avgR = Math.atan2(y / anglesDeg.length, x / anglesDeg.length);
        return Math.toDegrees(avgR);
    }

    public void setSegmentLength(float length) {
        this.ropeParams.segmentGoalLength = length;
    }

    public void setColor(Color[] color) {
        this.trailColor = color;
    }
}
