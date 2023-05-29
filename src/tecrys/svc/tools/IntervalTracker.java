package tecrys.svc.tools;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;

public class IntervalTracker {
    float timeOfNextElapse, min, max;
    boolean includePausedTime;
    CombatEngineAPI engine;
    
    final void init(float min, float max, boolean includePausedTime) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
        this.includePausedTime = includePausedTime;
        this.timeOfNextElapse = 0;
    }
    float incrementInterval(float time) {
        return timeOfNextElapse = time + ((min == max)
                ? min : min + (max - min) * (float)Math.random());
    }
    
    public IntervalTracker() {
        init(1, 1, false);
    }
    public IntervalTracker(float intervalDuration) {
        init(intervalDuration, intervalDuration, false);
    }
    public IntervalTracker(float minIntervalDuration, float maxIntervalDuration) {
        init(minIntervalDuration, maxIntervalDuration, false);
    }
    public IntervalTracker(float minIntervalDuration, float maxIntervalDuration, boolean includePausedTime) {
        init(minIntervalDuration, maxIntervalDuration, includePausedTime);
    }

    public void reset() {
        engine = Global.getCombatEngine();
        incrementInterval(engine.getTotalElapsedTime(includePausedTime));
    }
    public boolean intervalIsFixed() {
        return min == max;
    }
    public float getAverageInterval() {
        return (min + max) / 2;
    }
    public float getMinimumInterval() {
        return min;
    }
    public float getMaximumInterval() {
        return max;
    }
    public void setInterval(float intervalDuration) {
        min = max = intervalDuration;
    }
    public void setInterval(float minIntervalDuration, float maxIntervalDuration) {
        min = Math.min(minIntervalDuration, maxIntervalDuration);
        max = Math.max(minIntervalDuration, maxIntervalDuration);
    }
    public boolean intervalElapsed() {
        if(engine != Global.getCombatEngine()) reset();
        
        float time = engine.getTotalElapsedTime(includePausedTime);
        
        if(timeOfNextElapse <= time) {
            incrementInterval(timeOfNextElapse);
            if(timeOfNextElapse <= time) incrementInterval(time);
            return true;
        } else return false;
    }
}
