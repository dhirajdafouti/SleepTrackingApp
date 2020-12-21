/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SleepQualityViewModel(val sleepNightId: Long = 0L, val sleepDatabaseDao: SleepDatabaseDao)
    : ViewModel() {

    private val _navigateToSleepTracker = MutableLiveData<SleepNight>()
    val navigateToSleepNight: LiveData<SleepNight>
        get() = _navigateToSleepTracker

    fun doneNavigation() {
        _navigateToSleepTracker.value = null
    }

    fun setSleepQuality(quality: Int) {
        viewModelScope.launch {
           withContext(Dispatchers.IO){
               val tonight=sleepDatabaseDao.get(sleepNightId)?:return@withContext
               tonight.sleepQuality= quality
               sleepDatabaseDao.update(tonight)
           }
        }
    }



}