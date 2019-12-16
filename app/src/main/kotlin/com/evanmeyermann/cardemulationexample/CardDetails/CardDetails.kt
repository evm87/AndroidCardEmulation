package com.evanmeyermann.cardemulationexample.CardDetails

import android.support.v4.app.DialogFragment.STYLE_NORMAL
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.transition.TransitionManager
import android.view.LayoutInflater
import com.evanmeyermann.cardemulationexample.R
import com.evanmeyermann.cardemulationexample.dialogue.FragmentForgetNetworkDialogue
import com.evanmeyermann.cardemulationexample.injection.Injector
import com.evanmeyermann.cardemulationexample.ViewContainer
import com.evanmeyermann.cardemulationexample.databinding.ViewActivityCardDetailsBinding
import com.evanmeyermann.cardemulationexample.presenters.UserDetailsPresenter
import com.evanmeyermann.cardemulationexample.presenters.UserDetailsView
import kotlinx.android.synthetic.main.view_activity_card_details.*
import net.grandcentrix.thirtyinch.TiActivity

private const val TAG = "CardActvity"
private const val TAG_FORGET_CARD_DIALOGUE = "forget_card_details_dialogue"

class UserDetailsActivity : TiActivity<UserDetailsPresenter, UserDetailsView>(), UserDetailsView {

    companion object {

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, UserDetailsActivity::class.java)
            return intent
        }
    }

    private var viewContainer = ViewContainer()

    override fun providePresenter(): UserDetailsPresenter = Injector.provider.provideUserDetailsPresenter()

    override fun onForgetUserDetailsClicked() {
            presenter.onForgetDetailsClicked()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewContainer.attach(findViewById(android.R.id.content))

        // Enable back button on action bar
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun showUserDetailsActivity(name: String) {

        val viewBinding = ViewActivityCardDetailsBinding.inflate(LayoutInflater.from(this)).apply {
            val nameField = this.cardFirstNameInput
            val cardNumberField = this.cardCardNumberInput
            val securityCodeField = this.cardSecurityCodeInput

            if (name.isNotEmpty()) {
                nameField.setText(name)
            }

            visibilityButton.setOnClickListener {
                //If the input type is password, change input type to text only and untransform
                if (cardNumberField.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                    cardNumberField.inputType = InputType.TYPE_CLASS_TEXT
                    cardNumberField.transformationMethod = null
                    cardNumberField.setSelection(cardNumberField.text.length)
                    visibilityButton.setImageDrawable(applicationContext.getDrawable(R.drawable.ic_visibility))
                } else {
                    cardNumberField.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    cardNumberField.transformationMethod = PasswordTransformationMethod.getInstance()
                    cardNumberField.setSelection(cardNumberField.text.length)
                    visibilityButton.setImageDrawable(applicationContext.getDrawable(R.drawable.ic_visibility_off))
                }
            }

            hasSecurityCodeCheckbox.setOnClickListener {
                TransitionManager.beginDelayedTransition(parentView)

                if (hasSecurityCodeCheckbox.isChecked) {
                    // Show the middle name option
                    val hiddenConstraints = ConstraintSet()
                    hiddenConstraints.clone(parentView)
                    hiddenConstraints.connect(warningBox.id, ConstraintSet.TOP, card_security_code_underline.id, ConstraintSet.BOTTOM)
                    hiddenConstraints.applyTo(parentView)
                } else {
                    // Hide the middle name option
                    val visibleConstraints = ConstraintSet()
                    visibleConstraints.clone(parentView)
                    visibleConstraints.connect(warningBox.id, ConstraintSet.TOP, card_card_number_underline.id, ConstraintSet.BOTTOM)
                    visibleConstraints.applyTo(parentView)
                }

            }

            cardNumberField.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            cardNumberField.transformationMethod = PasswordTransformationMethod.getInstance()

            saveDetailsBtn.setOnClickListener {
                val hasSecurityCode = this.hasSecurityCodeCheckbox.isChecked
                presenter.onSaveDetailsClicked(nameField.text.toString(), cardNumberField.text.toString(), hasSecurityCode, securityCodeField.text.toString())
                finish()
            }

            forgetDetailsButton.setOnClickListener {
                val dialogue = FragmentForgetNetworkDialogue.newInstance()

                dialogue.setStyle(STYLE_NORMAL, R.style.ThemeOverlay_Dialogue)
                dialogue.isCancelable = true
                dialogue.show(supportFragmentManager, TAG_FORGET_CARD_DIALOGUE)
            }
        }
        viewContainer.transitionToScene(viewBinding, TAG)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
