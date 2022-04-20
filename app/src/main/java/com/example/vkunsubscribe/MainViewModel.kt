package com.example.vkunsubscribe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.base.dto.BaseOkResponse
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.GroupsGroupFull

class MainViewModel : ViewModel() {

    val selectedGroups: MutableLiveData<MutableList<GroupsGroupFull>> by lazy {
        MutableLiveData(mutableListOf())
    }
    val currentGroups: MutableLiveData<MutableList<GroupsGroupFull>> by lazy {
        MutableLiveData(mutableListOf())
    }
    val deletedGroups: MutableLiveData<MutableList<GroupsGroupFull>> by lazy {
        MutableLiveData(mutableListOf())
    }
    val observingDeleted: MutableLiveData<Boolean> = MutableLiveData(false)
    var nowSelecting: MutableLiveData<Boolean> = MutableLiveData(false)
    var userId: MutableLiveData<UserId> = MutableLiveData()

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
        selectedGroups.value?.let {
            for (group in it) {
                VK.execute(
                    GroupsService().groupsLeave(group.id)
                )
                currentGroups.value?.remove(group)
            }
        }
        selectedGroups.value = mutableListOf()
        nowSelecting.value = false
    }

    fun resubscribeToSelected() {
        selectedGroups.value?.let {
            for (group in it) {
                VK.execute(
                    GroupsService().groupsJoin(group.id)
                )
            }
        }
        selectedGroups.value = mutableListOf()
        nowSelecting.value = false
    }
}