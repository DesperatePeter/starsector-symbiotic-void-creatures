package tecrys.svc.hullmods

import com.fs.starfarer.api.alcoholism.hullmods.BaseAlcoholHullmodEffect
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class svc_booze : BaseAlcoholHullmodEffect() {

    companion object{
        const val SENSOR_PROFILE = 10f
        const val SUPPLY_MAINTENANCE = 5f
        const val CORONA_RESISTANCE = -50f
        const val FUEL_USE_DECREASE = -20f
        const val WITHDRAWAL_FUEL_USE = 15f
        const val TEXT_PADDING = 5f
        val POSITIVE_COLOR: Color = Misc.getPositiveHighlightColor()
        val NEUTRAL_COLOR: Color = Misc.getTextColor()
        val NEGATIVE_COLOR: Color = Misc.getNegativeHighlightColor()
        val WITHDRAWAL_COLOR = Color(150, 50, 50, 255)
    }

    override fun init(spec: HullModSpecAPI?) {
        super.init(spec)
    }

    override fun applyPositives(stats: MutableShipStatsAPI, effectMult: Float, id: String?) {
        stats.run {
            fuelUseMod.modifyMult(id, getPercentToCorrectedMult(FUEL_USE_DECREASE), desc)
            dynamic.getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, getPercentToCorrectedMult(CORONA_RESISTANCE))
        }
    }

    override fun applyNegatives(stats: MutableShipStatsAPI, effectMult: Float, id: String?) {
        stats.run {
            sensorProfile.modifyMult(id, getPercentToCorrectedMult(SENSOR_PROFILE))
            suppliesPerMonth.modifyMult(id, getPercentToCorrectedMult(SUPPLY_MAINTENANCE))
        }
    }

    override fun applyWithdrawal(stats: MutableShipStatsAPI, effectMult: Float, id: String?) {
        stats.run {
            fuelUseMod.modifyMult(id, getPercentToCorrectedMult(WITHDRAWAL_FUEL_USE))
        }
    }

    override fun addPositiveEffectTooltip(tooltip: TooltipMakerAPI, effectMult: Float) {
        tooltip.run {
            addSectionHeading("Positive Effects", NEUTRAL_COLOR, POSITIVE_COLOR, Alignment.MID, TEXT_PADDING)
            val fuelText = "Decreases fuel use by"
            addFormattedPercentPara(this, fuelText, FUEL_USE_DECREASE, effectMult, POSITIVE_COLOR)
            val coronaText = "Decreases impact of hyperspace storms and coronas by"
            addFormattedPercentPara(this, coronaText, CORONA_RESISTANCE, effectMult, POSITIVE_COLOR)
        }
    }

    override fun addNegativeEffectTooltip(tooltip: TooltipMakerAPI, effectMult: Float) {
        tooltip.run {
            addSectionHeading("Negative Effects", NEUTRAL_COLOR, NEGATIVE_COLOR, Alignment.MID, TEXT_PADDING)
            val supplyText = "Increases supply maintenance by"
            addFormattedPercentPara(this, supplyText, SUPPLY_MAINTENANCE, effectMult, NEGATIVE_COLOR)
            val sensorText = "Increases sensor profile by"
            addFormattedPercentPara(this, sensorText, SENSOR_PROFILE, effectMult, NEGATIVE_COLOR)
        }
    }

    override fun addWithdrawalEffectTooltip(tooltip: TooltipMakerAPI, effectMult: Float) {
        tooltip.run {
            addSectionHeading("Withdrawal Effects", NEUTRAL_COLOR, WITHDRAWAL_COLOR, Alignment.MID, TEXT_PADDING)
            val fuelText = "Increases fuel use by"
            addFormattedPercentPara(this, fuelText, WITHDRAWAL_FUEL_USE, effectMult, WITHDRAWAL_COLOR)
        }
    }

    private fun addFormattedPercentPara(tooltip: TooltipMakerAPI, text: String, value: Float, effectMult: Float, highlightColor: Color) {
        tooltip.addPara("$text %s [Max.: %s]", TEXT_PADDING, highlightColor,
            getAbsPercentStringForTooltip(value, effectMult), getAbsPercentStringForTooltip(value))
    }
}