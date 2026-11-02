package com.alifu.liatinmobil.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alifu.liatinmobil.R

@Composable
fun BackCircleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CircleTextButton(
        label = "←",
        contentDescription = stringResource(R.string.action_back),
        onClick = onClick,
        modifier = modifier,
    )
}
