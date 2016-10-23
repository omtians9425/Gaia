package com.yuyakaido.android.blueprint.infra.repository;

import com.yuyakaido.android.blueprint.domain.entity.GithubContributor;
import com.yuyakaido.android.blueprint.infra.InfraTest;
import com.yuyakaido.android.blueprint.infra.client.GithubClient;
import com.yuyakaido.android.blueprint.infra.dao.GithubDao;
import com.yuyakaido.android.blueprint.infra.module.GithubTestModule;
import com.yuyakaido.android.blueprint.util.ResponseUtil;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.mockwebserver.MockWebServer;
import rx.observers.TestSubscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by yuyakaido on 2/22/16.
 */
public class GithubRepositoryTest extends InfraTest {

    private GithubDao githubDao;

    @Override
    public void setUp() {
        super.setUp();
        githubDao = new GithubDao(ormaDatabase);
    }

    @Test
    public void getGithubContributorsTest() throws Exception {
        final int COUNT = 10;
        List<GithubContributor> expectGithubContributors = new ArrayList<>(COUNT);
        for (int i = 0; i < COUNT; i++) {
            GithubContributor githubContributor = new GithubContributor();
            githubContributor.login = String.valueOf(i);
            githubContributor.avatarUrl = String.valueOf(i);
            githubContributor.htmlUrl = String.valueOf(i);
            expectGithubContributors.add(githubContributor);
        }
        ormaDatabase.prepareInsertIntoGithubContributor()
                .executeAll(expectGithubContributors);

        File file = new File("src/test/assets/json/github_contributors.json");
        MockWebServer mockWebServer = new MockWebServer();
        mockWebServer.enqueue(ResponseUtil.createMockResponse(file));
        mockWebServer.start();

        GithubTestModule githubTestModule = new GithubTestModule();
        GithubClient githubClient = new GithubClient(
                githubTestModule.provideGithubService(mockWebServer));
        GithubRepositoryImpl githubRepository = new GithubRepositoryImpl(githubClient, githubDao);

        TestSubscriber<List<GithubContributor>> testSubscriber = new TestSubscriber<>();
        githubRepository.getGithubContributors().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();
        List<GithubContributor> daoGithubContributors = testSubscriber.getOnNextEvents().get(0);
        List<GithubContributor> clientGithubContributors = testSubscriber.getOnNextEvents().get(1);
        assertThat(daoGithubContributors.size(), is(expectGithubContributors.size()));
        assertThat(clientGithubContributors.size(), is(1));
    }

}