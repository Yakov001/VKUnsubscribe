package com.example.vkunsubscribe

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
            viewModel.unsubscribeFromSelected()
            viewModel.currentGroups.value?.let { adapter.setData(it) }
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
                    /*for (i in result.items) {

                    }*/
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
}