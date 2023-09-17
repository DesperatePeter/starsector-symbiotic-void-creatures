package tecrys.svc.hullmods;

import com.fs.starfarer.api.alcoholism.hullmods.BaseAlcoholHullmodEffect;
import com.fs.starfarer.api.alcoholism.hullmods.campaignEffects.SustainedBurnNavigationModifier;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class svc_cocktail extends BaseAlcoholHullmodEffect {
    public static final float FUEL_USE_DECREASE = -20f;
    public static final float MAX_BURN_INCREASE = +1;
    public static final float HULL_DAMAGE_DECREASE = -30f;

    public static final float NEG_WEAPON_DAMAGE_PERCENT = -10f;
    public static final float NEG_EMP_DAMAGE_TAKEN_PERCENT = +10f;

    public static final float WITHDRAWAL_MAX_CR_REDUCTION = -20f;
    public static final float WITHDRAWAL_ACCEL_DECREASE = -20;
    public static final float WITHDRAWAL_SPEED_DECREASE = -20;
    public static final float WITHDRAWAL_MANEUV_DECREASE = -20;

    public static final float WITHDRAWAL_85_FUEL_USE_INCREASE = 20f;
    public static final float WITHDRAWAL_70_FUEL_USE_INCREASE = 10f;

    @Override
    public void init(HullModSpecAPI spec) {
        super.init(spec);
    }

    @Override
    public void applyPositives(MutableShipStatsAPI stats, float effectMult, String id) {
        //decreases fuel use and hull damage taken, increases max burn level by 1,

        stats.getFuelUseMod().modifyMult(id, getPercentToCorrectedMult(FUEL_USE_DECREASE), getDesc());
        stats.getMaxBurnLevel().modifyFlat(id, MAX_BURN_INCREASE, getDesc());
        stats.getHullDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(HULL_DAMAGE_DECREASE), getDesc());

    }

    @Override
    public void applyNegatives(MutableShipStatsAPI stats, float effectMult, String id) {
        //decrease weapon damage dealt
        //increases emp damage taken
        stats.getBallisticWeaponDamageMult().modifyMult(id, getPercentToCorrectedMult(NEG_WEAPON_DAMAGE_PERCENT), getDesc());
        stats.getEnergyWeaponDamageMult().modifyMult(id, getPercentToCorrectedMult(NEG_WEAPON_DAMAGE_PERCENT), getDesc());
        stats.getMissileWeaponDamageMult().modifyMult(id, getPercentToCorrectedMult(NEG_WEAPON_DAMAGE_PERCENT), getDesc());
        stats.getEmpDamageTakenMult().modifyMult(id, getPercentToCorrectedMult(NEG_EMP_DAMAGE_TAKEN_PERCENT), getDesc());

    }

    @Override
    public void applyWithdrawal(MutableShipStatsAPI stats, float effectMult, String id) {
        //Lower max CR ,
        //Reduce ship accel and speed,
        //fuel use increase +10%-20%
        stats.getMaxCombatReadiness().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MAX_CR_REDUCTION), getDesc());
        stats.getMaxSpeed().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_SPEED_DECREASE), getDesc());

        if(effectMult > 0.5f){
            stats.getAcceleration().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_ACCEL_DECREASE), getDesc());
            stats.getMaxTurnRate().modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_MANEUV_DECREASE), getDesc());
        }

        if(effectMult > 0.7f){
            float decrease = effectMult > 0.85 ? WITHDRAWAL_85_FUEL_USE_INCREASE : WITHDRAWAL_70_FUEL_USE_INCREASE;
            stats.getFuelUseMod().modifyFlat(id, decrease, getDesc());
        }
    }

    @Override
    public void addPositiveEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Positive Effect", Misc.getTextColor(), new Color(50, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Decreases max. fuel use by %s  [Max.: %s]",
                opad,
                positive,
                getAbsPercentStringForTooltip(FUEL_USE_DECREASE, effectMult),
                getAbsPercentStringForTooltip(FUEL_USE_DECREASE));

        tooltip.addPara("Increases max. burn by %s",
                spad,
                positive,
                getAbsPercentStringForTooltip(MAX_BURN_INCREASE, effectMult),
                getAbsPercentStringForTooltip(MAX_BURN_INCREASE));

        tooltip.addPara("Decreases hull damage taken by %s",
                spad,
                positive,
                getAbsPercentStringForTooltip(HULL_DAMAGE_DECREASE, effectMult),
                getAbsPercentStringForTooltip(HULL_DAMAGE_DECREASE));


    }

    @Override
    public void addNegativeEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color neutral = Misc.getGrayColor();
        Color negative = Misc.getNegativeHighlightColor();

        tooltip.addSectionHeading("Negative Effect", Misc.getTextColor(), new Color(150, 100, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Reduces weapon damage dealt by %s  [Max.: %s]",
                opad,
                negative,
                getAbsPercentStringForTooltip(NEG_WEAPON_DAMAGE_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_WEAPON_DAMAGE_PERCENT));

        tooltip.addPara("Increases EMP damage taken by %s",
                spad,
                negative,
                getAbsPercentStringForTooltip(NEG_EMP_DAMAGE_TAKEN_PERCENT, effectMult),
                getAbsPercentStringForTooltip(NEG_EMP_DAMAGE_TAKEN_PERCENT));
    }

    @Override
    public void addWithdrawalEffectTooltip(TooltipMakerAPI tooltip, float effectMult) {
        float opad = 10f;
        float spad = 3f;
        Color positive = Misc.getPositiveHighlightColor();
        Color negative = Misc.getNegativeHighlightColor();
        Color bad = Color.red;

        tooltip.addSectionHeading("Withdrawal Effect", Misc.getTextColor(), new Color(150, 50, 50, 255), Alignment.MID, 10f);

        tooltip.addPara("Lowers max. combat readiness by %s  [Max.: %s]",
                opad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_MAX_CR_REDUCTION, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_MAX_CR_REDUCTION));

        tooltip.addPara("Reduces max. speed by %s  [Max.: %s]",
                spad,
                bad,
                getAbsPercentStringForTooltip(WITHDRAWAL_SPEED_DECREASE, effectMult),
                getAbsPercentStringForTooltip(WITHDRAWAL_SPEED_DECREASE));

        if(effectMult > 0.5f){
            tooltip.addPara("Reduces acceleration by %s  [Max.: %s]",
                    spad,
                    bad,
                    getAbsPercentStringForTooltip(WITHDRAWAL_ACCEL_DECREASE, effectMult),
                    getAbsPercentStringForTooltip(WITHDRAWAL_ACCEL_DECREASE));

            tooltip.addPara("Reduces maneuverability by %s  [Max.: %s]",
                    spad,
                    bad,
                    getAbsPercentStringForTooltip(WITHDRAWAL_MANEUV_DECREASE, effectMult),
                    getAbsPercentStringForTooltip(WITHDRAWAL_MANEUV_DECREASE));
        }

        if(effectMult > 0.7f){
            float maxDecrease = effectMult > 0.85 ? WITHDRAWAL_85_FUEL_USE_INCREASE : WITHDRAWAL_70_FUEL_USE_INCREASE;
            float decrease = maxDecrease * effectMult;

            tooltip.addPara("Reduces max. burn by %s",
                    spad,
                    bad,
                    Math.round(decrease) + "");
        }
    }
}

