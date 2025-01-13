package com.bluetoothchat.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bluetoothchat.core.ui.theme.LocalChatAppColorScheme

val CustomTextFieldValueHeight = 32.dp
val CustomTextFieldValueTextStyle = TextStyle(
    fontFamily = FontFamily.SansSerif,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
)

private const val CustomTextFieldValueDefaultMaxLength = 30

@Composable
fun CustomTextField(
    value: String,
    isError: Boolean,
    hint: String,
    modifier: Modifier = Modifier,
    maxLength: Int = CustomTextFieldValueDefaultMaxLength,
    onValueChange: (String) -> Unit = {},
) {
    Box(modifier = modifier) {
        var userName by remember { mutableStateOf(value) }
        CustomTextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(CustomTextFieldValueHeight),
            value = userName,
            isError = isError,
            textStyle = CustomTextFieldValueTextStyle.copy(
                color = LocalChatAppColorScheme.current.onScreenBackground,
            ),
            placeholder = {
                Text(
                    text = hint,
                    style = CustomTextFieldValueTextStyle.copy(
                        color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Normal,
                    ),
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            onValueChange = { newText ->
                if (newText.length <= maxLength) {
                    userName = newText
                    onValueChange(newText)
                }
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = CustomTextFieldColors(
                cursorColor = LocalChatAppColorScheme.current.accent,
                focusedIndicatorColor = LocalChatAppColorScheme.current.accent,
                unfocusedIndicatorColor = LocalChatAppColorScheme.current.onScreenBackground,
            ),
            contentPadding = PaddingValues(vertical = 4.dp),
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterEnd),
            text = "${userName.length} / $maxLength",
            textAlign = TextAlign.End,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = LocalChatAppColorScheme.current.onScreenBackground.copy(alpha = 0.5f),
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.shape,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    colors: CustomTextFieldColors
) {
    BasicTextField(
        value = value,
        modifier = modifier,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.cursorColor),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        decorationBox = @Composable { innerTextField ->
            // places leading icon, text field with label and placeholder, trailing icon
            TextFieldDefaults.DecorationBox(
                value = value,
                visualTransformation = visualTransformation,
                innerTextField = innerTextField,
                placeholder = placeholder,
                label = label,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                prefix = prefix,
                suffix = suffix,
                supportingText = supportingText,
                shape = shape,
                singleLine = singleLine,
                enabled = enabled,
                isError = isError,
                interactionSource = interactionSource,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = colors.focusedIndicatorColor,
                    unfocusedIndicatorColor = colors.unfocusedIndicatorColor,
                ),
                contentPadding = contentPadding,
            )
        }
    )
}

data class CustomTextFieldColors(
    val cursorColor: Color,
    val focusedIndicatorColor: Color = Color.Transparent,
    val unfocusedIndicatorColor: Color = Color.Transparent,
)
