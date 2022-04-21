package com.example.vkunsubscribe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.VkResult
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKAuthException
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.GroupsGetObjectExtendedResponse
import com.vk.sdk.api.groups.dto.GroupsGroupFull

class MainActivity : AppCompatActivity(), GroupsAdapter.OnGroupSelectedListener {

    private val adapter by lazy { GroupsAdapter(listener = this) }
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vkLogin()

        val mainButton: Button = findViewById(R.id.button)
        val changeButton: Button = findViewById(R.id.view_deleted_groups_button)

        findViewById<RecyclerView>(R.id.recycler_view).also {
            it.adapter = adapter
            it.layoutManager = GridLayoutManager(this, 3)

            if (viewModel.observingDeleted.value == false) {
                viewModel.currentGroups.value?.let { data -> adapter.setData(data) }
            } else {
                viewModel.deletedGroups.value?.let { data -> adapter.setData(data) }
            }
            viewModel.selectedGroups.value?.let { data -> adapter.updateSelectedGroups(data)}
        }

        viewModel.selectedGroups.observe(this, {
            viewModel.nowSelecting.value = it.size > 0
            adapter.updateSelectedGroups(it)
            mainButton.text = "Отписаться: ${it.size}"
        })

        mainButton.setOnClickListener {
            if (viewModel.observingDeleted.value == false) {
                saveDeletedGroups()
                viewModel.unsubscribeFromSelected()
                viewModel.currentGroups.value?.let { adapter.setData(it) }
            } else {
                resubscribeToDeleted()
            }
        }

        changeButton.setOnClickListener {
            viewModel.selectedGroups.value = mutableListOf()
            if (viewModel.observingDeleted.value == false) {
                getDeletedGroups()
                viewModel.observingDeleted.value = true
            } else {
                viewModel.currentGroups.value?.let { adapter.setData(it) }
                viewModel.observingDeleted.value = false
            }
        }

    }

    override fun onGroupSelected(group: GroupsGroupFull) {
        viewModel.selectGroup(group)
        adapter.updateItem(group)
    }

    fun getGroups() {
        VK.execute(
            GroupsService().groupsGetExtended(userId = viewModel.userId.value),
            object : VKApiCallback<GroupsGetObjectExtendedResponse> {
                override fun success(result: GroupsGetObjectExtendedResponse) {
                    viewModel.currentGroups.value = result.items.toMutableList()
                    adapter.setData(result.items.toMutableList())
                }
                override fun fail(error: Exception) {}
            }
        )
    }

    private fun saveDeletedGroups() {
        val sp = getSharedPreferences("groups", Context.MODE_PRIVATE)
        sp.edit().putStringSet("groups", viewModel.selectedGroups.value?.map {
            it.id.value.toString()
        }?.toMutableSet().also {
            sp.getStringSet("groups", setOf<String>())?.forEach { s ->
                it?.add(s)
            }
        }).apply()
    }

    private fun getDeletedGroups() {
        val sp = getSharedPreferences("groups", Context.MODE_PRIVATE)
        val groups = sp.getStringSet("groups", setOf<String>())
        val ids = groups?.let { set ->
            set.map {
                UserId(it.toLong())
            }
        }?.toList()
        VK.execute(
            GroupsService().groupsGetById(ids),
            object : VKApiCallback<List<GroupsGroupFull>> {
                override fun success(result: List<GroupsGroupFull>) {
                    result.toMutableList().also {
                        viewModel.deletedGroups.value = it
                        adapter.setData(it)
                    }
                }
                override fun fail(error: Exception) {}
            }
        )
    }

    private fun resubscribeToDeleted() {

        // 1) delete selected groups from SP
        // 2) add selected groups to currentGroups
        viewModel.selectedGroups.value?.let { selGroups ->
            // 1)
            val sp = getSharedPreferences("groups", Context.MODE_PRIVATE)
            val groups = mutableSetOf(*sp.getStringSet("groups", setOf<String>())!!.toTypedArray())
            val groupsToResubscribe = selGroups.map { it.id.toString() }.toSet()
            groups.removeAll(groupsToResubscribe)
            sp.edit().putStringSet("groups", groups).apply()

            // 2)
            viewModel.currentGroups.value!!.addAll(selGroups)

            viewModel.resubscribeToSelected()
            viewModel.currentGroups.value?.let { adapter.setData(it) }
            viewModel.observingDeleted.value = false
        }
    }

    private fun vkLogin() {
        if (viewModel.userId.value == null) {
            VK.login(this, object: ActivityResultCallback<VKAuthenticationResult> {
                override fun onActivityResult(result: VKAuthenticationResult?) {
                    when (result) {
                        is VKAuthenticationResult.Success -> {
                            viewModel.userId.value = result.token.userId
                            getGroups()
                        }
                        is VKAuthenticationResult.Failed -> { }
                    }
                }
            }).launch(arrayListOf(VKScope.GROUPS))
        }
    }
}