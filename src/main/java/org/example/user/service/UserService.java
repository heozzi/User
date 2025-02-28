package org.example.user.service;

import jakarta.transaction.Transactional;
import org.example.user.dto.UserDto;
import org.example.user.entity.GroupMembershipEntity;
import org.example.user.entity.UserEntity;
import org.example.user.repository.GroupMembershipRepository;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 회원관련 비즈니스 로직 처리
 */

@Service
public class UserService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

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
        String url = "http://34.210.64.115:8080/user/vaild?token=" + token;
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
    @Transactional
    public void signoutUser(UserDto userDto) {
        // 1. 이메일로 사용자 찾기
        UserEntity userEntity = userRepository.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(userDto.getPassword(), userEntity.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 사용자의 UID 가져오기
        Long uid = userEntity.getUid();

        try {
            // 4. Post 서비스에 사용자 관련 게시글 및 댓글 삭제 요청
            // Post 서비스 포트는 실제 환경에 맞게 수정 필요
            try {
                String postServiceUrl = "http://35.86.77.149:8081/post/deleteByUser/" + uid;
                restTemplate.delete(postServiceUrl);
                System.out.println("[회원탈퇴] 사용자 " + uid + "의 게시글 및 댓글 삭제 완료");
            } catch (Exception e) {
                // 게시글 서비스 호출 실패 시에도 회원탈퇴는 진행
                System.err.println("[회원탈퇴] 게시글 삭제 중 오류 발생: " + e.getMessage());
            }

            // 5. 사용자가 속한 모든 그룹에서 탈퇴
            groupMembershipRepository.deleteByUser(userEntity);
            System.out.println("[회원탈퇴] 사용자 " + uid + "의 그룹 멤버십 삭제 완료");

            // 6. 사용자 삭제
            userRepository.delete(userEntity);
            System.out.println("[회원탈퇴] 사용자 " + uid + " 삭제 완료");

            // 7. Redis 인증 토큰 삭제
            redisTemplate.delete(userEntity.getEmail());
            System.out.println("[회원탈퇴] 사용자 " + uid + "의 Redis 토큰 삭제 완료");

        } catch (Exception e) {
            throw new RuntimeException("회원 탈퇴 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 자신의 프로필 조회
    public UserDto getProfile(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return UserDto.fromEntity(userEntity);
    }

    // 특정 사용자의 프로필 조회
    public UserDto getUserProfile(Long uid, String requestingUserEmail) {
        // 요청한 사용자 확인
        UserEntity requestingUser = userRepository.findByEmail(requestingUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("요청한 사용자를 찾을 수 없습니다."));

        // 조회 대상 사용자 확인
        UserEntity targetUser = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("조회 대상 사용자를 찾을 수 없습니다."));

        // 권한 체크 로직 (예: 같은 그룹 멤버인지 확인)
        if (!hasViewPermission(requestingUser, targetUser)) {
            throw new IllegalArgumentException("해당 사용자의 프로필을 조회할 권한이 없습니다.");
        }

        return UserDto.fromEntity(targetUser);
    }

    // 프로필 조회 권한 확인
    private boolean hasViewPermission(UserEntity requestingUser, UserEntity targetUser) {
        // 1. 자기 자신의 프로필인 경우
        if (requestingUser.getUid().equals(targetUser.getUid())) {
            return true;
        }

        // 2. 관리자인 경우
        if (requestingUser.getRoles().contains("ROLE_ADMIN")) {
            return true;
        }

        // 3. 같은 그룹의 멤버인 경우
        List<GroupMembershipEntity> requestingUserGroups = groupMembershipRepository.findByUser(requestingUser);
        List<GroupMembershipEntity> targetUserGroups = groupMembershipRepository.findByUser(targetUser);

        return requestingUserGroups.stream()
                .anyMatch(requestingGroup ->
                        targetUserGroups.stream()
                                .anyMatch(targetGroup ->
                                        requestingGroup.getGroup().getGid().equals(targetGroup.getGroup().getGid())
                                )
                );
    }

}
