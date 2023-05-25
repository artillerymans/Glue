package com.artillery.glue.scant

import no.nordicsemi.android.support.v18.scanner.ScanSettings

/**
 * @author : zhiweizhu
 * create on: 2023/5/25 下午2:29
 */

/**
 * @param
 * scanMode 扫描模式
 * @see ScanSettings.SCAN_MODE_LOW_LATENCY 使用最高占空比进行扫描。建议仅在应用程序在前台运行时使用此模式
 * @see ScanSettings.SCAN_MODE_LOW_POWER  在低功耗模式下执行蓝牙 LE 扫描。这是默认扫描模式，
 * 因为它消耗的电量最少。 扫描仪将扫描 0.5 秒，休息 4.5 秒。蓝牙 LE 设备应播发 经常（至少每 100 毫秒一次）
 * 以便使用此模式找到，否则扫描间隔可能会错过部分甚至全部 广告活动。如果扫描应用程序不在前台，则可以强制实施此模式。
 * @see ScanSettings.SCAN_MODE_BALANCED 在平衡电源模式下执行蓝牙 LE 扫描。扫描结果的返回速率提供
 * 扫描频率和功耗之间的良好权衡。扫描仪将扫描 2 秒，随后 空闲 3 秒。
 * @see ScanSettings.SCAN_MODE_OPPORTUNISTIC 一种特殊的蓝牙 LE 扫描模式。
 * 使用此扫描模式的应用程序将被动侦听其他扫描结果 无需启动 BLE 扫描本身.
 * @param legacy 扫描的蓝牙广播数据是否是旧
 * @param reportDelayMillis 按照这个频率回调 ScanCallback.onBatchScanResults(List),
 * 但是如果这个值是小于5000的时候，回调的间隔时间是不固定的
 */
fun createScanSettings(
    legacy: Boolean = false,
    scanMode: Int = ScanSettings.SCAN_MODE_LOW_LATENCY,
    reportDelayMillis: Long = 0L,
    use: Boolean = true,
): ScanSettings {
    return ScanSettings.Builder()
        .setLegacy(legacy)
        .setScanMode(scanMode)
        .setReportDelay(reportDelayMillis)
        .setUseHardwareBatchingIfSupported(use)
        .build()
}