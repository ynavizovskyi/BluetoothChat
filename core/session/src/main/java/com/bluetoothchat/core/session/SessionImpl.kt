package com.bluetoothchat.core.session

import com.bluetoothchat.core.analytics.AnalyticsClient
import com.bluetoothchat.core.analytics.consts.PROPERTY_THEME
import com.bluetoothchat.core.analytics.toAnalyticsValue
import com.bluetoothchat.core.config.RemoteConfig
import com.bluetoothchat.core.db.datasource.MessageDataSource
import com.bluetoothchat.core.db.datasource.UserDataSource
import com.bluetoothchat.core.dispatcher.ApplicationScope
import com.bluetoothchat.core.dispatcher.DispatcherManager
import com.bluetoothchat.core.domain.model.User
import com.bluetoothchat.core.prefs.USER_DEVICE_ADDRESS_NOT_SET
import com.bluetoothchat.core.prefs.session.SessionPrefs
import com.bluetoothchat.core.prefs.settings.AppSettingsPrefs
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionImpl @Inject constructor(
    private val dispatcherManager: DispatcherManager,
    private val applicationScope: ApplicationScope,
    private val userDataSource: UserDataSource,
    private val messageDataSource: MessageDataSource,
    private val analyticsClient: AnalyticsClient,
    private val config: RemoteConfig,
    private val sessionPrefs: SessionPrefs,
    private val settingsPrefs: AppSettingsPrefs,
    private val userColorProvider: SessionUserColorProvider,
) : Session {

    private val userInitFlow = MutableStateFlow(false)
    private val userDeviceAddressFlow = MutableStateFlow(sessionPrefs.getUserDeviceAddress())

    init {
        applicationScope.launch(dispatcherManager.default) {
            initUser()
            initAnalyticsUserId()
            incrementTotalSessionCount()
        }

        observeConfigForAnalytics()
        observePrefsForAnalytics()
    }

    //Dummy function for the session object to be initialized
    override fun init() = Unit

    override suspend fun getAnalyticsUserId() = withContext(dispatcherManager.default) {
        sessionPrefs.getAnalyticsUserId()
    }

    override suspend fun getUser(): User {
        return withContext(dispatcherManager.io) {
            userInitFlow.first { it }
            val savedUser = userDataSource.get(sessionPrefs.getUserDeviceAddress())
            requireNotNull(savedUser)
        }
    }

    override fun observeUser(): Flow<User> {
        return userDeviceAddressFlow
            .flatMapLatest { deviceAddress ->
                userDataSource.observe(deviceAddress).filterNotNull()
            }
            .distinctUntilChanged()
    }

    override suspend fun setUserAddressIfEmpty(address: String) {
        if (sessionPrefs.getUserDeviceAddress() == USER_DEVICE_ADDRESS_NOT_SET) {
            val user = requireNotNull(userDataSource.get(USER_DEVICE_ADDRESS_NOT_SET))
            val updatedUser = user.copy(deviceAddress = address)

            //Updating user and all the entities that have device address as a foreign key
            sessionPrefs.setUserDeviceAddress(address)
            userDataSource.delete(user)
            userDataSource.save(updatedUser)
            messageDataSource.updateUserDeviceAddress(oldAddress = USER_DEVICE_ADDRESS_NOT_SET, newAddress = address)

            userDeviceAddressFlow.value = address
        }
    }

    override suspend fun setDeviceNameIfChanged(deviceName: String?) {
        val user = getUser()
        if (deviceName != null && user.deviceAddress != deviceName) {
            val updatedUser = user.copy(deviceName = deviceName)
            userDataSource.save(updatedUser)
        }
    }

    override suspend fun isCurrentUser(user: User) = isCurrentUser(user.deviceAddress)

    override suspend fun isCurrentUser(deviceAddress: String) = deviceAddress == sessionPrefs.getUserDeviceAddress()

    private suspend fun initUser() {
        val userDeviceAddress = sessionPrefs.getUserDeviceAddress()
        val savedUser = userDataSource.get(userDeviceAddress)
        if (savedUser == null) {
            userDataSource.save(createUser())
        }
        userInitFlow.emit(true)
    }

    private suspend fun initAnalyticsUserId() {
        val userId = sessionPrefs.getAnalyticsUserId()
        if (userId.isEmpty()) {
            val newUserId = UUID.randomUUID().toString()
            sessionPrefs.setAnalyticsUserId(newUserId)
        }

        val updatedUuid = sessionPrefs.getAnalyticsUserId()
        analyticsClient.setUserId(updatedUuid)
        FirebaseCrashlytics.getInstance().setUserId(updatedUuid)
    }

    private fun incrementTotalSessionCount() {
        val updatedSessionCount = sessionPrefs.getTotalSessionCount() + 1
        sessionPrefs.setTotalSessionCount(updatedSessionCount)
    }

    private fun observeConfigForAnalytics() {
        config.observeRawConfig()
            .onEach { properties ->
                val prefixedProperties = properties.mapKeys {
                    "config_${it.key}"
                }
                analyticsClient.setUserProperties(prefixedProperties)
            }
            .flowOn(dispatcherManager.default)
            .launchIn(applicationScope)
    }

    private fun observePrefsForAnalytics() {
        settingsPrefs.observeAppTheme()
            .onEach { theme ->
                analyticsClient.setUserProperty(PROPERTY_THEME, theme.toAnalyticsValue())
            }
            .flowOn(dispatcherManager.default)
            .launchIn(applicationScope)
    }

    private fun createUser() = User(
        deviceAddress = sessionPrefs.getUserDeviceAddress(),
        color = userColorProvider.getArgbColors().random(),
        deviceName = null,
        userName = null,
        picture = null,
    )
}
