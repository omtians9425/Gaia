package com.yuyakaido.android.blueprint.presentation.presenter;

import android.content.Context;

import com.yuyakaido.android.blueprint.app.App;
import com.yuyakaido.android.blueprint.domain.entity.GithubContributor;
import com.yuyakaido.android.blueprint.domain.usecase.GithubUseCase;
import com.yuyakaido.android.blueprint.presentation.view.GithubView;

import java.util.List;

import javax.inject.Inject;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by yuyakaido on 3/13/16.
 */
public class GithubPresenter {

    @Inject
    Scheduler scheduler;

    private GithubView githubView;
    private GithubUseCase githubUseCase;

    public GithubPresenter(Context context, GithubView githubView, GithubUseCase githubUseCase) {
        App.getAppComponent(context).inject(this);
        this.githubView = githubView;
        this.githubUseCase = githubUseCase;
    }

    public void onCreate() {
        githubView.initViews();
        githubView.showProgressBar();
        githubView.refresh();
    }

    public void refresh() {
        githubUseCase.getGithubContributors()
                .subscribeOn(scheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<GithubContributor>>() {
                    @Override
                    public void call(List<GithubContributor> githubContributors) {
                        githubView.setGithubContributors(githubContributors);
                        githubView.hideProgressBar();
                    }
                });
    }

    public void onItemClick(GithubContributor githubContributor) {
        githubView.startWebViewActivity(githubContributor);
    }

}