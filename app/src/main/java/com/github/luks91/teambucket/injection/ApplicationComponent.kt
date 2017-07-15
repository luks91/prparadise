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

package com.github.luks91.teambucket.injection

import android.content.Context
import com.github.luks91.teambucket.persistence.PersistenceProvider
import com.github.luks91.teambucket.ConnectionProvider
import com.github.luks91.teambucket.TeamMembersProvider
import com.github.luks91.teambucket.util.ReactiveBus
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun persistenceProvider(): PersistenceProvider

    fun connectionProvider(): ConnectionProvider

    fun teamMembersProvider(): TeamMembersProvider

    @AppContext
    fun applicationContext(): Context

    fun reactiveBus(): ReactiveBus
}