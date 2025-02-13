package org.example.user.kafka;

public class FollowRequest {
    private String followerId;
    private String followeeId;

    // getter와 setter 메서드 추가
    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getFolloweeId() {
        return followeeId;
    }

    public void setFolloweeId(String followeeId) {
        this.followeeId = followeeId;
    }
}
