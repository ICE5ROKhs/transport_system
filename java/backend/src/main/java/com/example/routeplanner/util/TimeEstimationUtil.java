package com.example.routeplanner.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 时间估算工具类
 * 处理时间段转换和时间相关计算
 */
@Component
public class TimeEstimationUtil {

    /**
     * 获取当前时间段 (1-24)
     */
    public Integer getCurrentTimeSlot() {
        LocalTime now = LocalTime.now();
        return now.getHour() + 1; // 1-24时间段
    }

    /**
     * 获取当前时间段 (1-12)
     */
    public Integer getCurrentTimeSlot12() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        return (hour / 2) + 1; // 每2小时一个时间段
    }

    /**
     * 将24小时制转换为12时间段制
     */
    public Integer convertTo12TimeSlot(Integer hour24) {
        if (hour24 < 1 || hour24 > 24) {
            throw new IllegalArgumentException("小时必须在1-24之间");
        }
        return ((hour24 - 1) / 2) + 1;
    }

    /**
     * 验证时间段是否有效
     */
    public boolean isValidTimeSlot(Integer timeSlot, Integer maxSlots) {
        return timeSlot != null && timeSlot >= 1 && timeSlot <= maxSlots;
    }

    /**
     * 根据车流量估算通行时间
     */
    public Double estimateTimeWithTraffic(Double baseTime, Integer trafficVolume) {
        if (baseTime == null || trafficVolume == null) {
            return baseTime != null ? baseTime : 1.0;
        }

        // 根据车流量计算拥堵系数
        double congestionFactor = calculateCongestionFactor(trafficVolume);
        return baseTime * congestionFactor;
    }

    /**
     * 计算拥堵系数
     */
    private double calculateCongestionFactor(Integer trafficVolume) {
        if (trafficVolume <= 10) return 1.0;      // 畅通
        if (trafficVolume <= 30) return 1.2;     // 轻微拥堵
        if (trafficVolume <= 60) return 1.5;     // 中度拥堵
        if (trafficVolume <= 100) return 2.0;    // 重度拥堵
        return 3.0;                               // 严重拥堵
    }

    /**
     * 格式化时间显示
     */
    public String formatTime(Double timeInMinutes) {
        if (timeInMinutes == null || timeInMinutes < 0) {
            return "未知";
        }

        int hours = (int) (timeInMinutes / 60);
        int minutes = (int) (timeInMinutes % 60);

        if (hours > 0) {
            return String.format("%d小时%d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }

    /**
     * 获取指定时间段的描述
     */
    public String getTimeSlotDescription(Integer timeSlot) {
        if (timeSlot == null || timeSlot < 1 || timeSlot > 24) {
            return "未知时间段";
        }

        int hour = timeSlot - 1;
        return String.format("%02d:00-%02d:59", hour, hour);
    }
}
