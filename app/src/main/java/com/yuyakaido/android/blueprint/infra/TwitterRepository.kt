package com.yuyakaido.android.blueprint.infra

import com.yuyakaido.android.blueprint.domain.Tweet
import io.reactivex.Observable
import javax.inject.Inject

class TwitterRepository @Inject constructor(
        private val client: TwitterClient) {

    private var cache: List<Tweet>? = null

    fun getUserTimeline(): Observable<List<Tweet>> {
        return if (cache == null) {
            client.getUserTimeline()
                    .doOnNext { cache = it }
        } else {
            Observable.just(cache)
        }
    }

}