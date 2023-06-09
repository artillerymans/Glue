/*
package com.artillery.connect


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ValueChangedCallback
import java.util.*

class GattService : Service() {

    private val defaultScope = CoroutineScope(Dispatchers.Default)

    private lateinit var bluetoothObserver: BroadcastReceiver

    private var myCharacteristicChangedChannel: SendChannel<String>? = null

    private val clientManagers = mutableMapOf<String, ClientManager>()

    override fun onCreate() {
        super.onCreate()

        // Setup as a foreground service

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             val notificationChannel = NotificationChannel(
                GattService::class.java.simpleName,
                resources.getString(R.string.gatt_service_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationService =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationService.createNotificationChannel(notificationChannel)
        } else {
             val notification = NotificationCompat.Builder(this, GattService::class.java.simpleName)
                 .setSmallIcon(R.mipmap.ic_launcher)
                 .setContentTitle(resources.getString(R.string.gatt_service_name))
                 .setContentText(resources.getString(R.string.gatt_service_running_notification))
                 .setAutoCancel(true)

             startForeground(1, notification.build())
        }

        bluetoothObserver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val bluetoothState = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_STATE,
                            -1
                        )
                        when (bluetoothState) {
                            BluetoothAdapter.STATE_ON -> enableBleServices()
                            BluetoothAdapter.STATE_OFF -> disableBleServices()
                        }
                    }
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        Log.d(TAG, "Bond state changed for device ${device?.address}: ${device?.bondState}")
                        when (device?.bondState) {
                            BluetoothDevice.BOND_BONDED -> addDevice(device)
                            BluetoothDevice.BOND_NONE -> removeDevice(device)
                        }
                    }

                }
            }
        }
        registerReceiver(bluetoothObserver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        registerReceiver(bluetoothObserver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))

        // Startup BLE if we have it

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter?.isEnabled == true) enableBleServices()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothObserver)
        disableBleServices()
    }

    override fun onBind(intent: Intent?): IBinder? =
        when (intent?.action) {
            DATA_PLANE_ACTION -> {
                DataPlane()
            }
            else -> null
        }

    override fun onUnbind(intent: Intent?): Boolean =
        when (intent?.action) {
            DATA_PLANE_ACTION -> {
                myCharacteristicChangedChannel = null
                true
            }
            else -> false
        }

    */
/**
     * A binding to be used to interact with data of the service
     *//*

    inner class DataPlane : Binder() {
        fun setMyCharacteristicChangedChannel(sendChannel: SendChannel<String>) {
            myCharacteristicChangedChannel = sendChannel
        }
    }

    private fun enableBleServices() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter?.isEnabled == true) {
            Log.i(TAG, "Enabling BLE services")
            bluetoothManager.adapter.bondedDevices.forEach { device -> addDevice(device) }
        } else {
            Log.w(TAG, "Cannot enable BLE services as either there is no Bluetooth adapter or it is disabled")
        }
    }

    private fun disableBleServices() {
        clientManagers.values.forEach { clientManager ->
            clientManager.close()
        }
        clientManagers.clear()
    }

    private fun addDevice(device: BluetoothDevice) {
        if (!clientManagers.containsKey(device.address)) {
            val clientManager = ClientManager()
            clientManager.connect(device).useAutoConnect(true).enqueue()
            clientManagers[device.address] = clientManager
        }
    }

    private fun removeDevice(device: BluetoothDevice) {
        clientManagers.remove(device.address)?.close()
    }

    */
/*
     * Manages the entire GATT service, declaring the services and characteristics on offer
     *//*

    companion object {
        */
/**
         * A binding action to return a binding that can be used in relation to the service's data
         *//*

        const val DATA_PLANE_ACTION = "data-plane"

        private const val TAG = "gatt-service"
    }

    private inner class ClientManager : BleManager(this@GattService) {
        override fun getGattCallback(): BleManagerGattCallback = GattCallback()

        override fun log(priority: Int, message: String) {
            if (BuildConfig.DEBUG || priority == Log.ERROR) {
                Log.println(priority, TAG, message)
            }
        }

        private inner class GattCallback : BleManagerGattCallback() {

            private var myCharacteristic: BluetoothGattCharacteristic? = null

            override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                val service = gatt.getService(MyServiceProfile.MY_SERVICE_UUID)
                myCharacteristic =
                    service?.getCharacteristic(MyServiceProfile.MY_CHARACTERISTIC_UUID)
                val myCharacteristicProperties = myCharacteristic?.properties ?: 0
                return (myCharacteristicProperties and BluetoothGattCharacteristic.PROPERTY_READ != 0) &&
                        (myCharacteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
            }

            override fun initialize() {

                requestMtu(238)
                    .enqueue()

                setNotificationCallback(myCharacteristic).with { _, data ->
                    if (data.value != null) {
                        val value = String(data.value!!, Charsets.UTF_8)
                        defaultScope.launch {
                            myCharacteristicChangedChannel?.send(value)
                        }
                    }
                }

                beginAtomicRequestQueue()
                    .add(enableNotifications(myCharacteristic)
                        .fail { _: BluetoothDevice?, status: Int ->
                            log(Log.ERROR, "Could not subscribe: $status")
                            disconnect().enqueue()
                        }
                    )
                    .done {
                        log(Log.INFO, "Target initialized")
                    }
                    .enqueue()
            }

            override fun onServicesInvalidated() {
                myCharacteristic = null
            }
        }
    }

    object MyServiceProfile {
        val MY_SERVICE_UUID: UUID = UUID.fromString("80323644-3537-4F0B-A53B-CF494ECEAAB3")
        val MY_CHARACTERISTIC_UUID: UUID = UUID.fromString("80323644-3537-4F0B-A53B-CF494ECEAAB3")
    }
}*/
