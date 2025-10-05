package com.coding.projects.WanderInn.repository;

import com.coding.projects.WanderInn.entity.Hotel;
import com.coding.projects.WanderInn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findByOwner(User user);
}
