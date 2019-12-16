package com.evanmeyermann.cardemulationexample.presenters

import io.reactivex.disposables.CompositeDisposable
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.TiView

/**
 * Base class for presenters
 */
abstract class PresenterBase<V : TiView> : TiPresenter<V>() {

    //Used to clean up pending subscriptions/disposables
    protected val disposables = CompositeDisposable()

    //Used to cleanup subscriptions when view becomes inactive
    protected val viewVisibleDisposables = CompositeDisposable()

    override fun onDetachView() {
        viewVisibleDisposables.clear()
        super.onDetachView()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }
}