package org.example.user.service;

import org.example.user.dto.DMDto;
import org.example.user.dto.DMReqDto;
import org.example.user.entity.DMEntity;
import org.example.user.entity.UserEntity;
import org.example.user.repository.DMRepository;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DMService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DMRepository dmRepository;

    public void sendDM(String senderEmail, DMDto dmDto) {
        UserEntity sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("발신자를 찾을 수 없습니다."));

        UserEntity receiver = userRepository.findById(dmDto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("수신자를 찾을 수 없습니다."));

        if (!receiver.isEnabled()) {
            throw new IllegalArgumentException("비활성화된 사용자에게는 DM을 보낼 수 없습니다.");
        }

        DMEntity dm = DMEntity.builder()
                .sender(sender)
                .receiver(receiver)
                .content(dmDto.getContent())
                .build();

        dmRepository.save(dm);
    }

    public List<DMReqDto> getReceivedDMs(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return dmRepository.findByReceiverAndIsReadFalse(user)
                .stream()
                .map(DMReqDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DMReqDto> getSentDMs(String email) {
        UserEntity sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return dmRepository.findBySenderAndReceiver(sender, sender)
                .stream()
                .map(DMReqDto::fromEntity)
                .collect(Collectors.toList());
    }
}