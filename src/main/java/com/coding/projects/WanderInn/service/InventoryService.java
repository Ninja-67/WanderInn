package com.coding.projects.WanderInn.service;

import com.coding.projects.WanderInn.dto.HotelPriceResponseDto;
import com.coding.projects.WanderInn.dto.HotelSearchRequest;
import com.coding.projects.WanderInn.dto.InventoryDto;
import com.coding.projects.WanderInn.dto.UpdateInventoryRequestDto;
import com.coding.projects.WanderInn.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);

    Page<HotelPriceResponseDto> searchHotels(HotelSearchRequest hotelSearchRequest);

    List<InventoryDto> getAllInventoryByRoom(Long roomId);

    void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto);
}
