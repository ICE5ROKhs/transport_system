package com.example.navigation.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户个人资料响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private boolean success;
    private UserProfileData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileData {
        private String name;         // 姓名
        private String phoneNumber;  // 电话号码
        private Integer age;         // 年龄
        private String sex;          // 性别
    }
} 