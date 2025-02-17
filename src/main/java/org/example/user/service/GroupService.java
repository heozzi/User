package org.example.user.service;

import jakarta.transaction.Transactional;
import org.example.user.dto.GroupDto;
import org.example.user.dto.UserDto;
import org.example.user.entity.GroupEntity;
import org.example.user.entity.GroupMembershipEntity;
import org.example.user.entity.UserEntity;
import org.example.user.repository.GroupMembershipRepository;
import org.example.user.repository.GroupRepository;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    // 그룹 생성 시 중복 그룹명 체크
    @Transactional
    public void createGroup(GroupDto groupDto) {
        // 이미 존재하는 그룹명 확인
        if (groupRepository.existsByGroupName(groupDto.getGroupName())) {
            throw new IllegalArgumentException("이미 존재하는 그룹명입니다.");
        }

        // 그룹 생성자(owner) 확인
        UserEntity owner = userRepository.findById(groupDto.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 그룹 엔티티 생성
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setGroupName(groupDto.getGroupName());
        groupEntity.setGroupDetail(groupDto.getGroupDetail());

        // 그룹 저장
        GroupEntity savedGroup = groupRepository.save(groupEntity);

        // 그룹 멤버십 생성 (생성자를 관리자로)
        GroupMembershipEntity membership = new GroupMembershipEntity();
        membership.setGroup(savedGroup);
        membership.setUser(owner);
        membership.setRole(GroupMembershipEntity.MemberRole.ADMIN);

        groupMembershipRepository.save(membership);
    }

    // 그룹 멤버 추가 메서드
    @Transactional
    public void addGroupMember(Long gid, Long uid, GroupMembershipEntity.MemberRole role) {
        GroupEntity group = groupRepository.findById(gid)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        GroupMembershipEntity membership = new GroupMembershipEntity();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setRole(role);

        groupMembershipRepository.save(membership);
    }

    // 그룹 멤버 조회 메서드
    public List<UserDto> getGroupMembers(Long gid) {
        List<GroupMembershipEntity> memberships = groupMembershipRepository.findByGroup_Gid(gid);

        return memberships.stream()
                .map(membership -> {
                    UserEntity user = membership.getUser();
                    UserDto userDto = new UserDto();
                    userDto.setUid(user.getUid());
                    userDto.setUserName(user.getUserRealName());
                    userDto.setEmail(user.getEmail());
                    return userDto;
                })
                .collect(Collectors.toList());
    }
}
