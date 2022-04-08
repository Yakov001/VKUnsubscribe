package com.example.vkunsubscribe

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.GridLayout
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKApiExecutionException
import com.vk.api.sdk.exceptions.VKAuthException
import com.vk.api.sdk.internal.ApiCommand
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.friends.FriendsService
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.GroupsGetObjectExtendedResponse
import com.vk.sdk.api.groups.dto.GroupsGetResponse

class MainActivity : AppCompatActivity() {

    private val adapter by lazy { GroupsAdapter() }
    val viewModel: MainViewModel by viewModels()
    lateinit var userId : UserId
    lateinit var groupsGetResponse: GroupsGetObjectExtendedResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        VK.login(this, arrayListOf(VKScope.WALL, VKScope.PHOTOS))

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.also {
            it.adapter = adapter
            it.layoutManager = GridLayoutManager(this, 3)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                userId = token.userId

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
            GroupsService().groupsGetExtended(userId = userId),
            object : VKApiCallback<GroupsGetObjectExtendedResponse> {
                override fun success(result: GroupsGetObjectExtendedResponse) {
                    groupsGetResponse = result
                    /*for (i in result.items) {

                    }*/
                    adapter.setData(result)
                }
                override fun fail(error: Exception) {

                }
            }
        )
    }
}