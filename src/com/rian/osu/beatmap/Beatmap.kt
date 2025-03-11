package com.rian.osu.beatmap

import com.rian.osu.GameMode
import com.rian.osu.beatmap.hitobject.HitObject
import com.rian.osu.beatmap.hitobject.Slider
import com.rian.osu.beatmap.sections.*
import com.rian.osu.mods.IModApplicableToBeatmap
import com.rian.osu.mods.IModApplicableToDifficulty
import com.rian.osu.mods.IModApplicableToDifficultyWithSettings
import com.rian.osu.mods.IModApplicableToHitObject
import com.rian.osu.mods.IModApplicableToHitObjectWithSettings
import com.rian.osu.mods.Mod
import com.rian.osu.mods.ModNightCore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ensureActive

/**
 * Represents a beatmap.
 */
open class Beatmap(
    /**
     * The [GameMode] this [Beatmap] was parsed as.
     */
    @JvmField
    val mode: GameMode
) : IBeatmap, Cloneable {
    override var formatVersion = 14
    override val general = BeatmapGeneral()
    override val metadata = BeatmapMetadata()
    override var difficulty = BeatmapDifficulty()
    override val events = BeatmapEvents()
    override val colors = BeatmapColor()
    override val controlPoints = BeatmapControlPoints()
    override var hitObjects = BeatmapHitObjects()
    override var filePath = ""
    override var md5 = ""

    override val maxCombo by lazy {
        hitObjects.objects.sumOf {
            if (it is Slider) it.nestedHitObjects.size else 1
        }
    }

    /**
     * Constructs a [DroidPlayableBeatmap] from this [Beatmap], where all [HitObject] and [BeatmapDifficulty]
     * [Mod]s have been applied, and [HitObject]s have been fully constructed.
     *
     * @param mods The [Mod]s to apply to the [Beatmap]. Defaults to No Mod.
     * @param scope The [CoroutineScope] to use for coroutines.
     * @return The [DroidPlayableBeatmap].
     */
    @JvmOverloads
    fun createDroidPlayableBeatmap(
        mods: Iterable<Mod>? = null,
        scope: CoroutineScope? = null
    ) = DroidPlayableBeatmap(createPlayableBeatmap(GameMode.Droid, mods, scope), mods)

    /**
     * Constructs a [StandardPlayableBeatmap] from this [Beatmap], where all [HitObject] and [BeatmapDifficulty]
     * [Mod]s have been applied, and [HitObject]s have been fully constructed.
     *
     * @param mods The [Mod]s to apply to the [Beatmap]. Defaults to No Mod.
     * @param scope The [CoroutineScope] to use for coroutines.
     * @return The [StandardPlayableBeatmap].
     */
    @JvmOverloads
    fun createStandardPlayableBeatmap(
        mods: Iterable<Mod>? = null,
        scope: CoroutineScope? = null
    ) = StandardPlayableBeatmap(createPlayableBeatmap(GameMode.Standard, mods, scope), mods)

    private fun createPlayableBeatmap(mode: GameMode, mods: Iterable<Mod>?, scope: CoroutineScope?): Beatmap {
        if (this.mode == mode && (mods?.firstOrNull() == null)) {
            // Beatmap is already playable as is.
            return this
        }

        val converter = BeatmapConverter(this, scope)

        // Convert
        val converted = converter.convert()

        // Apply difficulty mods
        mods?.filterIsInstance<IModApplicableToDifficulty>()?.forEach {
            scope?.ensureActive()
            it.applyToDifficulty(mode, converted.difficulty)
        }

        mods?.filterIsInstance<IModApplicableToDifficultyWithSettings>()?.forEach {
            scope?.ensureActive()
            it.applyToDifficulty(mode, converted.difficulty, mods)
        }

        val processor = BeatmapProcessor(converted, scope)

        processor.preProcess()

        // Compute default values for hit objects, including creating nested hit objects in-case they're needed
        converted.hitObjects.objects.forEach {
            scope?.ensureActive()
            it.applyDefaults(converted.controlPoints, converted.difficulty, mode, scope)
        }

        mods?.filterIsInstance<IModApplicableToHitObject>()?.forEach {
            for (obj in converted.hitObjects.objects) {
                scope?.ensureActive()
                it.applyToHitObject(mode, obj)
            }
        }

        mods?.filterIsInstance<IModApplicableToHitObjectWithSettings>()?.forEach {
            for (obj in converted.hitObjects.objects) {
                scope?.ensureActive()
                it.applyToHitObject(mode, obj, mods)
            }
        }

        processor.postProcess()

        mods?.filterIsInstance<IModApplicableToBeatmap>()?.forEach {
            scope?.ensureActive()
            it.applyToBeatmap(converted)
        }

        return converted
    }

    public override fun clone() = super.clone() as Beatmap
}