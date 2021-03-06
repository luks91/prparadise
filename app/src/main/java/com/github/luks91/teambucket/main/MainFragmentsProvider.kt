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

package com.github.luks91.teambucket.main

import android.support.v4.app.Fragment
import com.github.luks91.teambucket.main.home.HomeComponent
import com.github.luks91.teambucket.main.home.HomeFragment
import com.github.luks91.teambucket.main.reviewers.ReviewersFragment
import com.github.luks91.teambucket.main.reviewers.ReviewersComponent
import com.github.luks91.teambucket.main.statistics.StatisticsComponent
import com.github.luks91.teambucket.main.statistics.load.StatisticsLoadFragment
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap
import dagger.Binds
import dagger.Module
import dagger.android.support.FragmentKey

@Module
abstract class MainFragmentsProvider {

    @Binds
    @IntoMap
    @FragmentKey(HomeFragment::class)
    internal abstract fun provideHomeFragmentFactory(builder: HomeComponent.Builder):
            AndroidInjector.Factory<out Fragment>

    @Binds
    @IntoMap
    @FragmentKey(ReviewersFragment::class)
    internal abstract fun provideReviewersFragmentFactory(builder: ReviewersComponent.Builder):
            AndroidInjector.Factory<out Fragment>

    @Binds
    @IntoMap
    @FragmentKey(StatisticsLoadFragment::class)
    internal abstract fun provideStatisticsFragmentFactory(builder: StatisticsComponent.Builder):
            AndroidInjector.Factory<out Fragment>
}