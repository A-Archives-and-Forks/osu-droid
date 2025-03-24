package com.rian.osu.mods

import com.rian.osu.GameMode
import com.rian.osu.beatmap.sections.BeatmapDifficulty
import com.rian.osu.utils.CircleSizeCalculator
import ru.nsu.ccfit.zuev.osu.game.mods.GameMod

/**
 * Represents the Really Easy mod.
 */
class ModReallyEasy : Mod(), IModUserSelectable, IModApplicableToDifficultyWithSettings {
    override val encodeChar = 'l'
    override val acronym = "RE"
    override val textureNameSuffix = "reallyeasy"
    override val enum = GameMod.MOD_REALLYEASY

    override fun calculateScoreMultiplier(difficulty: BeatmapDifficulty) = 0.5f

    override fun applyToDifficulty(mode: GameMode, difficulty: BeatmapDifficulty, mods: Iterable<Mod>) =
        difficulty.run {
            val difficultyAdjustMod = mods.find { it is ModDifficultyAdjust } as? ModDifficultyAdjust

            if (difficultyAdjustMod?.ar == null) {
                if (mods.any { it is ModEasy }) {
                    ar *= 2
                    ar -= 0.5f
                }

                val customSpeedMultiplier = (mods.find { it is ModCustomSpeed } as? ModCustomSpeed)?.trackRateMultiplier ?: 1f

                ar -= 0.5f
                ar -= customSpeedMultiplier - 1
            }

            if (difficultyAdjustMod?.cs == null) {
                difficultyCS = when (mode) {
                    GameMode.Droid -> {
                        val scale = CircleSizeCalculator.droidCSToDroidDifficultyScale(difficultyCS)

                        CircleSizeCalculator.droidDifficultyScaleToDroidCS(scale + 0.125f)
                    }

                    GameMode.Standard -> difficultyCS * ADJUST_RATIO
                }

                gameplayCS = when (mode) {
                    GameMode.Droid -> {
                        val scale = CircleSizeCalculator.droidCSToDroidGameplayScale(gameplayCS)

                        CircleSizeCalculator.droidGameplayScaleToDroidCS(scale + 0.125f)
                    }

                    GameMode.Standard -> gameplayCS * ADJUST_RATIO
                }
            }

            if (difficultyAdjustMod?.od == null) {
                od *= ADJUST_RATIO
            }

            if (difficultyAdjustMod?.hp == null) {
                hp *= ADJUST_RATIO
            }
        }

    override fun equals(other: Any?) = other === this || other is ModReallyEasy
    override fun hashCode() = super.hashCode()
    override fun deepCopy() = ModReallyEasy()

    companion object {
        private const val ADJUST_RATIO = 0.5f
    }
}