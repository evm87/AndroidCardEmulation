package com.evanmeyermann.cardemulationexample.presenters

import net.grandcentrix.thirtyinch.TiView

/**
 * Interface classes used to implement home view/presenter
 */
interface HomeView : TiView {

    fun showHome(timezone: String, name: String)

    fun showBroadcasting(isBroadcasting: Boolean)

    fun navigateToWifi()

    fun navigateToTimeZone()
}

abstract class HomePresenter : PresenterBase<HomeView>() {

    abstract fun onWifiClicked()

    abstract fun onTimeZoneClicked()

    abstract fun onTransferClicked()
}

