package com.history.exam.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 学生登录入参
 * <p>小程序 wx.login 返回的 code 必填，昵称与头像选填，用于首次/再次登录时落库。</p>
 */
@Data
public class StudentLoginDTO {

    /** wx.login 返回的临时登录 code */
    @NotBlank(message = "code 不能为空")
    @Size(min = 32, max = 64, message = "code 长度需在 32-64 之间")
    private String code;

    /** 昵称，选填 */
    @Size(max = 64, message = "昵称长度不能超过 64")
    private String nickname;

    /** 头像 URL，选填 */
    @Size(max = 255, message = "头像 URL 长度不能超过 255")
    private String avatarUrl;
}
