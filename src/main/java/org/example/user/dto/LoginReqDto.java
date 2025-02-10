package org.example.user.dto;

import lombok.Data;

/**
 * 로그인시 전달되는 데이터(JSON)을 담는 그릇
 */
@Data
public class LoginReqDto {
    private String email;
    //private String userName;
    private String password;
}
