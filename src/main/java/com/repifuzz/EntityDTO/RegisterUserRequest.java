package com.repifuzz.EntityDTO;

import lombok.Data;

@Data
public class RegisterUserRequest {
    private String username;
    private String email;
    private String phone;
    private String address;
    private String pinCode;
    private String city;
    private String country;
    private String password;
}
