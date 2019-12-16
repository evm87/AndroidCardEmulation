package com.evanmeyermann.cardemulationexample.Timezone

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.evanmeyermann.cardemulationexample.R
import com.evanmeyermann.cardemulationexample.databinding.CellTimezoneBinding
import com.evanmeyermann.cardemulationexample.presenters.TimeZonePresenter
import com.evanmeyermann.cardemulationexample.presenters.TimeZoneView
import net.grandcentrix.thirtyinch.TiActivity
import java.util.concurrent.TimeUnit
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import com.evanmeyermann.cardemulationexample.DisposableWrapper
import com.evanmeyermann.cardemulationexample.ViewContainer
import com.evanmeyermann.cardemulationexample.databinding.ViewActivityTimezoneBinding
import com.evanmeyermann.cardemulationexample.injection.Injector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

private const val TAG = "TimezoneActivity"

class TimeZoneActivity : TiActivity<TimeZonePresenter, TimeZoneView>(), TimeZoneView {

    companion object {

        fun newIntent(context: Context): Intent {
            val intent = Intent(context, TimeZoneActivity::class.java)
            return intent
        }
    }

    private var viewContainer = ViewContainer()

    // Used for cleaning up resources after diff operation on computation scheduler
    private val viewCompositeDisposable = CompositeDisposable()
    private val diffSubscription = DisposableWrapper(viewCompositeDisposable)

    //Create presenter
    override fun providePresenter(): TimeZonePresenter = Injector.provider.provideTimezonePresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewContainer.attach(findViewById(android.R.id.content))

        // Enable back button on action bar
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun showSelectTimezone(timezones: Collection<Triple<String, String, CharSequence>>, selectedTimezone: String) {

        val viewBinding = ViewActivityTimezoneBinding.inflate(LayoutInflater.from(this)).apply {

            // Set up recyclerview
            this.timezoneList.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = TimezoneAdapter({ timezoneString ->
                    presenter.onTimezoneSelected(timezoneString)
                }, timezones, selectedTimezone)
            }

            // Set up the dynamic search function
            val textChange = PublishSubject.create<String>()

            // Set up searchview
            this.searchView.apply {
                // Set up search text observable
                this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        textChange.onNext(newText)
                        return true
                    }
                })
            }

            val disposable = textChange
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .switchMap { newFilter ->
                        // Set up the adapter to change
                        (timezoneList.adapter as TimezoneAdapter).let { adapter ->
                            val fullSet = timezones.toList()

                            // Filter out matches
                            val newData = fullSet.mapNotNull { (rawId, sanitizedId, offset) ->

                                if (sanitizedId.contains(newFilter, true)) {
                                    Triple(rawId, sanitizedId, offset)
                                } else {
                                    null
                                }
                            }.toList()
                            Observable.fromCallable<Pair<DiffUtil.DiffResult, Collection<Triple<String, String, CharSequence>>>> {
                                Pair(DiffUtil.calculateDiff(DiffCallback(adapter.getTimezones(), newData), true), newData)
                            }
                        }
                    }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ (diff, newData) ->
                        diffSubscription.disposable = null

                        (timezoneList.adapter as? TimezoneAdapter)?.apply {
                            setFilteredData(newData)
                            diff.dispatchUpdatesTo(this)
                        }

                        // Hide the searchTitle header if the list of timezones has been filtered
                        if (newData.size != timezones.size) {
                            searchTitle.visibility = View.GONE
                        } else {
                            searchTitle.visibility = View.VISIBLE
                        }

                    }, { error ->
                        diffSubscription.disposable = null
                        Log.e(TAG, "Failed to compute diffs", error)
                    })

            viewCompositeDisposable.add(disposable)
        }

        viewContainer.transitionToScene(viewBinding, TAG)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun navigateAway() {
        finish()
    }

    /**
     * Adapter for timezones recyclerview
     */
    private class TimezoneAdapter(private val timezoneSelectedCallback: (String) -> Unit,
                                  timezones: Collection<Triple<String, String, CharSequence>>,
                                  private val selectedTimezone: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val mutableTimezones = timezones.toMutableList()

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return TimeZoneViewHolder(CellTimezoneBinding.inflate(LayoutInflater.from(parent?.context), parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

            val (_, city, gmt) = mutableTimezones.elementAt(position)

            (holder as? TimeZoneViewHolder)?.apply {
                holder.cityName.text = city
                holder.gmtOffset.text = gmt.toString()

                if(city == selectedTimezone) {
                    cityName.setTextColor(ContextCompat.getColor(cityName.context, R.color.colorPrimary))
                    gmtOffset.setTextColor(ContextCompat.getColor(gmtOffset.context, R.color.colorPrimary))
                    checkedIcon.visibility = View.VISIBLE
                } else {
                    cityName.setTextColor(ContextCompat.getColor(cityName.context, R.color.colorOrientationText))
                    gmtOffset.setTextColor(ContextCompat.getColor(gmtOffset.context, R.color.brown_grey_two))
                    checkedIcon.visibility = View.INVISIBLE
                }

                holder.cellContainer.setOnClickListener {
                    cityName.setTextColor(ContextCompat.getColor(it.context, R.color.colorPrimary))
                    gmtOffset.setTextColor(ContextCompat.getColor(it.context, R.color.colorPrimary))
                    checkedIcon.visibility = View.VISIBLE

                    // Invoke the callback
                    timezoneSelectedCallback(city)
                }
            }
        }

        fun getTimezones() = mutableTimezones.toList()

        override fun getItemCount(): Int {
            return mutableTimezones.size
        }

        /**
         * Updates the data set
         */
        fun setFilteredData(filteredTimezones: Collection<Triple<String, String, CharSequence>>) {
            // Clear the availabilities and add the newly filtered ones
            mutableTimezones.clear()
            mutableTimezones.addAll(filteredTimezones)
        }
    }

    /**
     * Holds the timezone selection data. Exposes values to be populated by result data.
     */
    private class TimeZoneViewHolder(val view: CellTimezoneBinding,
                                     val cityName: TextView = view.cityName,
                                     val gmtOffset: TextView = view.gmtOffset,
                                     val checkedIcon: ImageView = view.checkedIcon,
                                     val cellContainer: ConstraintLayout = view.cellContainer) : RecyclerView.ViewHolder(view.root)

    /**
     * Used to create a [DiffUtil.Callback] to be used with [android.support.v7.widget.RecyclerView]
     * in order to issue list changes notifications.
     */

    private class DiffCallback(private val oldList: List<Triple<String, String, CharSequence>>,
                               private val newList: List<Triple<String, String, CharSequence>>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return newList[newItemPosition]
        }
    }
}