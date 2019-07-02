package com.dawnwin.stick.service;

public interface StickService {
    /**
     * GPS定位
     * @param imei
     */
    boolean startLocation(String imei);

    /**
     * 测量血压心率
     * @param imei
     */
    boolean startMeasure(String imei);

    /**
     * 设置紧急号码
     * @param imei
     * @param phoneNumberList
     */
    boolean setSosList(String imei, String phoneNumberList);

    /**
     * 开启远程监听
     * @param imei
     */
    boolean startMonitor(String imei, String mobile);

    /**
     * 远程关机
     * @param imei
     */
    boolean shutdownStick(String imei);

    /**
     * 远程重置
     * @param imei
     */
    boolean resetStick(String imei);

    /**
     * 打开GPS定位
     * @param imei
     */
    boolean openGPS(String imei);

    /**
     * 关闭GPS
     * @param imei
     */
    boolean closeGPS(String imei);

    /**
     * 设置定位间隔
     * @param imei
     * @param interval 定位时间间隔（单位为10s)
     */
    boolean setInterval(String imei, int interval);

    /**
     * 设置wifi定位间隔
     * @param imei
     * @param interval 定位时间间隔（单位为1分钟)
     */
    boolean setWifiInterval(String imei, int interval);
}
