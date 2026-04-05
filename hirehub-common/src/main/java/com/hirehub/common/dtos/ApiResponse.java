package com.hirehub.common.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Factories statiques — usage : ApiResponse.ok(monObjet)
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<T>(true, "OK", data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<T>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<T>(false, message, null);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<T>(false, message, data);
    }

}
