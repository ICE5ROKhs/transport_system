package com.example.navigation.model.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户个人资料更新请求DTO
 */
@Data
public class UserProfileUpdateRequest {
    private String name;         // 姓名(可选)
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "电话号码格式不正确")
    private String phoneNumber;  // 电话号码(可选)
    
    private Integer age;         // 年龄(可选)
    
    @Pattern(regexp = "^(男|女|其他)$", message = "性别只能选择：男、女、其他")
    private String sex;          // 性别(可选)
} 