package org.example.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private Long gid;
    private String groupName;
    private String groupDetail;
    private Long ownerId; // 그룹 생성자 ID
}
