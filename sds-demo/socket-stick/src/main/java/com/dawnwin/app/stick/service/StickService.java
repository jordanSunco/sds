package com.dawnwin.app.stick.service;

import com.dawnwin.app.stick.model.StickCommand;

public interface StickService {
    /**
     * 处理来自设备的指令
     * @param command
     * @return
     */
    String processStickCommand(String modelName, String uniqueKey, StickCommand command);

    /**
     * 发送指令到设备
     * @param cmd
     * @return
     */
    void sendCommand(String imsi, String cmd);
}
