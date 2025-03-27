package com.rian.osu.mods

/**
 * Represents the Custom Speed mod. Serves as a container for custom speed multipliers.
 *
 * @param trackRateMultiplier The multiplier to apply to the track's playback rate.
 */
class ModCustomSpeed(trackRateMultiplier: Float) : ModRateAdjust(trackRateMultiplier) {
    override val isRanked = true

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }

        if (other !is ModCustomSpeed) {
            return false
        }

        return super.equals(other)
    }

    override fun hashCode() = super.hashCode()
    override fun deepCopy() = ModCustomSpeed(trackRateMultiplier)
}