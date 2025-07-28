package com.study.odersystem.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDto<T> {
    private Boolean isSuccess;
    private T data;
    private StatusDto statusDto;

    // 성공 시 사용하는 생성자
    public static <T> ResponseDto<T> ofSuccess(T data, int statusCode, String statusMsg) {
        return new ResponseDto<>(true, data, new StatusDto(statusCode, statusMsg));
    }

    // 실패 시 사용하는 생성자
    public static <T> ResponseDto<T> ofFailure(int status, String message) {
        return new ResponseDto<>(false, null, new StatusDto(status, message));
    }
}
