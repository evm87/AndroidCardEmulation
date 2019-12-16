package com.evanmeyermann.cardemulationexample

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Wrapper for managing disposable is less verbose manner. Use provided constructor to specify
 * [CompositeDisposable] to be used for managing internal disposable
 */
class DisposableWrapper(private val composite: CompositeDisposable) {

    //Returns true if the disposable is still pending
    val isNotDisposed: Boolean
        get() {
            val tmpDisposable = disposable
            return (tmpDisposable !== null && !tmpDisposable.isDisposed)
        }

    //Returns true if disposable is not used
    val isDisposed: Boolean
        get() {
            val tmpDisposable = disposable
            return (tmpDisposable === null || tmpDisposable.isDisposed)
        }

    var disposable: Disposable? = null
        set(value) {
            if (value == null) {
                val oldValue = field
                field = null
                if (oldValue !== null) {
                    if (!oldValue.isDisposed)
                        oldValue.dispose()

                    //Remove disposable from the queue
                    composite.remove(oldValue)
                }
            } else {
                //Add disposable to the queue in the composite disposables
                composite.add(value)
                field = value
            }
        }
}


/**
 * Extract a timezones GMT offset
 */
fun TimeZone.formatTimezoneOffset() : CharSequence {
    val now = Date()

    val hours = TimeUnit.MILLISECONDS.toHours(this.getOffset(now.time).toLong())
    val minutes = Math.abs(TimeUnit.MILLISECONDS.toMinutes(this.getOffset(now.time).toLong()) - TimeUnit.HOURS.toMinutes(hours))

    // For removal of minutes if there isn't a minutes offset
    val stringMinutes = if (minutes.compareTo(0) == 0) { "" } else { ":" + minutes.toString() }

    return if (hours >= 0) {
        String.format("GMT +%d%s", hours, stringMinutes)
    } else {
        String.format("GMT %d%s", hours, stringMinutes)
    }
}
