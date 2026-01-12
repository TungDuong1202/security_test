package com.example.user.repository;

import com.example.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserProfileRepository extends JpaRepository<UserProfile, Long> {
}
