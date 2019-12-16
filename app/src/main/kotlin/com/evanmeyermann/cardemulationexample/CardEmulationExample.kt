package com.evanmeyermann.cardemulationexample

import android.app.Application
import android.util.Log
import com.evanmeyermann.cardemulationexample.injection.AndroidInjectionProvider
import com.evanmeyermann.cardemulationexample.injection.Injector
import com.evanmeyermann.cardemulationexample.storage.AndroidRealm
import com.evanmeyermann.cardemulationexample.storage.AndroidRealmSchedulers
import com.evanmeyermann.cardemulationexample.storage.model.Model
import com.evanmeyermann.cardemulationexample.storage.RealmCardDetailsRepository

class CardEmulationExample : Application()
{
    override fun onCreate() {
        super.onCreate()

        //Set up injection provider
        Injector.provider = AndroidInjectionProvider(this)

        val androidRealm = AndroidRealm(this, AndroidRealmSchedulers.realmThread)

        //Create a default row if we do not have one.
        val configRepo = RealmCardDetailsRepository(androidRealm)
        configRepo.getCardDetails()
                .subscribe(
                    { _ ->
                        Log.d("Realm", "Successfully Retrieved new row!")
                    },
                    { error ->
                        configRepo.addOrUpdateCardDetails(Model.CardDetails())
                                .subscribe(
                                    { _ ->
                                        Log.d("Realm", "Successfully added new row!")
                                    },
                                    { error ->
                                        Log.e("Realm", error.message)
                                    })
                        Log.e("Realm", error.message)
                    })
    }
}