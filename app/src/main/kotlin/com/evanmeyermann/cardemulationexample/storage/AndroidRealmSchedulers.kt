package com.evanmeyermann.cardemulationexample.storage

import android.content.Context
import android.os.HandlerThread
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.concurrent.atomic.AtomicReference

private const val CURRENT_SCHEMA_VERSION = 1L

/**
 * Used for initialising realm DB and accessing it
 */

private val realmInitialisation = AtomicReference<Observable<Boolean>>()
private const val TAG = "AndroidRealm"

interface RealmProvider {
    fun get(): Observable<Realm>
}

class AndroidRealm(context: Context,
                   scheduler: Scheduler = AndroidRealmSchedulers.realmThread) : RealmProvider {

    init {
        if (realmInitialisation.get() == null) {
            val initialisation = Observable.fromCallable {

                Realm.init(context)

                val config = RealmConfiguration.Builder()
                        .name("cardemulation.realm")
                        .schemaVersion(CURRENT_SCHEMA_VERSION)
                        .build()

                Realm.setDefaultConfiguration(config)

                realmInitialisation.set(Observable.just(true))

                true
            }.subscribeOn(scheduler).share()

            realmInitialisation.compareAndSet(null, initialisation)
        }
    }

    override fun get(): Observable<Realm> = realmInitialisation.get().map { Realm.getDefaultInstance() }
}

/**
 * Realm scheduler used to access realm data using rx2.
 */
object AndroidRealmSchedulers {
    /**
     * Realm thread to be used for all realm related operations
     */
    val realmThread: Scheduler by lazy {
        val handlerThread = HandlerThread("Realm handler thread")
        handlerThread.start()
        AndroidSchedulers.from(handlerThread.looper)
    }
}