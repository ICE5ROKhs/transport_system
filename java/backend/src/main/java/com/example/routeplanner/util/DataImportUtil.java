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
 * 数据导入工具类
 * 处理CSV格式的交通数据导入
 */
@Component
public class DataImportUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataImportUtil.class);

    @Autowired
    private TrafficMapper trafficMapper;

    /**
     * 导入CSV格式的交通数据
     * 数据格式: locationId,timeSlot,flowVolume
     *
     * @param csvFilePath CSV文件路径
     * @return 导入成功的记录数
     */
    public int importTrafficDataFromCSV(String csvFilePath) {
        logger.info("开始导入交通数据文件: {}", csvFilePath);

        List<TrafficFlow> trafficFlows = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            int lineNumber = 0;

            // 跳过标题行（如果有）
            String firstLine = br.readLine();
            if (firstLine != null && !isDataLine(firstLine)) {
                logger.info("跳过标题行: {}", firstLine);
                lineNumber++;
            } else if (firstLine != null) {
                // 第一行就是数据
                TrafficFlow flow = parseCSVLine(firstLine, lineNumber + 1);
                if (flow != null) {
                    trafficFlows.add(flow);
                    successCount++;
                } else {
                    errorCount++;
                }
            }

            // 读取数据行
            while ((line = br.readLine()) != null) {
                lineNumber++;

                if (line.trim().isEmpty()) {
                    continue; // 跳过空行
                }

                TrafficFlow flow = parseCSVLine(line, lineNumber);
                if (flow != null) {
                    trafficFlows.add(flow);
                    successCount++;

                    // 批量插入（每1000条记录插入一次）
                    if (trafficFlows.size() >= 1000) {
                        batchInsertTrafficFlows(trafficFlows);
                        trafficFlows.clear();
                    }
                } else {
                    errorCount++;
                }
            }

            // 插入剩余数据
            if (!trafficFlows.isEmpty()) {
                batchInsertTrafficFlows(trafficFlows);
            }

            logger.info("数据导入完成 - 成功: {} 条, 失败: {} 条", successCount, errorCount);

        } catch (IOException e) {
            logger.error("读取CSV文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("CSV文件导入失败", e);
        }

        return successCount;
    }

    /**
     * 解析CSV行数据
     */
    private TrafficFlow parseCSVLine(String line, int lineNumber) {
        try {
            String[] parts = line.split(",");

            if (parts.length < 3) {
                logger.warn("第{}行数据格式不正确，需要至少3列: {}", lineNumber, line);
                return null;
            }

            // 解析数据
            Integer locationId = Integer.parseInt(parts[0].trim());
            Integer timeSlot = Integer.parseInt(parts[1].trim());
            Integer flowVolume = Integer.parseInt(parts[2].trim());

            // 数据验证
            if (!validateTrafficData(locationId, timeSlot, flowVolume, lineNumber)) {
                return null;
            }

            // 创建TrafficFlow对象
            TrafficFlow flow = new TrafficFlow(locationId, timeSlot, flowVolume);

            // 如果有第4列，设置位置类型
            if (parts.length > 3 && !parts[3].trim().isEmpty()) {
                flow.setLocationType(parts[3].trim().toUpperCase());
            }

            return flow;

        } catch (NumberFormatException e) {
            logger.warn("第{}行数据解析失败，数字格式错误: {}", lineNumber, line);
            return null;
        } catch (Exception e) {
            logger.warn("第{}行数据处理失败: {}, 错误: {}", lineNumber, line, e.getMessage());
            return null;
        }
    }

    /**
     * 验证交通数据
     */
    private boolean validateTrafficData(Integer locationId, Integer timeSlot, Integer flowVolume, int lineNumber) {
        // 验证位置ID
        if (locationId == null || locationId <= 0) {
            logger.warn("第{}行: 位置ID无效 ({})", lineNumber, locationId);
            return false;
        }

        // 验证时间段
        if (timeSlot == null || timeSlot < 1 || timeSlot > 24) {
            logger.warn("第{}行: 时间段无效 ({}), 应在1-24之间", lineNumber, timeSlot);
            return false;
        }

        // 验证车流量
        if (flowVolume == null || flowVolume < 0) {
            logger.warn("第{}行: 车流量无效 ({}), 不能为负数", lineNumber, flowVolume);
            return false;
        }

        // 车流量合理性检查
        if (flowVolume > 1000) {
            logger.warn("第{}行: 车流量异常高 ({}), 请检查数据", lineNumber, flowVolume);
        }

        return true;
    }

    /**
     * 判断是否为数据行（而非标题行）
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
     * 批量插入交通流量数据
     */
    private void batchInsertTrafficFlows(List<TrafficFlow> trafficFlows) {
        try {
            trafficMapper.batchInsertTrafficFlow(trafficFlows);
            logger.debug("批量插入 {} 条交通数据", trafficFlows.size());
        } catch (Exception e) {
            logger.error("批量插入交通数据失败", e);
            throw new RuntimeException("数据库插入失败", e);
        }
    }

    /**
     * 生成测试数据并导入
     */
    public int generateAndImportTestData(int locationCount, int timeSlotCount) {
        logger.info("生成测试数据: {} 个位置, {} 个时间段", locationCount, timeSlotCount);

        List<TrafficFlow> testData = new ArrayList<>();

        for (int locationId = 1; locationId <= locationCount; locationId++) {
            for (int timeSlot = 1; timeSlot <= timeSlotCount; timeSlot++) {
                // 生成模拟车流量数据
                Integer flowVolume = generateMockFlowVolume(locationId, timeSlot);
                TrafficFlow flow = new TrafficFlow(locationId, timeSlot, flowVolume);
                testData.add(flow);
            }
        }

        // 批量插入测试数据
        int batchSize = 1000;
        int totalInserted = 0;

        for (int i = 0; i < testData.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, testData.size());
            List<TrafficFlow> batch = testData.subList(i, endIndex);
            batchInsertTrafficFlows(batch);
            totalInserted += batch.size();
        }

        logger.info("测试数据生成完成，共插入 {} 条记录", totalInserted);
        return totalInserted;
    }

    /**
     * 生成模拟车流量数据
     */
    private Integer generateMockFlowVolume(int locationId, int timeSlot) {
        // 基于位置和时间段生成合理的车流量数据
        int baseFlow = 20 + (locationId % 10) * 5; // 基础流量 20-65

        // 时间段影响因子
        double timeFactor = 1.0;
        if (timeSlot >= 7 && timeSlot <= 9) {        // 早高峰
            timeFactor = 2.0;
        } else if (timeSlot >= 17 && timeSlot <= 19) { // 晚高峰
            timeFactor = 1.8;
        } else if (timeSlot >= 12 && timeSlot <= 14) { // 午高峰
            timeFactor = 1.3;
        } else if (timeSlot >= 22 || timeSlot <= 6) {  // 夜间
            timeFactor = 0.3;
        }

        // 添加随机因子
        double randomFactor = 0.7 + Math.random() * 0.6; // 0.7-1.3

        int finalFlow = (int) (baseFlow * timeFactor * randomFactor);
        return Math.max(0, Math.min(200, finalFlow)); // 限制在0-200之间
    }

    /**
     * 清空所有交通数据
     */
    public int clearAllTrafficData() {
        logger.warn("清空所有交通数据");
        return trafficMapper.clearAllTrafficData();
    }

    /**
     * 获取数据统计信息
     */
    public void printDataStatistics() {
        Long totalCount = trafficMapper.getTotalRecordCount();
        List<Integer> timeSlots = trafficMapper.getAllTimeSlots();
        List<Integer> locations = trafficMapper.getAllLocationIds();

        logger.info("=== 交通数据统计 ===");
        logger.info("总记录数: {}", totalCount);
        logger.info("位置数量: {}", locations.size());
        logger.info("时间段数量: {}", timeSlots.size());
        logger.info("位置ID范围: {} - {}",
                locations.isEmpty() ? "N/A" : locations.get(0),
                locations.isEmpty() ? "N/A" : locations.get(locations.size() - 1));
        logger.info("时间段范围: {} - {}",
                timeSlots.isEmpty() ? "N/A" : timeSlots.get(0),
                timeSlots.isEmpty() ? "N/A" : timeSlots.get(timeSlots.size() - 1));
    }
}
