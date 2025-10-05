package com.coding.projects.WanderInn.service;

import com.coding.projects.WanderInn.dto.*;
import com.coding.projects.WanderInn.entity.Inventory;
import com.coding.projects.WanderInn.entity.Room;
import com.coding.projects.WanderInn.entity.User;
import com.coding.projects.WanderInn.exception.ResourceNotFoundException;
import com.coding.projects.WanderInn.repository.HotelMinPriceRepository;
import com.coding.projects.WanderInn.repository.InventoryRepository;
import com.coding.projects.WanderInn.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.coding.projects.WanderInn.util.AppUtils.getCurrentUser;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository; // if used elsewhere

    @Override
    @Caching(evict = {
            // the room’s cached inventory becomes stale after bulk generation
            @CacheEvict(cacheNames = "inventoryByRoom", key = "#room.id"),
            // search results across dates are impacted → clear search cache
            @CacheEvict(cacheNames = "inventorySearch", allEntries = true)
    })
    public void initializeRoomForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for (; !today.isAfter(endDate); today = today.plusDays(1)) {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reservedCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();
            inventoryRepository.save(inventory);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "inventoryByRoom", key = "#room.id"),
            @CacheEvict(cacheNames = "inventorySearch", allEntries = true)
    })
    public void deleteAllInventories(Room room) {
        log.info("Deleting the inventories of room with id: {}", room.getId());
        inventoryRepository.deleteByRoom(room);
    }

    @Override
    @Cacheable(
            cacheNames = "inventorySearch",
            key =
                    // build a stable key from *all* inputs that affect results
                    "T(java.util.Objects).hash(" +
                            "#hotelSearchRequest.city?.toLowerCase()?.trim()," +
                            "#hotelSearchRequest.startDate," +
                            "#hotelSearchRequest.endDate," +
                            "#hotelSearchRequest.roomsCount," +
                            "#hotelSearchRequest.page," +
                            "#hotelSearchRequest.size" +
                            ")",
            condition =
                    "#hotelSearchRequest.city != null && !#hotelSearchRequest.city.isBlank() && " +
                            "#hotelSearchRequest.startDate != null && #hotelSearchRequest.endDate != null",
            unless = "#result == null || #result.isEmpty()" // don’t store empty pages
    )
    public Page<HotelPriceResponseDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels for {} city, from {} to {}",
                hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(),
                hotelSearchRequest.getEndDate());

        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(), hotelSearchRequest.getSize());
        long dateCount = ChronoUnit.DAYS.between(
                hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) + 1;

        Page<HotelPriceDto> hotelPage =
                inventoryRepository.findHotelsWithAvailableforSearchInventory(
                        hotelSearchRequest.getCity(),
                        hotelSearchRequest.getStartDate(),
                        hotelSearchRequest.getEndDate(),
                        hotelSearchRequest.getRoomsCount(),
                        dateCount,
                        pageable
                );

        return hotelPage.map(hotelPriceDto -> {
            HotelPriceResponseDto dto =
                    modelMapper.map(hotelPriceDto.getHotel(), HotelPriceResponseDto.class);
            dto.setPrice(hotelPriceDto.getPrice());
            return dto;
        });
    }

    @Override
    @Cacheable(
            cacheNames = "inventoryByRoom",
            key = "#roomId",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Getting All inventory by room for room with id: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        User user = getCurrentUser();
        if (!user.equals(room.getHotel().getOwner()))
            throw new AccessDeniedException("You are not the owner of room with id: " + roomId);

        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map(el -> modelMapper.map(el, InventoryDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Caching(evict = {
            // room inventory list becomes stale after updates in a range
            @CacheEvict(cacheNames = "inventoryByRoom", key = "#roomId"),
            // search results (date/availability) are affected → clear search cache
            @CacheEvict(cacheNames = "inventorySearch", allEntries = true)
    })
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
        log.info("Updating inventory for room {} between {} - {}",
                roomId, updateInventoryRequestDto.getStartDate(), updateInventoryRequestDto.getEndDate());

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        User user = getCurrentUser();
        if (!user.equals(room.getHotel().getOwner()))
            throw new AccessDeniedException("You are not the owner of room with id: " + roomId);

        inventoryRepository.getInventoryAndLockBeforeUpdate(
                roomId,
                updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate()
        );

        inventoryRepository.updateInventory(
                roomId,
                updateInventoryRequestDto.getStartDate(),
                updateInventoryRequestDto.getEndDate(),
                updateInventoryRequestDto.getClosed(),
                updateInventoryRequestDto.getSurgeFactor()
        );
    }
}