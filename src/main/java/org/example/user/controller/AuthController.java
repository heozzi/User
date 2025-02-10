package org.example.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.user.dto.LoginReqDto;
import org.example.user.service.AuthService;
import org.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserService userService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginReqDto loginReqDto,
                                        HttpServletResponse response) {
        return ResponseEntity.ok( authService.login(loginReqDto, response) );
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("X-Auth-User") String email,
                                         @RequestHeader("Authorization") String accessToken) {
        authService.logout(email, accessToken);
        return ResponseEntity.ok("로그아웃 성공");
    }

}
