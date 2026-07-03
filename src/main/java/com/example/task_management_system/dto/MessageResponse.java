package com.example.task_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Simple API message response")
public class MessageResponse {

    @Schema(description = "Response message", example = "Registration successful")
    private String message;
}
