package com.coding.projects.WanderInn.repository;

import com.coding.projects.WanderInn.entity.Guest;
import com.coding.projects.WanderInn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuestRepository extends JpaRepository<Guest, Long> {
    List<Guest> findByUser(User user);
}