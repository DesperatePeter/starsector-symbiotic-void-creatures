package tecrys.svc.weapons.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import tecrys.svc.plugins.tentacle_render;
import tecrys.svc.plugins.tentacle_render.SegmentData;

public class tentacle_engine_02 extends BaseCombatLayeredRenderingPlugin implements EveryFrameWeaponEffectPlugin {
	Vector2f center;

	private boolean runOnce=false;
	private float counter1 = 0f;
	private float engineLevel = 0f;
	private float engineGain = 1f;

	//private float range =0f;
	private float animSpeed = 0f;

	private float turnrate = 0f;

	private float turnRateGain = 1f;
	private float maxTurnRate = 0f;
	private float minTurnRate = 0f;

	private float animSpeedMult =0f;
	private Vector2f size = new Vector2f();




	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {



		if(weapon.getSlot().isHidden())return;
		if(engine.isPaused())return;
//		if(!weapon.getShip().isAlive())return;
		if(!tentacle_render.screen(size.y, weapon.getLocation()))return;

		if(!runOnce){
			size = weapon.getSpec().getTurretFireOffsets().get(0);
			maxTurnRate = weapon.getTurnRate();
			minTurnRate = 0.01f;
			MathUtils.getRandomNumberInRange(0f,1f);
			animSpeedMult=weapon.getSpec().getBaseValue()*MathUtils.getRandomNumberInRange(0.9f,1.1f);
			Global.getLogger(this.getClass()).info("TURN RATE = "+maxTurnRate);
			runOnce = true;
		}

		ShipAPI ship = weapon.getShip();
		ShipEngineControllerAPI ec = ship.getEngineController();
		if(ec.isAccelerating()||ec.isDecelerating()||ec.isAcceleratingBackwards()||ec.isStrafingLeft()||ec.isStrafingRight()||ec.isTurningLeft()||ec.isTurningRight()){
			if(engineLevel<1f){
				engineLevel+=engineGain*amount;
			}
		}else{
			if(engineLevel>0f){
				engineLevel-=engineGain*amount;
			}
		}

		animSpeed = 0.2f+0.4f*engineLevel+0.4f*ship.getFluxLevel();






		Vector2f start = new Vector2f(0,0);
		float startAngle = weapon.getSlot().getAngle()+weapon.getShip().getFacing();
		Vector2f curveStart = MathUtils.getPointOnCircumference(new Vector2f(), size.y/2,startAngle);
		Vector2f end = MathUtils.getPointOnCircumference(new Vector2f(),size.y, weapon.getCurrAngle());
		float endAngle = VectorUtils.getAngle(end,start)+180f;
		//endAngle = weapon.getCurrAngle();
		Vector2f curveEnd = MathUtils.getPointOnCircumference(new Vector2f(), size.y/2, endAngle);

		List<SegmentData> points = tentacle_render.makeSegmentsBezierCurve(
				Arrays.asList(tentacle_render.add2f(start,curveStart), tentacle_render.add2f(end,curveEnd)), //List<Vector4f> points
				size.x/2f, //float segmentLength
				new Vector2f(size.x,0f), //Vector2f width
				size.x/4f,//float offsetWaveformSize
				Arrays.asList(new Vector2f(0.5f,0),new Vector2f(5f,-counter1)),//List<Vector2f> offsetWaveformParams
				size.x, //float widthWaveformSize
				Arrays.asList(new Vector2f(0.5f,1f),new Vector2f(1f,1f))//List<Vector2f> widthWaveformParams
		);

		String texName = weapon.getSpec().getHardpointSpriteName();
		SpriteAPI tex = Global.getSettings().getSprite(texName);
		tentacle_render.renderQuadStrip(new tentacle_render.renderData(
				weapon.getLocation(),
				points,
				tex,
				//Global.getSettings().getSprite("fx","BGE_tentacle"),
				32, new Vector2f(0,0), Color.WHITE,0.3f,0, CombatEngineLayers.BELOW_SHIPS_LAYER

		));




		////set directions
		float DC = weapon.getCurrAngle();
		float SA = weapon.getSlot().getAngle()+ship.getFacing();//HOST.getFacing()+weapon.getSlot().getAngle()+MathUtils.getShortestRotation(HOST.getFacing(), SHIP.getFacing());		//Direction of weapon slot

		//TFD3C4_misc.testRender(weapon.getLocation(), SA);

		float Dir_Goal = getVectorThrusterDirection(weapon);

		float DP=Dir_Goal;

		float halfarc = weapon.getArc()/2f;

		if( Math.abs(MathUtils.getShortestRotation(SA,Dir_Goal))>halfarc){ DP = SA+halfarc*Math.signum(MathUtils.getShortestRotation(SA,Dir_Goal));} //clamp direction inside arc


		float difP = MathUtils.getShortestRotation(DC, DP);

		turnrate = maxTurnRate;
		float maxDifAngle = halfarc/4f;

		float MR = maxTurnRate;
		if(Math.abs(difP)<maxDifAngle){
			MR= minTurnRate*maxTurnRate + (1-minTurnRate)*maxTurnRate*(Math.abs(difP)/maxDifAngle);
		}

		if(turnrate<=MR){turnrate+=(turnRateGain*amount);}else{turnrate=MR;}


		if(Math.abs(difP)<maxDifAngle){
			turnrate= minTurnRate*maxTurnRate + (1-minTurnRate)*maxTurnRate*(Math.abs(difP)/maxDifAngle);
		}

///work the engine
		///actual rotation
		weapon.setCurrAngle(DC+amount*Math.signum(MathUtils.getShortestRotation(DC, DP))*turnrate);

		counter1+=amount*animSpeed*animSpeedMult;




	}


	private static float getVectorThrusterDirection(WeaponAPI weapon){
		float result = 0;
		ShipAPI ship = weapon.getShip();
		ShipAPI host = ship;
		if(ship.getParentStation()!=null)host=ship.getParentStation();
		ShipEngineControllerAPI ec = host.getEngineController();
		boolean burn=false;
		boolean turn=false;

		float SA = weapon.getSlot().getAngle()+ship.getFacing();

		float SD = host.getFacing();
		float Dir_Move = SA;
		float Dir_Goal = Dir_Move;


		if(ec.isAccelerating()){Dir_Move=SD+180f; burn=true;}
		if(ec.isAcceleratingBackwards()){Dir_Move=SD+0.f; burn=true;}
		if(ec.isStrafingLeft()){Dir_Move=SD+(-90f); burn=true;}
		if(ec.isStrafingRight()){Dir_Move=SD+90f; burn=true;}

		if(ec.isAccelerating()&&ec.isStrafingLeft()){Dir_Move=SD+(-135f);}
		if(ec.isAccelerating()&&ec.isStrafingRight()){Dir_Move=SD+135f;}
		if(ec.isAcceleratingBackwards()&&ec.isStrafingLeft()){Dir_Move=SD+(-45f);}
		if(ec.isAcceleratingBackwards()&&ec.isStrafingRight()){Dir_Move=SD+45f;}

		if(ec.isDecelerating()){Dir_Move=VectorUtils.getFacing(host.getVelocity()); burn=true;}

		if(burn)Dir_Goal=Dir_Move;

		float SLA =  VectorUtils.getAngle(host.getLocation(),weapon.getLocation());
		float Dir_Turn=SLA;
		if(ec.isTurningLeft()){Dir_Turn=SLA-90f; turn=true;}
		if(ec.isTurningRight()){Dir_Turn=SLA+90f; turn=true;}

		if(turn)Dir_Goal=Dir_Turn;

		if(burn && turn){
			Dir_Goal=(Dir_Move+Dir_Turn)/2f;
		}



		return Dir_Goal;
	}

}
