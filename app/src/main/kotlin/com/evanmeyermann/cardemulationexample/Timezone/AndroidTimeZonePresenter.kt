package com.evanmeyermann.cardemulationexample.Timezone

import android.content.Context
import android.util.Log
import com.evanmeyermann.cardemulationexample.DisposableWrapper
import com.evanmeyermann.cardemulationexample.presenters.TimeZonePresenter
import com.evanmeyermann.cardemulationexample.presenters.TimeZoneView
import com.evanmeyermann.cardemulationexample.formatTimezoneOffset
import com.evanmeyermann.cardemulationexample.injection.InjectorProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*

private const val TAG = "TimeZonePresenter"

class AndroidTimeZonePresenter(private val context: Context, injectorProvider: InjectorProvider) : TimeZonePresenter() {

    private val configRepo = injectorProvider.provideRealmCardDetailsRepo()

    // Disposables
    private val realmDisposable = DisposableWrapper(viewVisibleDisposables)

    /**
     * Collection of timezone Ids - sanitized timezone ID's - sanitized GMT offsets for storage, display and sorting
     */
    private val sanitizedTimezoneMap = TimeZone.getAvailableIDs().filter { timezoneId ->
        // This filter matches any invalid timezone IDs which contain numbers or only capital letters
        val idFilter = ("[A-Z]?[a-z]+[_-]*[A-Za-z]*[_-]?[A-Za-z]*\$").toRegex()

        timezoneId.substringAfterLast("/")
                  .matches(idFilter)

    }.sortedBy { timezoneId ->
        // Sort by the offset
        val now = Date()
        val currentTimeZone = TimeZone.getTimeZone(timezoneId)

        currentTimeZone.getOffset(now.time)

    }.map { timezoneId ->
        // Emit the raw zoneId for storage with formatted zone ID and offset for display
        Triple(timezoneId,
               timezoneId.substringAfterLast('/').replace('_', ' '),
               TimeZone.getTimeZone(timezoneId).formatTimezoneOffset())

    }.distinctBy { (_, sanitizedId, _) ->
        // Filter out duplicate cities as some can exist in multiple regions
        sanitizedId
    }

    override fun onAttachView(view: TimeZoneView) {
        super.onAttachView(view)

        realmDisposable.disposable = configRepo.getCardDetails()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { cardDetails ->
                            realmDisposable.disposable = null

                            val selectedTimezone = if (cardDetails.timezone.isNotEmpty()) {
                                sanitizedTimezoneMap.first {
                                    it.first.equals(cardDetails.timezone)
                                }.first
                            } else {
                                cardDetails.timezone
                            }

                            view.showSelectTimezone(sanitizedTimezoneMap, selectedTimezone)

                        }, { error ->
                            realmDisposable.disposable = null
                            Log.e(TAG, error.message)
                        })

    }

    override fun onTimezoneSelected(timezoneId: String) {
        val zoneIds = sanitizedTimezoneMap.first {
            it.second == timezoneId
        }

        realmDisposable.disposable = configRepo.updateTimezoneDetails(zoneIds.first)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { _ ->
                            realmDisposable.disposable = null
                            Log.d(TAG, "Successfully updated timezone")
                            this.view?.navigateAway()

                        }, { error ->
                            realmDisposable.disposable = null
                            Log.e(TAG, error.message)
                        })
    }

}