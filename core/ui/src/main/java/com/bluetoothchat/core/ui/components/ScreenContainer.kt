package com.bluetoothchat.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme

@Composable
fun ScreenContainer(
    topAppBar: @Composable () -> Unit = {},
    fab: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .background(LocalChatAppColorScheme.current.toolbar)
            .statusBarsPadding()
            .background(LocalChatAppColorScheme.current.navigationBar)
            .navigationBarsPadding()
            .imePadding()
            .fillMaxSize(),
        topBar = topAppBar,
        floatingActionButton = fab,
        containerColor = LocalChatAppColorScheme.current.screenBackground,
    ) {
        it.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
        ) {
            content()
        }
    }
}
