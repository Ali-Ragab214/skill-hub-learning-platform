package com.example.Skill_Hub_Learning_Platform.application.services.user;

import com.example.Skill_Hub_Learning_Platform.application.dto.response.UserResponse;
import com.example.Skill_Hub_Learning_Platform.application.exceptions.ResourceNotFoundException;
import com.example.Skill_Hub_Learning_Platform.application.mapper.UserMapper;
import com.example.Skill_Hub_Learning_Platform.domain.models.User;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.LessonProgressRepository;
import com.example.Skill_Hub_Learning_Platform.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Delete all progress tracking entries associated with the user
        lessonProgressRepository.deleteByStudentId(id);
        lessonProgressRepository.deleteByInstructorId(id);

        userRepository.delete(user);
    }
}
