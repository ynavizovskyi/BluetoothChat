package com.bluetoothchat.core.ui.model

import android.os.Parcelable
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import kotlinx.parcelize.Parcelize

sealed interface UiText : Parcelable {
    @Composable
    fun asString(): String

    @Composable
    fun asAnnotatedString(): AnnotatedString


    @Parcelize
    class Resource(@StringRes val resId: Int, val params: List<UiTextParam> = emptyList()) : UiText {
        @Composable
        override fun asString() = if (params.isEmpty()) {
            stringResource(id = resId)
        } else {
            stringResource(id = resId, formatArgs = params.map { it.text }.toTypedArray())
        }

        @Composable
        override fun asAnnotatedString() = if (params.isEmpty()) {
            val string = stringResource(id = resId)
            buildAnnotatedString { append(string) }
        } else {
            val rawString = stringResource(id = resId)
            buildAnnotatedString {
                rawString.split("%s").forEachIndexed { index, s ->
                    append(s)
                    params.getOrNull(index)?.let { params ->
                        when (params.style) {
                            UiTextParamStyle.REGULAR -> append(params.text)
                            UiTextParamStyle.BOLD -> withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(params.text)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Parcelize
data class UiTextParam(val text: String, val style: UiTextParamStyle = UiTextParamStyle.REGULAR) : Parcelable

enum class UiTextParamStyle { REGULAR, BOLD }
