package com.evanmeyermann.cardemulationexample.home

import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.util.Log
import com.evanmeyermann.cardemulationexample.DisposableWrapper
import com.evanmeyermann.cardemulationexample.formatTimezoneOffset
import com.evanmeyermann.cardemulationexample.injection.InjectorProvider
import com.evanmeyermann.cardemulationexample.presenters.HomePresenter
import com.evanmeyermann.cardemulationexample.presenters.HomeView
import com.evanmeyermann.cardemulationexample.R
import com.evanmeyermann.cardemulationexample.nfc.CardService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

private const val TAG = "HomePresenter"
// Constant strings for creating keys for MIME's
private const val SSID = "name"
private const val PASSWORD = "password"
private const val TIMEZONE = "timezone"
private const val IS_HIDDEN = "hasSecurityCode"
private const val BSSID = "securityCode"

// Constants for broadcasting intents
const val ACTION_UPDATE_CAN_BROADCAST = "broadcastIntent"
const val BROADCAST_STATUS_HEADER = "broadcastStatus"
const val ACTION_NDEF = "ndefIntent"
const val NDEF_STATUS_MESSAGE = "ndefMessage"


class AndroidHomePresenter(private val context: Context, injector: InjectorProvider) : HomePresenter() {

    //Injected properties
    private val configRepo = injector.provideRealmCardDetailsRepo()

    // Disposables
    private val realmDisposable = DisposableWrapper(viewVisibleDisposables)

    // ByteArray for wrapping NdefMessage with identifier data to be sent
    private var ndefFile = ByteArray(0)

    private var isTransferring = false

    override fun onAttachView(attachedView: HomeView) {
        super.onAttachView(attachedView)

        // Get the stored card info from Realm
        realmDisposable.disposable = configRepo.getCardDetails()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { cardDetails ->
                            realmDisposable.disposable = null

                            // Extract timezone ID from Realm, sanitize, and add GMT offset for display
                            val displayTimezone = if (cardDetails.timezone.isNotEmpty()) {
                                val gmtOffset = TimeZone.getTimeZone(cardDetails.timezone).formatTimezoneOffset()
                                val timezoneId = cardDetails.timezone.substringAfterLast('/').replace('_', ' ')

                                this.context.resources.getString(R.string.displayed_timezone, timezoneId, gmtOffset)
                            } else {
                                ""
                            }

                            attachedView.showHome(displayTimezone, cardDetails.name)

                        }, { error ->
                            realmDisposable.disposable = null
                            Log.e(TAG, error.message)
                        }
                )
    }

    override fun onWifiClicked() {
        // Send intent to card service to set can broadcast to false
        val changeBroadcastingStatusIntent = Intent(context, CardService::class.java).apply {
            this.action = ACTION_UPDATE_CAN_BROADCAST
            this.putExtra(BROADCAST_STATUS_HEADER, false)
        }
        this.context.startService(changeBroadcastingStatusIntent)
        view?.navigateToWifi()
    }

    override fun onTimeZoneClicked() {
        // Send intent to card service to set can broadcast to false
        val changeBroadcastingStatusIntent = Intent(context, CardService::class.java).apply {
            this.action = ACTION_UPDATE_CAN_BROADCAST
            this.putExtra(BROADCAST_STATUS_HEADER, false)
        }
        this.context.startService(changeBroadcastingStatusIntent)
        view?.navigateToTimeZone()
    }

    override fun onTransferClicked() {
        if (!isTransferring) {
            // Send intent to card service to set can broadcast to true
            val changeBroadcastingStatusIntent = Intent(context, CardService::class.java).apply {
                this.action = ACTION_UPDATE_CAN_BROADCAST
                this.putExtra(BROADCAST_STATUS_HEADER, true)

            }
            this.context.startService(changeBroadcastingStatusIntent)

            // Update the local state
            isTransferring = true

            view?.showBroadcasting(true)
        } else {
            // Send intent to card service to set can broadcast to false
            val changeBroadcastingStatusIntent = Intent(context, CardService::class.java).apply {
                this.action = ACTION_UPDATE_CAN_BROADCAST
                this.putExtra(BROADCAST_STATUS_HEADER, false)
            }

            this.context.startService(changeBroadcastingStatusIntent)

            // Update the local state
            isTransferring = false

            view?.showBroadcasting(false)
        }


        realmDisposable.disposable = configRepo.getCardDetails()
                .observeOn(Schedulers.io())
                .subscribe ({ (_, timezone, name, cardNumber, hasSecurityCode, securityCode) ->
                    realmDisposable.disposable = null

                    // Create NdefRecords for each piece of data to be sent
                    val ssidRecord = NdefRecord.createMime(SSID, name.toByteArray())
                    val passwordRecord = NdefRecord.createMime(PASSWORD, cardNumber.toByteArray())
                    val hasSecurityCodeRecord = NdefRecord.createMime(IS_HIDDEN, hasSecurityCode.toString().toByteArray())
                    val timezoneRecord = NdefRecord.createMime(TIMEZONE, timezone.toByteArray())
                    val securityCodeRecord = NdefRecord.createMime(BSSID, securityCode.toByteArray())

                    // Construct NdefMessage to be sent with NdefRecords
                    val ndefMessage = NdefMessage(ssidRecord, passwordRecord, hasSecurityCodeRecord, timezoneRecord, securityCodeRecord)

                    // Update the file containing the NdefMessage to be sent and stored in the receiving device
                    val ndefLength = ndefMessage.byteArrayLength
                    ndefFile = ByteArray(ndefLength + 2)
                    ndefFile[0] = ((65280 and ndefLength) / 256).toByte()
                    ndefFile[1] = (ndefLength and 255).toByte()
                    System.arraycopy(ndefMessage.toByteArray(), 0, ndefFile, 2, ndefMessage.byteArrayLength)

                    // Broadcast the ndef file to the service
                    val sendNdefIntent = Intent(context, CardService::class.java).apply {
                        this.action = ACTION_NDEF
                        this.putExtra(NDEF_STATUS_MESSAGE, ndefFile)
                    }

                    this.context.startService(sendNdefIntent)

                }, { error ->

                    realmDisposable.disposable = null
                    Log.e(TAG, error.message)
                })
    }
}