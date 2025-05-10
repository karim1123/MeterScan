package com.gabbasov.meterscan.settings.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gabbasov.meterscan.model.navigator.NavigatorType
import com.gabbasov.meterscan.settings.R

@Composable
fun NavigatorSelector(
    selectedNavigator: NavigatorType,
    onNavigatorSelected: (NavigatorType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
    ) {
        Text(
            text = stringResource(R.string.navigator_selection),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        NavigatorType.entries.forEach { navigatorType ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = selectedNavigator == navigatorType,
                        onClick = { onNavigatorSelected(navigatorType) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedNavigator == navigatorType,
                    onClick = null // Click is handled by the selectable modifier
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = when (navigatorType) {
                        NavigatorType.GOOGLE_MAPS -> stringResource(R.string.google_maps)
                        NavigatorType.YANDEX_MAPS -> stringResource(R.string.yandex_maps)
                        NavigatorType.SYSTEM_DEFAULT -> stringResource(R.string.system_default)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun NavigatorSelectorPreview() {
    var selectedNavigator = NavigatorType.GOOGLE_MAPS
    NavigatorSelector(
        selectedNavigator = selectedNavigator,
        onNavigatorSelected = { selectedNavigator = it }
    )
}
