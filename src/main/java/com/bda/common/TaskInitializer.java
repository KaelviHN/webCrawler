package com.bda.common;

import com.bda.stock.WebCrawlerUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author: anran.ma
 * @created: 2024/9/14
 * @description:
 **/
@Component
@Getter
public class TaskInitializer {
    private static final Log log = LogFactory.getLog(TaskInitializer.class);
    @Autowired
    private DynamicTaskScheduler dynamicTaskScheduler;

    private Config config;

    private String jarPath ;

    private final ReentrantLock lock = new ReentrantLock();

    @SneakyThrows
    @PostConstruct
    public void initialize() {
        // 从外部配置文件加载 config
        this.jarPath = FileUtil.getJarPath();
        this.config = FileUtil.getConfig(jarPath);
        log.info("读取到配置文件" + config);
        init();
        dynamicTaskScheduler.scheduleTask(config.getCorn(), this::executeTask);
    }


    /**
     * 分钟
     */
    public void executeTask(){
        String jarPath = FileUtil.getJarPath();
        WebCrawlerUtils.minute(jarPath);
        SFTPUtil.uploadFile(jarPath, getConfig(),false,true);
        commitSecondData();
    }


    /**
     * 其他
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void fixedTask(){
        String jarPath = FileUtil.getJarPath();
        WebCrawlerUtils.day(jarPath);
        WebCrawlerUtils.fiveDay(jarPath);
        WebCrawlerUtils.week(jarPath);
        WebCrawlerUtils.month(jarPath);
        WebCrawlerUtils.quarter(jarPath);
        WebCrawlerUtils.year(jarPath);
        SFTPUtil.uploadFile(jarPath, getConfig(),false,true);
    }

    /**
     * 初始化
     */
    public void init(){
        String jarPath = FileUtil.getJarPath();
        WebCrawlerUtils.minute(jarPath);
        WebCrawlerUtils.day(jarPath);
        WebCrawlerUtils.week(jarPath);
        WebCrawlerUtils.month(jarPath);
        WebCrawlerUtils.quarter(jarPath);
        WebCrawlerUtils.year(jarPath);
        WebCrawlerUtils.fiveDay(jarPath);
        SFTPUtil.uploadFile(jarPath, getConfig(),false,true);
    }

    /**
     * 实时
     */
    @Scheduled(cron = "*/5 * * * * *")
    public void realTask(){
        String jarPath = FileUtil.getJarPath();
        WebCrawlerUtils.realTask(jarPath);
    }


    /**
     * 上传文件
     */
    public void commitSecondData(){
        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    SFTPUtil.uploadFile(jarPath, getConfig(), true, false);
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("任务被锁定，上传文件操作被跳过");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
        }
    }

    /**
     * 刷新文件名
     */
    @Scheduled(fixedRate = 3600000)
    public void refreshFileName(){
        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    WebCrawlerUtils.hkRealTime = "";
                    WebCrawlerUtils.macRealTime = "";
                    WebCrawlerUtils.hsiRealTime = "";
                    SFTPUtil.uploadFile(jarPath, getConfig(),true,true);
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("任务被锁定，上传文件操作被跳过");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
        }
    }
}