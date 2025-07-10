package com.example.routeplanner.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * ʱ����㹤����
 * ����ʱ���ת����ʱ����ؼ���
 */
@Component
public class TimeEstimationUtil {

    /**
     * ��ȡ��ǰʱ��� (1-24)
     */
    public Integer getCurrentTimeSlot() {
        LocalTime now = LocalTime.now();
        return now.getHour() + 1; // 1-24ʱ���
    }

    /**
     * ��ȡ��ǰʱ��� (1-12)
     */
    public Integer getCurrentTimeSlot12() {
        LocalTime now = LocalTime.now();
        int hour = now.getHour();
        return (hour / 2) + 1; // ÿ2Сʱһ��ʱ���
    }

    /**
     * ��24Сʱ��ת��Ϊ12ʱ�����
     */
    public Integer convertTo12TimeSlot(Integer hour24) {
        if (hour24 < 1 || hour24 > 24) {
            throw new IllegalArgumentException("Сʱ������1-24֮��");
        }
        return ((hour24 - 1) / 2) + 1;
    }

    /**
     * ��֤ʱ����Ƿ���Ч
     */
    public boolean isValidTimeSlot(Integer timeSlot, Integer maxSlots) {
        return timeSlot != null && timeSlot >= 1 && timeSlot <= maxSlots;
    }

    /**
     * ���ݳ���������ͨ��ʱ��
     */
    public Double estimateTimeWithTraffic(Double baseTime, Integer trafficVolume) {
        if (baseTime == null || trafficVolume == null) {
            return baseTime != null ? baseTime : 1.0;
        }

        // ���ݳ���������ӵ��ϵ��
        double congestionFactor = calculateCongestionFactor(trafficVolume);
        return baseTime * congestionFactor;
    }

    /**
     * ����ӵ��ϵ��
     */
    private double calculateCongestionFactor(Integer trafficVolume) {
        if (trafficVolume <= 10) return 1.0;      // ��ͨ
        if (trafficVolume <= 30) return 1.2;     // ��΢ӵ��
        if (trafficVolume <= 60) return 1.5;     // �ж�ӵ��
        if (trafficVolume <= 100) return 2.0;    // �ض�ӵ��
        return 3.0;                               // ����ӵ��
    }

    /**
     * ��ʽ��ʱ����ʾ
     */
    public String formatTime(Double timeInMinutes) {
        if (timeInMinutes == null || timeInMinutes < 0) {
            return "δ֪";
        }

        int hours = (int) (timeInMinutes / 60);
        int minutes = (int) (timeInMinutes % 60);

        if (hours > 0) {
            return String.format("%dСʱ%d����", hours, minutes);
        } else {
            return String.format("%d����", minutes);
        }
    }

    /**
     * ��ȡָ��ʱ��ε�����
     */
    public String getTimeSlotDescription(Integer timeSlot) {
        if (timeSlot == null || timeSlot < 1 || timeSlot > 24) {
            return "δ֪ʱ���";
        }

        int hour = timeSlot - 1;
        return String.format("%02d:00-%02d:59", hour, hour);
    }
}
