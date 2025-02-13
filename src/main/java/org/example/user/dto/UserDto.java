package org.example.user.dto;

import lombok.Data;
import lombok.ToString;
import org.antlr.v4.runtime.misc.NotNull;
import org.example.user.entity.UserEntity;

@Data
@ToString
public class UserDto {
    // @NotNull : 향후 버전에서는 제거될수 있다. 널 허용 x 반드시 세팅하는 강제조항
    @NotNull
    private Long uid;
    @NotNull
    private String userName;
    @NotNull
    private String password;
    @NotNull
    private String email;

    private String role = "ROLE_USER";

    // 프로필 조회용 생성자 추가
    public static UserDto fromEntity(UserEntity entity) {
        UserDto dto = new UserDto();
        dto.setUid(entity.getUid());
        dto.setUserName(entity.getUserRealName());
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRoles());
        return dto;
    }
}
