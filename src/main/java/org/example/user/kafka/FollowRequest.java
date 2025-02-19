package org.example.user.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowRequest {
    private String followerId;
    private String followeeId;
    private String action;
    //형식 수정
}
