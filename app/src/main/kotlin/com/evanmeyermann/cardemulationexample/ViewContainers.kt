package com.evanmeyermann.cardemulationexample

import android.databinding.ViewDataBinding
import android.transition.Fade
import android.transition.Scene
import android.transition.TransitionManager
import android.view.ViewGroup

/**
 * helper container class for transitioning views
 */
class ViewContainer {

    private var sceneRoot: ViewGroup? = null

    fun attach(view: ViewGroup) {
        this.sceneRoot = view
    }

    //Store current view binding.
    var currentViewBinding: ViewDataBinding? = null

    //Clean up function for the currently shown view
    private var mCleanUp: (() -> Unit)? = null

    /**
     * Helper function to transition scene to a new one. The old one is removed from the view hierarchy.
     */
    fun transitionToScene(viewBinding: ViewDataBinding, tag: String, cleanUp: (() -> Unit)? = null) {
        mCleanUp?.invoke()
        mCleanUp = cleanUp

        val sceneRoot = sceneRoot ?: return

        val scene = Scene(sceneRoot, viewBinding.root)
        sceneRoot.setTag(R.id.tag_view_name, tag)
        currentViewBinding = viewBinding

        val fadeTransition = Fade()
        TransitionManager.go(scene, fadeTransition)
    }

    fun isViewShowing(tag: String): Boolean =
            tag == sceneRoot?.getTag(R.id.tag_view_name)

    inline fun <reified T : ViewDataBinding> toViewBinding(): T? =
            if (currentViewBinding is T)
                currentViewBinding as T
            else null

    fun detach() {
        mCleanUp?.invoke()
        mCleanUp = null
        currentViewBinding = null
        sceneRoot = null
    }
}