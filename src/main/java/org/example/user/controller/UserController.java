package org.example.user.controller;

import org.example.user.dto.GroupDto;
import org.example.user.dto.UserDto;
import org.example.user.entity.GroupMembershipEntity;
import org.example.user.entity.UserEntity;
import org.example.user.repository.UserRepository;
import org.example.user.service.GroupService;
import org.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDto userDto) {
        System.out.println("회원가입요청 : " + userDto.toString());
        userService.createUser(userDto);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 이메일 검증 처리
    @GetMapping("/vaild")
    public ResponseEntity<String> vaild(@RequestParam("token") String token) {
        try {
            userService.updateActivate(token);
            return ResponseEntity.ok("이메일 인증 완료. 계정이 활성화 되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("비정상, Bad Request : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버측 내부 오류 : " + e.getMessage());
        }
    }

    // 회원탈퇴
    @DeleteMapping("/signout")
    public ResponseEntity<String> signout(@RequestBody UserDto userDto) {
        try {
            userService.signoutUser(userDto);
            return ResponseEntity.ok("회원 탈퇴 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("회원 탈퇴 실패: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    // 그룹 생성
    @PostMapping("/group")
    public ResponseEntity<String> createGroup(
            @RequestBody GroupDto groupDto,
            @RequestHeader("X-Auth-User") String email
    ) {
        try {
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            groupDto.setOwnerId(user.getUid());

            groupService.createGroup(groupDto);
            return ResponseEntity.ok("그룹 생성 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    // 그룹 멤버 추가
    @PostMapping("group/{gid}/members")
    public ResponseEntity<String> addGroupMember(
            @PathVariable Long gid,
            @RequestParam Long uid,
            @RequestParam GroupMembershipEntity.MemberRole role
    ) {
        try {
            groupService.addGroupMember(gid, uid, role);
            return ResponseEntity.ok("멤버 추가 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 그룹 멤버 조회
    @GetMapping("group/{gid}/members")
    public ResponseEntity<List<UserDto>> getGroupMembers(@PathVariable Long gid) {
        try {
            List<UserDto> members = groupService.getGroupMembers(gid);
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 프로필
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("X-Auth-User") String email) {
        try {
            UserDto userProfile = userService.getProfile(email);
            return ResponseEntity.ok(userProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    // 특정 사용자 프로필 조회 (관리자 또는 같은 그룹 멤버만 조회 가능)
    @GetMapping("/profile/{uid}")
    public ResponseEntity<?> getUserProfile(
            @PathVariable Long uid,
            @RequestHeader("X-Auth-User") String email) {
        try {
            UserDto userProfile = userService.getUserProfile(uid, email);
            return ResponseEntity.ok(userProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    // 테스트용
    @PostMapping("/checkemail")
    public Long checkemail(@RequestBody UserDto userDto) {
        System.out.println("이메일 pid 확인 "+userDto.getEmail());
        Optional<UserEntity> data =userRepository.findByEmail(userDto.getEmail());
        return data.get().getUid();
    }

    // 그룹 속한지 테스트
    @GetMapping("groupCheck/")
    public List<Long> checkGroupMembers(
            @RequestParam String email) {

        List<Long> result = groupService.checkGroupMembers(email);
        return result;
    }
}
