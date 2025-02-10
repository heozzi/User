package org.example.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name="members")
@Data
@NoArgsConstructor
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(name = "username")
    private String userName;
    private String password;
    private String hp;
    private String email;
    private String roles;
    private boolean enable;


    @Builder
    public UserEntity(Long uid, String userName, String password, String hp, String email, String roles, boolean enable) {
        this.uid = uid;
        this.userName = userName;
        this.password = password;
        this.hp = hp;
        this.email = email;
        this.roles = roles;
        this.enable = enable;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 역활 설정 => ROLE_USER
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        // email, password 통해서 로그인 예정
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 이메일 인증 여부를 체크
    @Override
    public boolean isEnabled() {
        return enable;
    }

    public String getUserRealName() {
        return userName;
    }
}