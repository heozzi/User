package org.example.user.controller;

import org.example.user.dto.DMDto;
import org.example.user.kafka.DMEventPublisher;
import org.example.user.service.DMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dm")
public class DMController {
    private final DMService dmService;
    private final DMEventPublisher dmEventPublisher;

    @Autowired
    public DMController(DMService dmService, DMEventPublisher dmEventPublisher) {
        this.dmService = dmService;
        this.dmEventPublisher = dmEventPublisher;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendDM(
            @RequestBody DMDto dmDto,
            @RequestHeader("X-Auth-User") String senderEmail) {
        try {
            // DM 저장
            dmService.sendDM(senderEmail, dmDto);

            // Kafka에 이벤트 발행
            dmEventPublisher.publishDMEvent(senderEmail,
                    dmDto.getReceiverId().toString(),
                    dmDto.getContent());

            return ResponseEntity.ok("DM이 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/received")
    public ResponseEntity<?> getReceivedDMs(@RequestHeader("X-Auth-User") String email) {
        return ResponseEntity.ok(dmService.getReceivedDMs(email));
    }
}
