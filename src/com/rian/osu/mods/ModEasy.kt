package com.rian.osu.mods

import com.rian.osu.GameMode
import com.rian.osu.beatmap.sections.BeatmapDifficulty
import com.rian.osu.utils.CircleSizeCalculator
import ru.nsu.ccfit.zuev.osu.game.mods.GameMod

/**
 * Represents the Easy mod.
 */
class ModEasy : Mod(), IModUserSelectable, IModApplicableToDifficulty {
    override val encodeChar = 'e'
    override val name = "Easy"
    override val acronym = "EZ"
    override val textureNameSuffix = "easy"
    override val enum = GameMod.MOD_EASY
    override val isRanked = true
    override val incompatibleMods = super.incompatibleMods + ModHardRock::class

    override fun calculateScoreMultiplier(difficulty: BeatmapDifficulty) = 0.5f

    override fun applyToDifficulty(mode: GameMode, difficulty: BeatmapDifficulty) = difficulty.run {
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

        ar *= ADJUST_RATIO
        od *= ADJUST_RATIO
        hp *= ADJUST_RATIO
    }

    override fun equals(other: Any?) = other === this || other is ModEasy
    override fun hashCode() = super.hashCode()
    override fun deepCopy() = ModEasy()

    companion object {
        private const val ADJUST_RATIO = 0.5f
    }
}