package com.cafe.booking.service;

import com.cafe.booking.dto.Dtos;
import com.cafe.booking.repository.MenuItemRepository;
import com.cafe.booking.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LookupService {

    private final RestaurantTableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    private final Mapper mapper;

    public LookupService(RestaurantTableRepository tableRepository,
                         MenuItemRepository menuItemRepository,
                         Mapper mapper) {
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
        this.mapper = mapper;
    }

    public List<Dtos.TableDto> listTables() {
        return tableRepository.findAll().stream()
                .sorted((a, b) -> Integer.compare(a.getTableNumber(), b.getTableNumber()))
                .map(mapper::toDto)
                .toList();
    }

    public List<Dtos.MenuItemDto> listMenu() {
        return menuItemRepository.findByAvailableTrueOrderByCategoryAscNameAsc().stream()
                .map(mapper::toDto)
                .toList();
    }
}
