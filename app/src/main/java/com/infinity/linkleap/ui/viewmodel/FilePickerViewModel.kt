package com.infinity.linkleap.ui.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.infinity.linkleap.data.model.PickedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FilePickerViewModel @Inject constructor(): ViewModel() {
    private val _pickedItems = MutableStateFlow<List<PickedItem>>(emptyList())
    val pickedItems = _pickedItems.asStateFlow()

    fun addItem(item: PickedItem) {
        _pickedItems.value = _pickedItems.value + item
    }

    fun clearAll() {
        _pickedItems.value = emptyList()
    }

    fun getInstalledApps(context: Context): List<PickedItem> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return apps.filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }.map {
            PickedItem(
                name = pm.getApplicationLabel(it).toString(),
                uri = null,
                path = it.sourceDir,
                type = "app"
            )
        }
    }
}