package com.lcsc.hackathon.kinectcontroller;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingListener implements UpdateListener {
    private static final Logger _logger = LoggerFactory.getLogger(LoggingListener.class);

    public LoggingListener() {}

    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        for (EventBean event : newEvents) {
            String gestureId = (String)event.get("gestureId");
            _logger.debug("GestureId Triggered: "+gestureId);
        }
    }
}