package com.andb.apps.weather.ui.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.andb.apps.weather.ui.theme.onBackgroundTertiary

@Composable
fun SearchBar(
    term: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onTermChange: (String) -> Unit,
) {
    BasicTextField(
        value = term,
        onValueChange = onTermChange,
        modifier = modifier,
        textStyle = MaterialTheme.typography.body1,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                Box(contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onBackgroundTertiary
                    )
                    innerTextField()
                }
            }
        }
    )
}