package org.example.user.kafka;

public class FollowRequest {
    private String followerId;
    private String followeeId;
    private String action;  // "follow" 또는 "unfollow"

    public FollowRequest() {}

    // Getter & Setter
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
