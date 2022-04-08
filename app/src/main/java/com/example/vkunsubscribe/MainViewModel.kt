package com.example.vkunsubscribe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.groups.dto.GroupsGetObjectExtendedResponse
import com.vk.sdk.api.groups.dto.GroupsGroupFull

class MainViewModel : ViewModel() {

    val selectedGroups: MutableLiveData<MutableList<GroupsGroupFull>> by lazy {
        MutableLiveData(mutableListOf())
    }
    var nowSelecting: MutableLiveData<Boolean> = MutableLiveData(false)
    var userId: MutableLiveData<UserId> = MutableLiveData()

    var groupsGetResponse: MutableLiveData<GroupsGetObjectExtendedResponse> = MutableLiveData()

    fun selectGroup(group: GroupsGroupFull) {
        if (selectedGroups.value?.contains(group) == true) {
            selectedGroups.value!!.remove(group)
            selectedGroups.value = selectedGroups.value
        } else {
            selectedGroups.value?.add(group)
            selectedGroups.value = selectedGroups.value
        }
    }

    fun unsubscribeFromSelected() {

    }
}