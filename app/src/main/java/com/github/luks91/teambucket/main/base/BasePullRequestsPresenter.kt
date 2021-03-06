/**
 * Copyright (c) 2017-present, Team Bucket Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.github.luks91.teambucket.main.base

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import com.github.luks91.teambucket.connection.ConnectionProvider
import com.github.luks91.teambucket.R
import com.github.luks91.teambucket.TeamMembersProvider
import com.github.luks91.teambucket.di.AppContext
import com.github.luks91.teambucket.model.*
import com.github.luks91.teambucket.persistence.PullRequestsStorage
import com.github.luks91.teambucket.connection.BitbucketApi
import com.github.luks91.teambucket.util.PicassoCircleTransformation
import com.github.luks91.teambucket.getLeadUser
import com.github.luks91.teambucket.persistence.RepositoriesStorage
import com.hannesdorfmann.mosby3.mvp.MvpPresenter
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
import io.reactivex.observables.ConnectableObservable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

open class BasePullRequestsPresenter<T : BasePullRequestsView>
        @Inject constructor(@AppContext private val context: Context,
                            private val connectionProvider: ConnectionProvider,
                            private val repositoriesStorage: RepositoriesStorage,
                            private val pullRequestsStorage: PullRequestsStorage,
                            private val teamMembersProvider: TeamMembersProvider) : MvpPresenter<T> {

    private var disposable = Disposables.empty()

    companion object {
        @JvmStatic val NULL_PULL_REQUESTS = listOf<PullRequest>()
    }

    override fun attachView(view: T) {
        pullRequests(view).apply {
            disposable = CompositeDisposable(
                    subscribeProvidingReviewers(this@apply, view),
                    pullRequestsStorage.pullRequestsPersisting(map { (pullRequests) -> pullRequests }),
                    connect(),
                    subscribeImageLoading(view)
            )
        }
    }

    private fun pullRequests(view: T): ConnectableObservable<Pair<List<PullRequest>, String>> {
        return Observable.combineLatest(
                view.intentPullToRefresh().startWith { Object() },
                connectionProvider.connections(),
                BiFunction<Any, BitbucketConnection, BitbucketConnection> { _, conn -> conn }
        ).switchMap { (_, serverUrl, api, token) ->
            repositoriesStorage.selectedRepositories()
                    .switchMap { list ->
                        Observable.fromIterable(list)
                                .flatMap { (slug, _, project) ->
                                    BitbucketApi.queryPaged { start ->
                                        api.getPullRequests(token, project.key, slug, start, avatarSize()) }
                                            .subscribeOn(Schedulers.io())
                                            .onErrorResumeNext(connectionProvider.handleNetworkError(
                                                    BasePullRequestsPresenter::class.java.simpleName))
                                }.reduce { t1, t2 -> t1 + t2 }.map { it to serverUrl }.toObservable()
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete { view.onLoadingCompleted() }
                                .switchIfEmpty(Observable.just(NULL_PULL_REQUESTS to serverUrl))
                    }
        }
                .doOnSubscribe { view.onSelfLoadingStarted() }
                .publish()
    }

    open protected fun avatarSize() = 96

    private fun subscribeProvidingReviewers(pullRequests: ConnectableObservable<Pair<List<PullRequest>, String>>,
                                            view: T): Disposable {
        return Observable.combineLatest(
                pullRequests,
                teamMembersProvider.teamMembers(view.intentPullToRefresh().startWith { Object() }),
                BiFunction<Pair<List<PullRequest>, String>, Map<User, Density>, ReviewersInformation> {
                    (pullRequests, serverUrl), team ->
                    if (pullRequests != NULL_PULL_REQUESTS)
                        reviewersInformationFrom(team, pullRequests, serverUrl)
                    else
                        reviewersInformationFrom(emptyMap(), pullRequests, serverUrl)
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { prCount -> view.onReviewersReceived(prCount) }
    }

    private fun reviewersInformationFrom(teamMembers: Map<User, Density>, pullRequests: List<PullRequest>, serverUrl: String)
            : ReviewersInformation {

        val usersToPullRequests = teamMembers.keys.associateBy({it}, {0}).toMutableMap()
        val lazyReviewers = mutableSetOf<User>()

        pullRequests.forEach {
            it.reviewers.asSequence()
                    .filterNot { it.approved }
                    .filter { usersToPullRequests.contains(it.user) }
                    .forEach { usersToPullRequests[it.user] = usersToPullRequests[it.user]!! + 1 }

            if (it.isLazilyReviewed()) {
                it.reviewers.filter { it.status == UNAPPROVED }.forEach { lazyReviewers.add(it.user) }
            }
        }

        val returnList = mutableListOf<Reviewer>()
        for ((key, value) in usersToPullRequests) {
            returnList.add(Reviewer(user = key, density = teamMembers[key]!!, reviewsCount = value,
                    isLazy = lazyReviewers.contains(key)))
        }

        val leadUser = teamMembers.getLeadUser(pullRequests)
        return ReviewersInformation(
                returnList.sortedWith(compareBy({ it.user != leadUser }, { it.reviewsCount }, { it.user.displayName })),
                returnList.sortedWith(compareBy({ it.user != leadUser },
                        { Math.pow(1.0 * (it.reviewsCount + 1), 2.5) - it.density.inbound * it.density.outbound }
                    )).take(Math.min(3, returnList.size)),
                leadUser, serverUrl)
    }

    private fun subscribeImageLoading(view: T): Disposable {
        return view.intentLoadAvatarImage()
                .withLatestFrom(
                        connectionProvider.connections().map { it.serverUrl },
                        BiFunction<AvatarLoadRequest, String, ImageLoadRequest> {
                            (user, target), serverUrl ->
                            ImageLoadRequest(serverUrl, user.avatarUrlSuffix, target)
                        })
                .subscribe { (serverUrl, urlPath, target) ->
                    val requestBuilder = Picasso.with(context)
                    val requestCreator = if (URLUtil.isValidUrl(urlPath)) requestBuilder.load(urlPath)
                    else requestBuilder.load(Uri.parse(serverUrl).buildUpon().appendEncodedPath(urlPath).build())

                    requestCreator
                            .transform(PicassoCircleTransformation())
                            .error(R.drawable.ic_sentiment_very_satisfied_black_24dp)
                            .into(target)
                }
    }

    override fun detachView(retainInstance: Boolean) {
        disposable.dispose()
    }

    private data class ImageLoadRequest(val serverUrl: String, val urlPath: String, val target: Target)
}