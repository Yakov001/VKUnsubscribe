package com.example.vkunsubscribe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vk.sdk.api.groups.dto.GroupsGroupFull

class MainViewModel : ViewModel() {

    var selectedGroups: MutableLiveData<MutableList<GroupsGroupFull>> = MutableLiveData(mutableListOf())
    var nowSelecting: MutableLiveData<Boolean> = MutableLiveData(false)

    fun selectGroup(group: GroupsGroupFull) {
        if (selectedGroups.value?.contains(group) == true) {
            selectedGroups.value!!.remove(group)
        } else {
            selectedGroups.value?.add(group)
        }
    }
}