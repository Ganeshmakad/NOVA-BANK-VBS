package com.vbs.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    String accountNumber;
    String name;
    String email;
    String gender;
    double balance;
    boolean blocked;
}
