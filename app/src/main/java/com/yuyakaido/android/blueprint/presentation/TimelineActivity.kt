package com.yuyakaido.android.blueprint.presentation

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.yuyakaido.android.blueprint.R
import com.yuyakaido.android.blueprint.app.Blueprint
import com.yuyakaido.android.blueprint.databinding.ActivityTimelineBinding
import com.yuyakaido.android.blueprint.domain.Account
import com.yuyakaido.android.blueprint.domain.LoggedInAccount
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TimelineActivity : AppCompatActivity() {

    private val twitterAuthClient = TwitterAuthClient()
    private val section = Section()
    private val disposables = CompositeDisposable()

    private val binding by lazy { ActivityTimelineBinding.inflate(layoutInflater) }

    @Inject
    lateinit var running: LoggedInAccount

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as Blueprint).component.inject(this)
        setContentView(binding.root)

        setupToolbar()
        setupSpinner()
        setupRecyclerView()
    }

    override fun onDestroy() {
        disposables.dispose()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        twitterAuthClient.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_account -> authorize()
            R.id.menu_account_list -> startAccountListActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        title = "Timeline"
    }

    private fun setupSpinner() {
        val adapter = AccountSpinnerAdapter(this)
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                running.switchTo(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        running.accounts()
                .subscribe { adapter.replace(it) }
                .addTo(disposables)
        running.current()
                .subscribe {
                    it.value?.let {
                        binding.spinner.setSelection(adapter.indexOf(it))
                    }
                }
                .addTo(disposables)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = GroupAdapter<ViewHolder>()
                .apply { add(section) }

        running.current()
                .subscribe {
                    if (it.value == null) {
                        section.update(mutableListOf())
                    } else {
                        it.value.repository.getUserTimeline()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe { section.update(it.map { TweetItem(it) }) }
                                .addTo(it.value.disposables)
                    }
                }
                .addTo(disposables)
    }

    private fun authorize() {
        twitterAuthClient.authorize(this, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                running.add(Account(result.data))
            }
            override fun failure(exception: TwitterException) {}
        })
    }

    private fun startAccountListActivity() {
        startActivity(AccountListActivity.createIntent(this))
    }

}