package com.evanmeyermann.cardemulationexample.CardDetails

import android.content.Context
import android.util.Log
import com.evanmeyermann.cardemulationexample.DisposableWrapper
import com.evanmeyermann.cardemulationexample.injection.InjectorProvider
import com.evanmeyermann.cardemulationexample.presenters.UserDetailsPresenter
import com.evanmeyermann.cardemulationexample.presenters.UserDetailsView
import io.reactivex.android.schedulers.AndroidSchedulers

private const val TAG = "WifiPresenter"

class AndroidUserDetailsPresenter(private val context: Context, injectorProvider: InjectorProvider) : UserDetailsPresenter() {

    private val configRepo = injectorProvider.provideRealmCardDetailsRepo()

    // Disposables
    private val realmDisposable = DisposableWrapper(viewVisibleDisposables)

    override fun onAttachView(attachedView: UserDetailsView) {
        super.onAttachView(attachedView)

        //Get our current settings info and show on the view which one we have saved previously
        realmDisposable.disposable = configRepo.getCardDetails()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { cardDetails ->
                            realmDisposable.disposable = null
                            attachedView.showUserDetailsActivity(cardDetails.name)
                        },
                        { error ->
                            realmDisposable.disposable = null
                            Log.e(TAG, error.message)
                        })
    }

    override fun onSaveDetailsClicked(name: String, cardNumber: String, hasSecurityCode: Boolean, securityCode: String) {
        realmDisposable.disposable = configRepo.updateUserDetails(name, cardNumber, hasSecurityCode, securityCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { _ ->
                            realmDisposable.disposable = null
                            Log.d(TAG, "Successfully updated card name details")
                        },
                        { error ->
                            realmDisposable.disposable = null
                            Log.e(TAG, error.message)
                        })
    }

    override fun onForgetDetailsClicked() {
        realmDisposable.disposable = configRepo.deleteUserDetails()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { _ ->
                            realmDisposable.disposable = null
                            Log.d(TAG, "Successfully deleted card name details")
                        },
                        { error ->
                            realmDisposable.disposable = null
                            Log.e(TAG, error.message)
                        })
    }
}
