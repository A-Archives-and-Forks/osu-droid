package com.rian.osu.mods

/**
 * Represents the Auto mod.
 */
class ModAuto : Mod() {
    override val name = "Auto"
    override val acronym = "AT"
    override val type = ModType.Automation
    override val textureNameSuffix = "autoplay"
    override val isValidForMultiplayer = false
    override val isValidForMultiplayerAsFreeMod = false
    override val incompatibleMods = super.incompatibleMods + arrayOf(
        ModRelax::class, ModAutopilot::class, ModPerfect::class, ModSuddenDeath::class
    )

    override fun equals(other: Any?) = other === this || other is ModAuto
    override fun hashCode() = super.hashCode()
    override fun deepCopy() = ModAuto()
}
