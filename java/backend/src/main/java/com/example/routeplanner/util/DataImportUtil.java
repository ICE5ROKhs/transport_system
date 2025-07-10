package com.example.routeplanner.util;

import com.example.routeplanner.entity.TrafficFlow;
import com.example.routeplanner.mapper.TrafficMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ���ݵ��빤����
 * ����CSV��ʽ�Ľ�ͨ���ݵ���
 */
@Component
public class DataImportUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataImportUtil.class);

    @Autowired
    private TrafficMapper trafficMapper;

    /**
     * ����CSV��ʽ�Ľ�ͨ����
     * ���ݸ�ʽ: locationId,timeSlot,flowVolume
     *
     * @param csvFilePath CSV�ļ�·��
     * @return ����ɹ��ļ�¼��
     */
    public int importTrafficDataFromCSV(String csvFilePath) {
        logger.info("��ʼ���뽻ͨ�����ļ�: {}", csvFilePath);

        List<TrafficFlow> trafficFlows = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int lineNumber = 0;

            // ���������У�����У�
            String firstLine = br.readLine();
            if (firstLine != null && !isDataLine(firstLine)) {
                logger.info("����������: {}", firstLine);
                lineNumber++;
            } else if (firstLine != null) {
                // ��һ�о�������
                TrafficFlow flow = parseCSVLine(firstLine, lineNumber + 1);
                if (flow != null) {
                    trafficFlows.add(flow);
                    successCount++;
                } else {
                    errorCount++;
                }
            }

            // ��ȡ������
            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue; // ��������
                }

                TrafficFlow flow = parseCSVLine(line, lineNumber);
                if (flow != null) {
                    trafficFlows.add(flow);
                    successCount++;

                    // �������루ÿ1000����¼����һ�Σ�
                    if (trafficFlows.size() >= 1000) {
                        batchInsertTrafficFlows(trafficFlows);
                        trafficFlows.clear();
                    }
                } else {
                    errorCount++;
                }
            }

            // ����ʣ������
            if (!trafficFlows.isEmpty()) {
                batchInsertTrafficFlows(trafficFlows);
            }

            logger.info("���ݵ������ - �ɹ�: {} ��, ʧ��: {} ��", successCount, errorCount);

        } catch (IOException e) {
            logger.error("��ȡCSV�ļ�ʧ��: {}", e.getMessage(), e);
            throw new RuntimeException("CSV�ļ�����ʧ��", e);
        }

        return successCount;
    }

    /**
     * ����CSV������
     */
    private TrafficFlow parseCSVLine(String line, int lineNumber) {
        try {
            String[] parts = line.split(",");

            if (parts.length < 3) {
                logger.warn("��{}�����ݸ�ʽ����ȷ����Ҫ����3��: {}", lineNumber, line);
                return null;
            }

            // ��������
            Integer locationId = Integer.parseInt(parts[0].trim());
            Integer timeSlot = Integer.parseInt(parts[1].trim());
            Integer flowVolume = Integer.parseInt(parts[2].trim());

            // ������֤
            if (!validateTrafficData(locationId, timeSlot, flowVolume, lineNumber)) {
                return null;
            }

            // ����TrafficFlow����
            TrafficFlow flow = new TrafficFlow(locationId, timeSlot, flowVolume);

            // ����е�4�У�����λ������
            if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                flow.setLocationType(parts[3].trim().toUpperCase());
            }

            return flow;

        } catch (NumberFormatException e) {
            logger.warn("��{}�����ݽ���ʧ�ܣ����ָ�ʽ����: {}", lineNumber, line);
            return null;
        } catch (Exception e) {
            logger.warn("��{}�����ݴ���ʧ��: {}, ����: {}", lineNumber, line, e.getMessage());
            return null;
        }
    }

    /**
     * ��֤��ͨ����
     */
    private boolean validateTrafficData(Integer locationId, Integer timeSlot, Integer flowVolume, int lineNumber) {
        // ��֤λ��ID
        if (locationId == null || locationId <= 0) {
            logger.warn("��{}��: λ��ID��Ч ({})", lineNumber, locationId);
            return false;
        }

        // ��֤ʱ���
        if (timeSlot == null || timeSlot < 1 || timeSlot > 24) {
            logger.warn("��{}��: ʱ�����Ч ({}), Ӧ��1-24֮��", lineNumber, timeSlot);
            return false;
        }

        // ��֤������
        if (flowVolume == null || flowVolume < 0) {
            logger.warn("��{}��: ��������Ч ({}), ����Ϊ����", lineNumber, flowVolume);
            return false;
        }

        // �����������Լ��
        if (flowVolume > 1000) {
            logger.warn("��{}��: �������쳣�� ({}), ��������", lineNumber, flowVolume);
        }

        return true;
    }

    /**
     * �ж��Ƿ�Ϊ�����У����Ǳ����У�
     */
    private boolean isDataLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 3) return false;

        try {
            Integer.parseInt(parts[0].trim());
            Integer.parseInt(parts[1].trim());
            Integer.parseInt(parts[2].trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * �������뽻ͨ��������
     */
    private void batchInsertTrafficFlows(List<TrafficFlow> trafficFlows) {
        try {
            trafficMapper.batchInsertTrafficFlow(trafficFlows);
            logger.debug("�������� {} ����ͨ����", trafficFlows.size());
        } catch (Exception e) {
            logger.error("�������뽻ͨ����ʧ��", e);
            throw new RuntimeException("���ݿ����ʧ��", e);
        }
    }

    /**
     * ���ɲ������ݲ�����
     */
    public int generateAndImportTestData(int locationCount, int timeSlotCount) {
        logger.info("���ɲ�������: {} ��λ��, {} ��ʱ���", locationCount, timeSlotCount);

        List<TrafficFlow> testData = new ArrayList<>();

        for (int locationId = 1; locationId <= locationCount; locationId++) {
            for (int timeSlot = 1; timeSlot <= timeSlotCount; timeSlot++) {
                // ����ģ�⳵��������
                Integer flowVolume = generateMockFlowVolume(locationId, timeSlot);
                TrafficFlow flow = new TrafficFlow(locationId, timeSlot, flowVolume);
                testData.add(flow);
            }
        }

        // ���������������
        int batchSize = 1000;
        int totalInserted = 0;

        for (int i = 0; i < testData.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, testData.size());
            List<TrafficFlow> batch = testData.subList(i, endIndex);
            batchInsertTrafficFlows(batch);
            totalInserted += batch.size();
        }

        logger.info("��������������ɣ������� {} ����¼", totalInserted);
        return totalInserted;
    }

    /**
     * ����ģ�⳵��������
     */
    private Integer generateMockFlowVolume(int locationId, int timeSlot) {
        // ����λ�ú�ʱ������ɺ���ĳ���������
        int baseFlow = 20 + (locationId % 10) * 5; // �������� 20-65

        // ʱ���Ӱ������
        double timeFactor = 1.0;
        if (timeSlot >= 7 && timeSlot <= 9) {        // ��߷�
            timeFactor = 2.0;
        } else if (timeSlot >= 17 && timeSlot <= 19) { // ��߷�
            timeFactor = 1.8;
        } else if (timeSlot >= 12 && timeSlot <= 14) { // ��߷�
            timeFactor = 1.3;
        } else if (timeSlot >= 22 || timeSlot <= 6) {  // ҹ��
            timeFactor = 0.3;
        }

        // ����������
        double randomFactor = 0.7 + Math.random() * 0.6; // 0.7-1.3

        int finalFlow = (int) (baseFlow * timeFactor * randomFactor);
        return Math.max(0, Math.min(200, finalFlow)); // ������0-200֮��
    }

    /**
     * ������н�ͨ����
     */
    public int clearAllTrafficData() {
        logger.warn("������н�ͨ����");
        return trafficMapper.clearAllTrafficData();
    }

    /**
     * ��ȡ����ͳ����Ϣ
     */
    public void printDataStatistics() {
        Long totalCount = trafficMapper.getTotalRecordCount();
        List<Integer> timeSlots = trafficMapper.getAllTimeSlots();
        List<Integer> locations = trafficMapper.getAllLocationIds();

        logger.info("=== ��ͨ����ͳ�� ===");
        logger.info("�ܼ�¼��: {}", totalCount);
        logger.info("λ������: {}", locations.size());
        logger.info("ʱ�������: {}", timeSlots.size());
        logger.info("λ��ID��Χ: {} - {}",
                locations.isEmpty() ? "N/A" : locations.get(0),
                locations.isEmpty() ? "N/A" : locations.get(locations.size() - 1));
        logger.info("ʱ��η�Χ: {} - {}",
                timeSlots.isEmpty() ? "N/A" : timeSlots.get(0),
                timeSlots.isEmpty() ? "N/A" : timeSlots.get(timeSlots.size() - 1));
    }
}
