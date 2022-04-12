package com.example.vkunsubscribe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
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
        VK.login(this, arrayListOf(VKScope.GROUPS))
        val mainButton : Button = findViewById(R.id.button)
        val changeButton : Button = findViewById(R.id.view_deleted_groups_button)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.also {
            it.adapter = adapter
            it.layoutManager = GridLayoutManager(this, 3)
        }

        viewModel.selectedGroups.observe(this, {
            viewModel.nowSelecting.value = it.size > 0
            adapter.updateSelectedGroups(it)
            mainButton.text = "Отписаться: ${it.size}"
        })

        mainButton.setOnClickListener {
            saveDeletedGroups()
            viewModel.unsubscribeFromSelected()
            viewModel.currentGroups.value?.let { adapter.setData(it) }
        }

        changeButton.setOnClickListener {
            if (viewModel.switch.value == false) {
                getDeletedGroups()
                viewModel.switch.value = true
            } else {
                viewModel.currentGroups.value?.let { adapter.setData(it) }
                viewModel.switch.value = false
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                viewModel.userId.value = token.userId
                getGroups()
            }

            override fun onLoginFailed(authException: VKAuthException) {

            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun getGroups() {
        VK.execute(
            GroupsService().groupsGetExtended(userId = viewModel.userId.value),
            object : VKApiCallback<GroupsGetObjectExtendedResponse> {
                override fun success(result: GroupsGetObjectExtendedResponse) {
                    viewModel.currentGroups.value = result.items.toMutableList()
                    adapter.setData(result.items.toMutableList())
                }
                override fun fail(error: Exception) {

                }
            }
        )
    }

    override fun onGroupSelected(group: GroupsGroupFull) {
        viewModel.selectGroup(group)
        adapter.updateItem(group)
    }

    private fun saveDeletedGroups() {
        val sp = getSharedPreferences("groups", Context.MODE_PRIVATE)
        sp.edit().putStringSet("groups", viewModel.selectedGroups.value?.map {
            it.id.value.toString()
        }?.toMutableSet().also { sp.getStringSet("groups", setOf<String>())?.forEach { s ->
            it?.add(s)
        } }).apply()
    }

    private fun getDeletedGroups() {
        val sp = getSharedPreferences("groups", Context.MODE_PRIVATE)
        val groups = sp.getStringSet("groups", setOf<String>())
        val ids = groups?.let { set ->
            set.map {
                UserId(it.toLong())
            } }?.toList()
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
}