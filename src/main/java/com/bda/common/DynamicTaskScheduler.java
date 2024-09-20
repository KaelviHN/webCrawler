package com.bda.common;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/**
 * @author: anran.ma
 * @created: 2024/9/14
 * @description:
 **/
@Component
public class DynamicTaskScheduler {

    private final TaskScheduler taskScheduler;


    public DynamicTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        this.taskScheduler = scheduler;
    }

    public void scheduleTask(String cronExpression, Runnable task) {
        CronTrigger cronTrigger = new CronTrigger(cronExpression);
        taskScheduler.schedule(task, cronTrigger);
    }


}