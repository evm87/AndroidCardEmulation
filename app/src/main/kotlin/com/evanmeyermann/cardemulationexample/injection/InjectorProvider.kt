package com.evanmeyermann.cardemulationexample.injection

import android.content.Context
import com.evanmeyermann.cardemulationexample.home.AndroidHomePresenter
import com.evanmeyermann.cardemulationexample.presenters.HomePresenter
import com.evanmeyermann.cardemulationexample.presenters.TimeZonePresenter
import com.evanmeyermann.cardemulationexample.storage.AndroidRealm
import com.evanmeyermann.cardemulationexample.Timezone.AndroidTimeZonePresenter
import com.evanmeyermann.cardemulationexample.CardDetails.AndroidUserDetailsPresenter
import com.evanmeyermann.cardemulationexample.presenters.UserDetailsPresenter
import com.evanmeyermann.cardemulationexample.storage.CardDetailsRepository
import com.evanmeyermann.cardemulationexample.storage.RealmCardDetailsRepository
import java.util.concurrent.atomic.AtomicReference

/**
 * Single class used to store reference to concrete implementation of injection provider
 */
object Injector {
    private val providerRef = AtomicReference<InjectorProvider>()

    @JvmStatic
    var provider: InjectorProvider
        get() = providerRef.get() ?: throw IllegalStateException("Injector has not been set")
        set(value) {
            providerRef.getAndSet(value)
        }
}

interface InjectorProvider {

    fun provideRealmCardDetailsRepo() : CardDetailsRepository

    fun provideHomePresenter() : HomePresenter

    fun provideTimezonePresenter() : TimeZonePresenter

    fun provideUserDetailsPresenter() : UserDetailsPresenter
}

class AndroidInjectionProvider(private val context: Context) : InjectorProvider {

    override fun provideRealmCardDetailsRepo(): CardDetailsRepository {
        return RealmCardDetailsRepository(AndroidRealm(context))
    }

    override fun provideHomePresenter(): HomePresenter {
        return AndroidHomePresenter(context, this)
    }

    override fun provideTimezonePresenter(): TimeZonePresenter {
        return AndroidTimeZonePresenter(context, this)
    }

    override fun provideUserDetailsPresenter(): UserDetailsPresenter {
        return AndroidUserDetailsPresenter(context, this)
    }
}