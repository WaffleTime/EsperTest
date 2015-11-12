package com.lcsc.hackathon.kinectcontroller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This project is just for tetsing esper patterns out and making sure the right patterns are being used!
 */
public class Main {
    private static final Logger _logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        test3();
    }

    /**
     * Goal:
     * Figure out a pattern that will work well for gluing multiple posturerules for a gesture together.
     * We want the gesture being triggered when ALL of the rules in the gesture are being matched at the same time.
     *
     * Test Results:
     *
     * Suppose the following esper pattern:
     * select from pattern[every (not A -> A) and every (not B -> B)]
     *
     * Suppose the following event sequence:
     * ~A A C C ~A A ~A A ~A ~B B
     *
     * This pattern will be triggered 3 time for the above event sequence. That is bad because both A and B need to be
     * matched at the same time and that should have only been triggered once.
     */
    public static void test1() {
        EsperHandler esper = new EsperHandler();
        String pattern = "select 'asdf' as gestureId from pattern[" +
                String.format("every ((Angle(vertex=1, angle < %d) or Angle(vertex=1, angle > %d))",4, 6) +
                String.format(" -> Angle(vertex=1, angle > %d, angle < %d))", 4, 6) +
                String.format(" and every ((Angle(vertex=2, angle < %d) or Angle(vertex=2, angle > %d))", 10, 15) +
                String.format(" -> Angle(vertex=2, angle > %d, angle < %d))", 10, 15) +
                "]";
        esper.setPattern("pat1", pattern);
        esper.addListener("pat1", new LoggingListener());

        _logger.debug("This part will trigger the pattern three times");

        for (int i=0; i<6; i++) {
            esper.sendEvent(new Angle(1, 1, 1, i));
        }

        for (int i=0; i<6; i++) {
            esper.sendEvent(new Angle(1, 1, 1, i));
        }

        for (int i=0; i<6; i++) {
            esper.sendEvent(new Angle(1, 1, 1, i));
        }

        for (int i=8; i<20; i++) {
            esper.sendEvent(new Angle(2, 2, 2, i));
        }
    }

    /**
     * Goal:
     * Figure out a pattern that will work well for gluing multiple posturerules for a gesture together.
     * We want the gesture being triggered when ALL of the rules in the gesture are being matched at the same time.
     *
     * Test Results:
     *
     * Suppose the following esper pattern:
     * select from pattern[every (A -> not ~A) and every (B -> not ~B)]
     *
     * This pattern produces weird results. I can't even explain why it's happening.
     * In the below test, the pattern is triggered 9 times.
     *
     * If the last two for loops are removed, the pattern is only triggered once though.
     *
     */
    public static void test2() {
        EsperHandler esper = new EsperHandler();
        String pattern = "select 'asdf' as gestureId from pattern[" +
                String.format("every (Angle(vertex=1, angle > %d, angle < %d)", 4, 6) +
                String.format(" -> not (Angle(vertex=1, angle < %d) or Angle(vertex=1, angle > %d)))", 4, 6)+
                String.format(" and every (Angle(vertex=2, angle > %d, angle < %d)", 10, 12) +
                String.format(" -> not (Angle(vertex=2, angle < %d) or Angle(vertex=2, angle > %d)))", 6, 8) +
                "]";
        esper.setPattern("pat1", pattern);
        esper.addListener("pat1", new LoggingListener());

        for (int i=0; i<7; i++) {
            esper.sendEvent(new Angle(1, 1, 1, i));
        }

        for (int i=8; i<15; i++) {
            esper.sendEvent(new Angle(2, 2, 2, i));
        }

        for (int i=0; i<7; i++) {
            esper.sendEvent(new Angle(1, 1, 1, i));
        }

        for (int i=8; i<15; i++) {
            esper.sendEvent(new Angle(2, 2, 2, i));
        }
    }

    /**
     * Goal:
     * Figure out a pattern that will work well for gluing multiple posturerules for a gesture together.
     * We want the gesture being triggered when ALL of the rules in the gesture are being matched at the same time.
     *
     * Test Results:
     *
     * Suppose the following esper pattern:
     * select from pattern[every ((~A or ~B) -> (A and B)) where timer:within(1 sec)]
     *
     * Refer to the logs and comments below for the results of this pattern!
     *
     */
    public static void test3()  {
        EsperHandler esper = new EsperHandler();
        String pattern = "select 'asdf' as gestureId from pattern[" +
                String.format("every (((Angle(vertex=1, angle < %d) or Angle(vertex=1, angle > %d))", 4, 10)+
                String.format(" or (Angle(vertex=2, angle < %d) or Angle(vertex=2, angle > %d)))", 170, 181) +
                String.format(" -> (Angle(vertex=1, angle > %d, angle < %d)", 4, 10) +
                String.format(" and Angle(vertex=2, angle > %d, angle < %d))) where timer:within(1 sec)", 170, 181) +
                "]";
        esper.setPattern("pat1", pattern);
        esper.addListener("pat1", new LoggingListener());

        _logger.debug("This part doesn't trigger the pattern at all because of the time window");

        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 7));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        esper.sendEvent(new Angle(2, 2, 2, 0));
        esper.sendEvent(new Angle(2, 2, 2, 175));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _logger.debug("This part will trigger the pattern exactly twice. " +
                "(The previous for loop's events are still in the time window!)");

        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 7));
        esper.sendEvent(new Angle(2, 2, 2, 175));
        esper.sendEvent(new Angle(1, 1, 1, 7));
        esper.sendEvent(new Angle(2, 2, 2, 175));
        esper.sendEvent(new Angle(1, 1, 1, 7));
        esper.sendEvent(new Angle(2, 2, 2, 175));

        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 7));
        esper.sendEvent(new Angle(2, 2, 2, 175));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _logger.debug("This part will trigger the pattern exactly twice. " +
                "(The previous for loop's events are still in the time window!)");

        esper.sendEvent(new Angle(2, 2, 2, 0));
        esper.sendEvent(new Angle(2, 2, 2, 175));
        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 7));

        esper.sendEvent(new Angle(2, 2, 2, 0));
        esper.sendEvent(new Angle(2, 2, 2, 175));
        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 7));

        //This part doesn't trigger the pattern because there isn't a third event with Angle(2, 2, 2, 175)
        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 7));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        _logger.debug("This part will trigger the pattern exactly once");

        //Only one matched event exist for this vertex, so the pattern can only be triggered once!
        esper.sendEvent(new Angle(2, 2, 2, 0));
        esper.sendEvent(new Angle(2, 2, 2, 175));

        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 7));

        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 7));

        esper.sendEvent(new Angle(1, 1, 1, 0));
        esper.sendEvent(new Angle(1, 1, 1, 7));
    }
}