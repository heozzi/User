package org.example.user.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.user.dto.LoginReqDto;
import org.example.user.entity.UserEntity;
import org.example.user.jwt.JwtTokenProvider;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    // DI
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private TokenService tokenService;

    public String login(LoginReqDto loginReqDto, HttpServletResponse response) {
        // 1. 전달된 DTO에서 email, password 추출 -> 변수할당
        String email = loginReqDto.getEmail();
        String password = loginReqDto.getPassword();
        try{
            // 2. 이메일 회원 조회 -> jpa
            UserEntity userEntity = userRepository.findByEmail(email)
                    .orElseThrow( ()-> new IllegalArgumentException("Email not found") );

            // 3. 비번검증 -> 암호화 처리
            if( !passwordEncoder.matches(password, userEntity.getPassword()) ) {
                throw new IllegalArgumentException("비밀번호 불일치");
            }

            // 4. 활성화 여부 점검 -> 비활성화 -> 점검 요청 후 반려 (생략)
            // 5. 토큰 발급 (엑세스 신규, 리플레시 (레디스 검색후 없으면(7일이후)-> 발급))
            String accessToken = jwtTokenProvider.createAccessToken(email, password);
            String refreshToken = tokenService.getRefreshToken(email);
            if( refreshToken == null ) {
                // 가입후 최초, 아주 오랜만에 로그인한 유저(토큰 만료시간 이후 진입한 유저)
                refreshToken = jwtTokenProvider.createRefreshToken();
                // 리플레시 토큰 저장 -> 레디스
                tokenService.saveRefreshToken(email, refreshToken);
            }
            // 6. 엑세스 토큰 저장(생략, 필요시 추가),
            // 7. 응답객체 전달되어야 한다 -> 해당 내용 응답 객체의 헤더에 토큰 저장 -> 전달
            //    응답 헤더에 토큰 세팅
            response.addHeader("RefreshToken", refreshToken);
            response.addHeader("AccessToken", accessToken);
            //    본 서비스의 커스텀 신호(식별용) 세팅
            response.addHeader("X-Auth-User", email);
            //    addCookie등을 이용한 세팅도 가능 -> 선택의 문제

            // 8. 응답 헤더에 서비스의 시그니처 헤더값 세팅 -> X-Auth-User (컨셉, 생략가능, 게이트웨이에서 처리)
        } catch (Exception e) {
            System.out.println("로그인시 오류 발생" + e.getMessage());
            return "로그인 실패";
        }
        return "로그인 성공";
    }

    public void logout(String email, String accessToken) {
        // 0. 토큰 검증
        if( !jwtTokenProvider.validateToken(accessToken) ) {
            throw new IllegalArgumentException("부적절한 토큰");
        }
        // 1. 로그아웃 -> 레디스에서 이메일에 해당되는 모든 토큰 삭제
        tokenService.deleteRefreshToken(email);
        // 엑세스 토큰 저장 -> 여러 기기에 중복 로그인시 활용 -> 모든 기기 동시 로그아웃 처리!!
        // PC에서 로그인 -> 테블릿은 로그아웃 처리(다른 기긱에서 로그인 하엿습니다.)
    }

}
