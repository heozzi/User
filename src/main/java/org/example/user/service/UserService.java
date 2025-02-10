package org.example.user.service;

import org.example.user.dto.UserDto;
import org.example.user.entity.UserEntity;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 회원관련 비즈니스 로직 처리
 */
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private JavaMailSender mailSender; // 자바 메일 전송 라이브러리

    // 회원가입 메소드
    public void createUser(UserDto userDto) {
        // 1. 입력값 검증
        if( userDto.getEmail() == null || userDto.getEmail().isEmpty() ) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if( userRepository.findByEmail(userDto.getEmail()).isPresent() ) {
            throw new IllegalArgumentException("Email already exists");
        }
        if( userDto.getUserName() == null || userDto.getUserName().isEmpty() ) {
            throw new IllegalArgumentException("userName cannot be empty");
        }
        if( userDto.getPassword() == null || userDto.getPassword().isEmpty() ) {
            throw new IllegalArgumentException("password cannot be empty");
        }

        // 2. 엔티티 생성
        UserEntity userEntity = UserEntity.builder()
                .email(userDto.getEmail())
                .userName(userDto.getUserName())
                .password( passwordEncoder.encode(userDto.getPassword()) )
                .roles("ROLE_USER")
                .enable(false)
                .build();

        // 3. 엔티티 저장 -> 디비에 members 테이블에 저장
        userRepository.save(userEntity);

        // 4. 인증 이메일 발송
        sendVaildEmail( userEntity );
    }


    // 이메일 전송 메소드
    private void sendVaildEmail(UserEntity userEntity) {

        // 1. 토큰 발행
        String token = UUID.randomUUID().toString();
        // 2. redis 저장
        //    이메일 인증절차
        redisTemplate.opsForValue().set(token,
                userEntity.getEmail(), 6, TimeUnit.HOURS);
        // 3. URL 구성 -> 가입한 사용자의 이메일에서 인증메일에 전송된 링크
        String url = "http://localhost:8080/user/vaild?token=" + token;
        // 4. 메일 전송 (받는 사람주소, 제목, 내용)
        sendMail( userEntity.getEmail(), "Email 인증", "링크를 눌러서 인증: " + url );
    }
    private void sendMail(String email, String subject, String content) {
        // 1. 메세지 구성
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);
        // 2. 전송
        mailSender.send(message);
    }
    // enable 컬럼 : f->t (유효할때만)
    public void updateActivate(String token) {
        // 1. 레디스 토큰 -> 이메일 획득
        String email = (String) redisTemplate.opsForValue().get(token);
        // 2. 없다면 -> 잘못된 토큰 혹은 만료된 토큰 -> 예외 처리(토큰오류)
        if(email == null) {
            throw new IllegalArgumentException("잘못된 토큰 혹은 만료된 토큰");
        }
        // 3. 존재한다면 -> 이메일(id, pk)  -> 엔티티 획득
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow( ()-> new IllegalArgumentException("사용자 오류(존재x)") );
        // 4. enable 컬럼 : f->t => 저장
        userEntity.setEnable(true);
        userRepository.save(userEntity);
        // 5. 레디스 토큰 삭제
        redisTemplate.delete(token);
    }

    // 회원탈퇴 메소드
    public void signoutUser(UserDto userDto) {
        // 1. 이메일과 비밀번호로 사용자 검증
        UserEntity userEntity = userRepository.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(userDto.getPassword(), userEntity.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 사용자 삭제
        userRepository.delete(userEntity);

        // 4. Redis 토큰 삭제 (이메일 인증 관련 토큰이 있다면)
        redisTemplate.delete(userEntity.getEmail());
    }
}
