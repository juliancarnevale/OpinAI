package com.opinai.controller.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}
