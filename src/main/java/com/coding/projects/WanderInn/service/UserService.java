package com.coding.projects.WanderInn.service;

import com.coding.projects.WanderInn.dto.ProfileUpdateRequestDto;
import com.coding.projects.WanderInn.dto.UserDto;
import com.coding.projects.WanderInn.entity.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
