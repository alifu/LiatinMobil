package com.alifu.liatinmobil.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alifu.liatinmobil.R

/** Big round back button; text arrow avoids pulling in an icon library. */
@Composable
fun BackCircleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.action_back)
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        modifier = modifier.size(48.dp).semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("←", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
