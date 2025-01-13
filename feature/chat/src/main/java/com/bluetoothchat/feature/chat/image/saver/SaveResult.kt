package com.bluetoothchat.feature.chat.image.saver

sealed interface SaveImageResult {

    object Success : SaveImageResult

    sealed interface Error : SaveImageResult {
        object NoWriteStoragePermission : Error
        object SaveError : Error
    }

}
