package com.rian.osu.mods

import com.rian.osu.GameMode
import com.rian.osu.beatmap.hitobject.HitObject
import com.rian.osu.beatmap.hitobject.Slider
import com.rian.osu.beatmap.sections.BeatmapDifficulty
import com.rian.osu.utils.ModUtils
import kotlin.math.exp
import kotlin.math.pow

/**
 * Represents the Difficulty Adjust mod. Serves as a container for forced difficulty statistics.
 */
class ModDifficultyAdjust @JvmOverloads constructor(
    /**
     * The circle size to enforce.
     */
    @JvmField
    var cs: Float? = null,

    /**
     * The approach rate to enforce.
     */
    @JvmField
    var ar: Float? = null,

    /**
     * The overall difficulty to enforce.
     */
    @JvmField
    var od: Float? = null,

    /**
     * The health drain rate to enforce.
     */
    @JvmField
    var hp: Float? = null
) : Mod(), IModApplicableToDifficultyWithSettings, IModApplicableToHitObjectWithSettings {
    override val name = "Difficulty Adjust"
    override val acronym = "DA"
    override val textureNameSuffix = "difficultyadjust"

    override val isRelevant
        get() = cs != null || ar != null || od != null || hp != null

    override fun calculateScoreMultiplier(difficulty: BeatmapDifficulty): Float {
        // Graph: https://www.desmos.com/calculator/yrggkhrkzz
        var multiplier = 1f

        if (cs != null) {
            val diff = cs!! - difficulty.difficultyCS

            multiplier *=
                if (diff >= 0) 1 + 0.0075f * diff.pow(1.5f)
                else 2 / (1 + exp(-0.5f * diff))
        }

        if (od != null) {
            val diff = od!! - difficulty.od

            multiplier *=
                if (diff >= 0) 1 + 0.005f * diff.pow(1.3f)
                else 2 / (1 + exp(-0.25f * diff))
        }

        return multiplier
    }

    override fun applyToDifficulty(mode: GameMode, difficulty: BeatmapDifficulty, mods: Iterable<Mod>) =
        difficulty.let {
            it.difficultyCS = getValue(cs, it.difficultyCS)
            it.gameplayCS = getValue(cs, it.gameplayCS)
            it.ar = getValue(ar, it.ar)
            it.od = getValue(od, it.od)
            it.hp = getValue(hp, it.hp)

            // Special case for force AR, where the AR value is kept constant with respect to game time.
            // This makes the player perceive the AR as is under all speed multipliers.
            if (ar != null) {
                val preempt = BeatmapDifficulty.difficultyRange(ar!!.toDouble(), HitObject.PREEMPT_MAX, HitObject.PREEMPT_MID, HitObject.PREEMPT_MIN)
                val trackRate = ModUtils.calculateRateWithMods(mods)

                it.ar = BeatmapDifficulty.inverseDifficultyRange(preempt * trackRate, HitObject.PREEMPT_MAX, HitObject.PREEMPT_MID, HitObject.PREEMPT_MIN).toFloat()
            }
        }

    override fun applyToHitObject(mode: GameMode, hitObject: HitObject, mods: Iterable<Mod>) {
        // Special case for force AR, where the AR value is kept constant with respect to game time.
        // This makes the player perceive the fade in animation as is under all speed multipliers.
        if (ar == null) {
            return
        }

        applyFadeAdjustment(hitObject, mods)

        if (hitObject is Slider) {
            hitObject.nestedHitObjects.forEach { applyFadeAdjustment(it, mods) }
        }
    }

    private fun applyFadeAdjustment(hitObject: HitObject, mods: Iterable<Mod>) {
        val initialTrackRate = ModUtils.calculateRateWithMods(mods)
        val currentTrackRate = ModUtils.calculateRateWithMods(mods, hitObject.startTime)

        // Cancel the rate that was initially applied to timePreempt (via applyToDifficulty above and
        // HitObject.applyDefaults) and apply the current one.
        hitObject.timePreempt *= currentTrackRate / initialTrackRate

        hitObject.timeFadeIn *= currentTrackRate
    }

    private fun getValue(value: Float?, fallback: Float) = value ?: fallback

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }

        if (other !is ModDifficultyAdjust) {
            return false
        }

        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()

        result = 31 * result + cs.hashCode()
        result = 31 * result + ar.hashCode()
        result = 31 * result + od.hashCode()
        result = 31 * result + hp.hashCode()

        return result
    }

    override fun deepCopy() = ModDifficultyAdjust(cs, ar, od, hp)
}