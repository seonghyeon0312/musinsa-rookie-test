package com.musinsa.course.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "모든 API에서 사용하는 공통 응답 래퍼")
public class ApiResponse<T> {

    @Schema(description = "성공 여부")
    private final boolean success;

    @Schema(description = "성공 시 데이터 페이로드", nullable = true)
    private final T data;

    @Schema(description = "오류 코드", nullable = true)
    private final String error;

    @Schema(description = "오류 메시지", nullable = true)
    private final String message;

    @Schema(description = "응답 생성 시각")
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, T data, String error, String message) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode, message);
    }
}
