package com.smkn8bone.jejakpklskadel.components.loading

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import com.smkn8bone.jejakpklskadel.R
import androidx.core.view.isVisible

class LoadingOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val backgroundView: View
    private val spinner: View

    init {
        LayoutInflater.from(context).inflate(R.layout.view_loading_overlay, this, true)
        backgroundView = findViewById(R.id.loadingScreen)
        spinner = findViewById(R.id.progressBar)
        visibility = View.GONE

        alpha = 0f
        scaleX = 0.95f
        scaleY = 0.95f
    }

    fun show() {
        if (isVisible) return

        visibility = View.VISIBLE
        backgroundView.visibility = View.VISIBLE
        spinner.visibility = View.VISIBLE

        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(250)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    fun hide() {
        if (!isVisible) return

        animate()
            .alpha(0f)
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(250)
            .withEndAction {
                visibility = View.GONE
                backgroundView.visibility = View.GONE
                spinner.visibility = View.GONE
            }
            .setInterpolator(OvershootInterpolator())
            .start()
    }
}