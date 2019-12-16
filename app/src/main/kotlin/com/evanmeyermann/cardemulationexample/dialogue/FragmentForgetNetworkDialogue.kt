package com.evanmeyermann.cardemulationexample.dialogue

import android.os.Bundle
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import com.evanmeyermann.cardemulationexample.CardDetails.UserDetailsActivity
import com.evanmeyermann.cardemulationexample.databinding.FragmentForgetDetailsDialogueBinding

class FragmentForgetNetworkDialogue : DialogFragment() {

    companion object {
        /**
         * Create a new instance of capacity dialogue
         */
        fun newInstance() : FragmentForgetNetworkDialogue {

            // Create new instance
            val dialogueFragment = FragmentForgetNetworkDialogue()

            return dialogueFragment

        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // If activity is null, then the fragment has not been attached which is an illegal state
        val baseContext = activity ?: context ?: throw IllegalStateException("The FragmentCapacityDialogue is not attached.")

        // Get the context (either the activity or the activity theme wrapped)
        val context = when (theme > 0) {
            true -> {
                ContextThemeWrapper(baseContext, theme)
            }
            else -> {
                baseContext
            }
        }

        // Inflate view binding
        val binding = FragmentForgetDetailsDialogueBinding.inflate(LayoutInflater.from(context))

        binding.cancelButton.setOnClickListener {
            this.dismiss()
        }

        binding.forgetNetworkButton.setOnClickListener { _ ->
            (this.activity as? UserDetailsActivity)?.onForgetUserDetailsClicked()
                this.dismiss()
        }

        val dialog = Dialog(baseContext, theme)
        dialog.setContentView(binding.root)
        dialog.create()
        return dialog

    }
}