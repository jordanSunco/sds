package com.dawnwin.app.stick.service;

import com.dawnwin.app.stick.model.StickCommand;

public interface StickService {

    String processStickCommand(String modelName, String uniqueKey, StickCommand command);

    void sendCommand(String imsi, String cmd);
}
