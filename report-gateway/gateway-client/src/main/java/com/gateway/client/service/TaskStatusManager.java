package com.gateway.client.service;

import org.springframework.stereotype.Component;

/**
 * 任务状态管理器 - 管理当前是否有用户在执行任务
 * 使用简单的内存存储，在任务开始时设置为true，任务完成或失败时设置为false
 */
@Component
public class TaskStatusManager {

    /**
     * 是否有任务正在执行
     */
    private volatile boolean isTaskRunning = false;

    /**
     * 当前执行的任务区域
     */
    private volatile String currentTaskRegion = null;

    /**
     * 任务开始时间（毫秒）
     */
    private volatile long taskStartTime = 0;

    /**
     * 标记任务开始执行
     * @param region 大区信息
     * @return 如果成功标记，返回true；如果已有任务在执行，返回false
     */
    public synchronized boolean startTask(String region) {
        if (isTaskRunning) {
            return false;
        }
        isTaskRunning = true;
        currentTaskRegion = region;
        taskStartTime = System.currentTimeMillis();
        return true;
    }

    /**
     * 标记任务完成
     */
    public synchronized void completeTask() {
        isTaskRunning = false;
        currentTaskRegion = null;
        taskStartTime = 0;
    }

    /**
     * 获取是否有任务正在执行
     * @return true 表示有任务在执行，false 表示无任务
     */
    public boolean isTaskRunning() {
        return isTaskRunning;
    }

    /**
     * 获取当前执行的任务大区
     * @return 大区信息，如果无任务返回null
     */
    public String getCurrentTaskRegion() {
        return currentTaskRegion;
    }

    /**
     * 获取任务已执行的时间（秒）
     * @return 任务执行时间，如果无任务返回0
     */
    public long getTaskElapsedSeconds() {
        if (!isTaskRunning || taskStartTime == 0) {
            return 0;
        }
        return (System.currentTimeMillis() - taskStartTime) / 1000;
    }
}
