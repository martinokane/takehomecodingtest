package com.barclays.takehomecodingtest.dto;

import java.util.List;

public record BadRequestErrorResponse(String message, List<ValidationErrorDetail> details) {
}
