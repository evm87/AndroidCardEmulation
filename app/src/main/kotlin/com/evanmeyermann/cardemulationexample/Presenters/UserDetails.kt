package com.evanmeyermann.cardemulationexample.presenters

import net.grandcentrix.thirtyinch.TiView

/**
 * Interface classes used to implement settings view/presenter
 */
interface UserDetailsView : TiView {

    /**
     * Called to display the wifi view
     */
    fun showUserDetailsActivity(name: String)

    fun onForgetUserDetailsClicked()

}

abstract class UserDetailsPresenter : PresenterBase<UserDetailsView>() {
    /**
     * Called to inform the presenter to save the wifi details
     */
    abstract fun onSaveDetailsClicked(name: String, cardNumber: String, hasSecurityCode: Boolean, securityCode: String)

    /**
     * Called to inform the presenter to delete the wifi details
     */
    abstract fun onForgetDetailsClicked()

}