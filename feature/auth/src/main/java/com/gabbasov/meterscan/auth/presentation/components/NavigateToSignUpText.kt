package com.gabbasov.meterscan.auth.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun NavigateToSignUpText(
    modifier: Modifier = Modifier,
    onNavigateToSignUp: () -> Unit
) {
    Text(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onNavigateToSignUp()
            },
        text = buildAnnotatedString {
            append("Do not have an account?")
            append("  ")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("Sign up")
            }
        },
        textAlign = TextAlign.Start,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Preview(showBackground = true)
@Composable
private fun NavigateToSignUpTextPreview() {
    NavigateToSignUpText(onNavigateToSignUp = {})
}