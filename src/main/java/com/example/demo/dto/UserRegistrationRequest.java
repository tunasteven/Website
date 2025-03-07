package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRegistrationRequest {
    // Getters 和 Setters
    private String username;
    private String email;
    private String password;
    private String phone;
    private String birthdate; // 可以根據需要修改為更詳細的格式，例如 "yyyy-MM-dd"
    private String title; // 這是 String 類型，將在服務層轉換為 Gender
    private String city;
    private String address;


    // 預設建構子
    public UserRegistrationRequest() {}


}
