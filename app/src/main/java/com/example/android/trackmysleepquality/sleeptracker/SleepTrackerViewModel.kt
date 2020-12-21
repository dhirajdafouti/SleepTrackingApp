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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val databaseDao: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {
    private val toNight = MutableLiveData<SleepNight?>()

    private val allNights = databaseDao.getAllNight()

    val nightStrings = Transformations.map(allNights) { nights ->
        formatNights(nights, application.resources)
    }

    // property of Mutable Live data
    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

    //Getter Method exposed to the Fragment.
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    fun doneNavigation() {
        _navigateToSleepQuality.value = null
    }

    init {
        initializeToNight()
    }

    private fun initializeToNight() {
        viewModelScope.launch {
            toNight.value = getToNightFromDataBase()
        }
    }

    private suspend fun getToNightFromDataBase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = databaseDao.getToNight()
            if (night?.endTimeMili != night?.startTimeMili) {
                night = null
            }
            night
        }
    }

    fun onStartTracking() {
        viewModelScope.launch {
            val night = SleepNight()
            insert(night)
            toNight.value = getToNightFromDataBase()
        }
    }

    fun onStopTracking() {
        viewModelScope.launch {
            val oldNight = toNight.value ?: return@launch
            oldNight.endTimeMili = System.currentTimeMillis()
            update(oldNight)
            //Once this is set to value of oldNight, the onchange will gets triggered in the fragment and will
            //Navigate to Sleep Quality Screen.
            _navigateToSleepQuality.value = oldNight
        }
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            databaseDao.update(night)
        }
    }

    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            databaseDao.insert(night)
        }

    }

    fun onClearTracking() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                databaseDao.clear()
            }
        }
    }


    override fun onCleared() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                databaseDao.clear()
            }
            toNight.value = null
        }
        super.onCleared()
    }


    companion object {
        private val TAG: String = SleepTrackerViewModel::class.java.simpleName
    }
}

