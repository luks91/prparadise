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

package com.github.luks91.teambucket.main.statistics

import com.github.luks91.teambucket.TeamMembersProvider
import com.github.luks91.teambucket.connection.ConnectionProvider
import com.github.luks91.teambucket.main.statistics.load.StatisticsLoadFragment
import com.github.luks91.teambucket.main.statistics.load.StatisticsLoadPresenter
import com.github.luks91.teambucket.main.statistics.load.StatisticsLoadView
import com.github.luks91.teambucket.persistence.RepositoriesStorage
import dagger.Module
import dagger.Provides

@Module
class StatisticsModule {

    @Provides
    internal fun provideStatisticsLoadFragmentView(statisticsLoadFragment: StatisticsLoadFragment): StatisticsLoadView = statisticsLoadFragment

    @Provides
    internal fun providesStatisticsLoadPresenter(connectionProvider: ConnectionProvider, teamMembersProvider: TeamMembersProvider,
                                                 repositoriesStorage: RepositoriesStorage, statisticsStorage: StatisticsStorage) =
            StatisticsLoadPresenter(connectionProvider, teamMembersProvider, repositoriesStorage, statisticsStorage)

    @Provides
    internal fun providesStatisticsStorage() = StatisticsStorage()
}