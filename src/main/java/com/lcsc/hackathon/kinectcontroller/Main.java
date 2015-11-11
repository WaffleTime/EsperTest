package com.lcsc.hackathon.kinectcontroller;

public class Main {
    public static void main(String[] args) {
        EsperHandler esper = new EsperHandler();
        esper.addListener("pat1", new LoggingListener());

        String pattern = "select 'asdf' as gestureId from pattern[" +
                String.format("every (Angle(angle < %d) or Angle(angle > %d))", 60, 90) +
                String.format(" -> Angle(angle > %d, angle < %d)", 60, 120) +
                "]";
        esper.setPattern("pat1", pattern);

        for (int i=0; i<180; i++) {
            esper.sendEvent(new Angle(1, 2, 3, i));
        }
    }
}