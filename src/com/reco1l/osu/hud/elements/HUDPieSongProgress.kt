package com.reco1l.osu.hud.elements

import com.reco1l.andengine.*
import com.reco1l.andengine.shape.*
import com.reco1l.framework.*
import com.reco1l.osu.hud.HUDElement
import ru.nsu.ccfit.zuev.osu.game.GameScene


class HUDPieSongProgress : HUDElement() {


    override var autoSizeAxes = Axes.Both


    private val circularProgress: Circle


    init {
        // Reference: https://github.com/ppy/osu/blob/6455c0583b5e607baeca7f584410bc63515aa619/osu.Game/Skinning/LegacySongProgress.cs

        Circle().also { clear ->

            clear.setSize(30f, 30f)
            clear.anchor = Anchor.Center
            clear.origin = Anchor.Center
            clear.color = ColorARGB.Transparent
            clear.depthInfo = DepthInfo.Clear

            attachChild(clear)
        }

        Circle().also { background ->

            background.setSize(33f, 33f)
            background.anchor = Anchor.Center
            background.origin = Anchor.Center
            background.color = ColorARGB.White
            background.depthInfo = DepthInfo.Default

            attachChild(background)
        }

        circularProgress = Circle().also { progress ->

            progress.setSize(30f, 30f)
            progress.anchor = Anchor.Center
            progress.origin = Anchor.Center
            progress.alpha = 0.6f

            attachChild(progress)
        }


        Circle().also { dot ->

            dot.setSize(4f, 4f)
            dot.anchor = Anchor.Center
            dot.origin = Anchor.Center
            dot.color = ColorARGB.White

            attachChild(dot)
        }

        onMeasureContentSize()
    }


    fun setProgress(progress: Float, isIntro: Boolean) {

        if (isIntro) {
            circularProgress.setColor(199f / 255f, 1f, 47f / 255f)
            circularProgress.setPortion(-1f + progress)
        } else {
            circularProgress.setColor(1f, 1f, 1f)
            circularProgress.setPortion(progress)
        }

    }

    override fun onGameplayUpdate(game: GameScene, secondsElapsed: Float) {
        if (game.elapsedTime < game.firstObjectStartTime) {
            setProgress((game.elapsedTime - game.initialElapsedTime) / (game.firstObjectStartTime - game.initialElapsedTime), true)
        } else {
            setProgress((game.elapsedTime - game.firstObjectStartTime) / (game.lastObjectEndTime - game.firstObjectStartTime), false)
        }
    }

}