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

package com.github.luks91.teambucket.main.home

import android.content.Context
import android.net.ConnectivityManager
import com.github.luks91.teambucket.ReactiveBus
import com.github.luks91.teambucket.TeamMembersProvider
import com.github.luks91.teambucket.connection.ConnectionProvider
import com.github.luks91.teambucket.di.AppContext
import com.github.luks91.teambucket.persistence.PersistenceProvider
import dagger.Module
import dagger.Provides

@Module
class HomeModule {

    @Provides
    internal fun provideHomeFragmentView(homeFragment: HomeFragment): HomeView {
        return homeFragment
    }

    @Provides
    internal fun provideHomePresenter(@AppContext context: Context, connectionProvider: ConnectionProvider,
                                            persistenceProvider: PersistenceProvider, teamMembersProvider: TeamMembersProvider,
                                            eventBus: ReactiveBus, connectivityManager: ConnectivityManager): HomePresenter {
        return HomePresenter(context, connectionProvider, persistenceProvider, teamMembersProvider, eventBus,
                connectivityManager)
    }
}