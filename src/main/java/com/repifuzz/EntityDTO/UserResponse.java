package com.repifuzz.EntityDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String pinCode;
    private String city;
    private String country;
}
