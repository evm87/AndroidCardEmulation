package com.evanmeyermann.cardemulationexample.storage

import android.util.Log
import com.evanmeyermann.cardemulationexample.storage.model.DBCardConfig
import com.evanmeyermann.cardemulationexample.storage.model.Model
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.Callable

interface CardDetailsRepository {
    fun addOrUpdateCardDetails(config: Model.CardDetails): Observable<Model.CardDetails>
    fun getCardDetails(): Observable<Model.CardDetails>
    fun updateUserDetails(name: String, cardNumber: String, hasSecurityCode: Boolean, securityCode: String): Observable<Model.CardDetails>
    fun deleteUserDetails() : Observable<Model.CardDetails>
    fun updateTimezoneDetails(timezone: String): Observable<Model.CardDetails>
}

class RealmCardDetailsRepository(private val androidRealm: AndroidRealm,
                               private val scheduler: Scheduler = AndroidRealmSchedulers.realmThread) : CardDetailsRepository {

    //Add or Update the card details
    override fun addOrUpdateCardDetails(config: Model.CardDetails): Observable<Model.CardDetails> =
            androidRealm.get().map {
                it.use { realm ->

                    //Get the first result so we can begin to update it
                    var dbCardConfig = realm.where(DBCardConfig::class.java).findFirst()

                    RealmHelper.executeTransaction(realm, Callable<Unit> {

                        //If we don't have a config row, add one
                        if (dbCardConfig == null) {
                            dbCardConfig = DBCardConfig()
                            dbCardConfig.id = config.id
                        }

                        //Update our fields
                        dbCardConfig.name = config.name
                        dbCardConfig.cardNumber = config.cardNumber
                        dbCardConfig.timezone = config.timezone
                        dbCardConfig.hasSecurityCode = config.hasSecurityCode
                        dbCardConfig.securityCode = config.securityCode

                        realm.copyToRealmOrUpdate(dbCardConfig)
                    })

                    makeCardDetailsModel(dbCardConfig)

                }
            }.subscribeOn(scheduler)

    //Our default returned model
    override fun getCardDetails(): Observable<Model.CardDetails> =
            androidRealm.get().map {
                it.use { realm ->

                    //Always find first for now
                    val dbCardConfig = realm.where(DBCardConfig::class.java).findFirst()

                    if (dbCardConfig != null) {
                        Log.d("Realm", "Found row, returning it!")
                        makeCardDetailsModel(dbCardConfig)
                    } else {
                        throw RepositoryException("Row could not be retrieved. You need to add a row.")
                    }
                }
            }.subscribeOn(AndroidRealmSchedulers.realmThread)

    override fun updateUserDetails(name: String, cardNumber: String, hasSecurityCode: Boolean, securityCode: String): Observable<Model.CardDetails> =
            androidRealm.get().map {
                it.use { realm ->

                    //Always find first for now
                    val dbCardConfig = realm.where(DBCardConfig::class.java).findFirst()

                    if (dbCardConfig != null) {
                        Log.d("Realm", "Updating card info")

                        RealmHelper.executeTransaction(realm, Callable<Unit> {
                            dbCardConfig.name = name
                            dbCardConfig.cardNumber = cardNumber
                            dbCardConfig.hasSecurityCode = hasSecurityCode
                            dbCardConfig.securityCode = securityCode

                            realm.copyToRealmOrUpdate(dbCardConfig)
                        })
                        makeCardDetailsModel(dbCardConfig)

                    } else {
                        throw RepositoryException("Row could not be retrieved. You need to add a row.")
                    }
                }
            }.subscribeOn(AndroidRealmSchedulers.realmThread)

    override fun deleteUserDetails(): Observable<Model.CardDetails> =
        androidRealm.get().map {
            it.use { realm ->

                //Always find first for now
                val dbCardConfig = realm.where(DBCardConfig::class.java).findFirst()

                if (dbCardConfig != null) {
                    Log.d("Realm", "Updating card info")

                    RealmHelper.executeTransaction(realm, Callable<Unit> {
                        dbCardConfig.name = ""
                        dbCardConfig.cardNumber = ""
                        dbCardConfig.hasSecurityCode = false
                        dbCardConfig.securityCode = ""

                        realm.copyToRealmOrUpdate(dbCardConfig)
                    })
                    makeCardDetailsModel(dbCardConfig)

                } else {
                    throw RepositoryException("Row could not be retrieved. You need to add a row.")
                }
            }
        }.subscribeOn(AndroidRealmSchedulers.realmThread)

    override fun updateTimezoneDetails(timezone: String): Observable<Model.CardDetails> =
            androidRealm.get().map {
                it.use { realm ->

                    //Always find first for now
                    val dbCardConfig = realm.where(DBCardConfig::class.java).findFirst()

                    if (dbCardConfig != null) {
                        Log.d("Realm", "Updating timezone info")

                        RealmHelper.executeTransaction(realm, Callable<Unit> {
                            dbCardConfig.timezone = timezone

                            realm.copyToRealmOrUpdate(dbCardConfig)
                        })
                        makeCardDetailsModel(dbCardConfig)

                    } else {
                        throw RepositoryException("Row could not be retrieved. You need to add a row.")
                    }
                }
            }.subscribeOn(AndroidRealmSchedulers.realmThread)

    //Helper methods
    private fun makeCardDetailsModel(dbCardConfig: DBCardConfig): Model.CardDetails {
        return Model.CardDetails(dbCardConfig.id,
                dbCardConfig.timezone,
                dbCardConfig.name,
                dbCardConfig.cardNumber,
                dbCardConfig.hasSecurityCode,
                dbCardConfig.securityCode
        )
    }
}