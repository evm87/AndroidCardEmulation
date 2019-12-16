package com.evanmeyermann.cardemulationexample.presenters

import net.grandcentrix.thirtyinch.TiView
import java.util.*

/**
 *  Interface classes used to implement timezone view/presenter
 */
interface TimeZoneView : TiView {

    /**
     * Called to show the timezone activity
     */
    fun showSelectTimezone(timezones: Collection<Triple<String, String, CharSequence>>, selectedTimezone: String)

    fun navigateAway()

}

abstract class TimeZonePresenter : PresenterBase<TimeZoneView>() {

    /**
     * Called to inform the presenter the timezone selection has occurred
     */
    abstract fun onTimezoneSelected(timezoneId: String)
}


