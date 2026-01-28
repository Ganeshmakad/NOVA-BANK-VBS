package com.vbs.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RaiseQueryDto {
    int id;
    String accountNumber;
    String issueType;
    String subject;
    String description;
}
