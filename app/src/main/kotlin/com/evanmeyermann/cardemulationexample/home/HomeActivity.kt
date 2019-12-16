package com.evanmeyermann.cardemulationexample.home

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import com.evanmeyermann.cardemulationexample.CardDetails.UserDetailsActivity
import com.evanmeyermann.cardemulationexample.presenters.HomePresenter
import com.evanmeyermann.cardemulationexample.presenters.HomeView
import com.evanmeyermann.cardemulationexample.ViewContainer
import com.evanmeyermann.cardemulationexample.databinding.ViewActivityHomeBinding
import net.grandcentrix.thirtyinch.TiActivity
import com.evanmeyermann.cardemulationexample.Timezone.TimeZoneActivity
import com.evanmeyermann.cardemulationexample.R
import com.evanmeyermann.cardemulationexample.injection.Injector

private const val TAG = "HomeActivity"
private const val TAG_VIEW_CONTAINER = "ViewContainer"

class HomeActivity : TiActivity<HomePresenter, HomeView>(), HomeView {

    private var viewContainer = ViewContainer()

    override fun providePresenter(): HomePresenter = Injector.provider.provideHomePresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewContainer.attach(findViewById(android.R.id.content))
    }

    override fun showHome(timezone: String, name: String) {
        val homeBinding = ViewActivityHomeBinding.inflate(LayoutInflater.from(this)).apply {
            if (name.isBlank()) {
                this.cardDetailsBodyText.text = resources.getString(R.string.enter_card_details)
            } else {
                this.cardDetailsBodyText.text = name
                this.cardDetailsBodyText.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            }

            this.cardDetailsTab.setOnClickListener {
                presenter.onWifiClicked()
            }

            if (timezone.isBlank()) {
                this.timezoneBodyText.text = resources.getString(R.string.select_your_timezone)
            } else {
                this.timezoneBodyText.text = timezone
                this.timezoneBodyText.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
            }
            this.timezoneTab.setOnClickListener {
                presenter.onTimeZoneClicked()
            }

            if (name.isNotEmpty() && timezone.isNotEmpty()) {
                this.transferButton.isEnabled = true
                this.transferButton.background.setTint(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                this.transferButton.setOnClickListener {
                    presenter.onTransferClicked()
                }
            }
        }

        viewContainer.transitionToScene(homeBinding, TAG)
    }

    override fun showBroadcasting(isBroadcasting: Boolean) {
        (viewContainer.currentViewBinding as ViewActivityHomeBinding).apply {
            if (isBroadcasting) {
                this.transferButton.background.setTint(ContextCompat.getColor(applicationContext, R.color.bright_orange))
                this.transferButton.text = resources.getString(R.string.broadcasting)
                this.nfcText.visibility = View.VISIBLE
                this.aviIndicator.visibility = View.VISIBLE
            } else {
                this.transferButton.background.setTint(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                this.transferButton.text = resources.getString(R.string.transfer_settings)
                this.nfcText.visibility = View.INVISIBLE
                this.aviIndicator.visibility = View.INVISIBLE
            }
        }
    }

    override fun navigateToWifi() {

        val wifiIntent = UserDetailsActivity.newIntent(this)
        startActivity(wifiIntent)
    }

    override fun navigateToTimeZone() {

        val timeZoneIntent = TimeZoneActivity.newIntent(this)
        startActivity(timeZoneIntent)
    }
}