package org.example.user.dto;

import lombok.Data;
import org.example.user.entity.DMEntity;

import java.time.LocalDateTime;

@Data
public class DMReqDto {
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime sentAt;

    public static DMReqDto fromEntity(DMEntity dm) {
        DMReqDto dto = new DMReqDto();
        dto.setSenderId(dm.getSender().getUid());
        dto.setSenderName(dm.getSender().getUserRealName());
        dto.setContent(dm.getContent());
        dto.setSentAt(dm.getSentAt());
        return dto;
    }
}