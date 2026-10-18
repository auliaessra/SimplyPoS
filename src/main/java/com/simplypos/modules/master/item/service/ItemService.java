package com.simplypos.modules.master.item.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.simplypos.modules.master.common.exception.BusinessException;
import com.simplypos.modules.master.item.dto.ItemRequest;
import com.simplypos.modules.master.item.dto.ItemResponse;
import com.simplypos.modules.master.item.entity.Item;
import com.simplypos.modules.master.item.repository.ItemRepository;
import com.simplypos.modules.master.unit.entity.Unit;
import com.simplypos.modules.master.unit.repository.UnitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository repo;
    private final UnitRepository unitRepo;

    public Page<ItemResponse> search(String keyword, Pageable pageable) {
        Page<Item> page = (keyword == null || keyword.isBlank())
                ? repo.findAll(pageable)
                : repo.search(keyword, pageable);
        return page.map(this::toResponse);
    }

    public ItemResponse findById(Long id) {
        return toResponse(repo.findById(id)
                .orElseThrow(() -> new BusinessException("Item tidak ditemukan")));
    }

    public Item getEntity(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new BusinessException("Item tidak ditemukan"));
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        if (repo.existsByCode(request.getCode())) {
            throw new BusinessException("Kode item sudah digunakan: " + request.getCode());
        }

        Unit unit = resolveUnit(request.getUnitId());

        Item entity = Item.builder()
                .code(request.getCode())
                .name(request.getName())
                .unit(unit)
                .description(request.getDescription())
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .build();

        return toResponse(repo.save(entity));
    }

    @Transactional
    public ItemResponse update(Long id, ItemRequest request) {
        Item entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Item tidak ditemukan"));

        if (!entity.getCode().equals(request.getCode())
                && repo.existsByCode(request.getCode())) {
            throw new BusinessException("Kode item sudah digunakan");
        }

        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setIsActive(request.getIsActive());

        if (request.getUnitId() != null) {
            entity.setUnit(resolveUnit(request.getUnitId()));
        }

        return toResponse(repo.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        Item entity = repo.findById(id)
                .orElseThrow(() -> new BusinessException("Item tidak ditemukan"));
        repo.delete(entity);
    }

    private Unit resolveUnit(Long unitId) {
        if (unitId == null) {
            return unitRepo.findByIsBaseTrue()
                    .orElseThrow(() -> new BusinessException("Base unit (ML) tidak ditemukan"));
        }
        return unitRepo.findById(unitId)
                .orElseThrow(() -> new BusinessException("Unit tidak ditemukan"));
    }

    private ItemResponse toResponse(Item e) {
        return new ItemResponse(
                e.getId(), e.getCode(), e.getName(),
                e.getUnit() != null ? e.getUnit().getId() : null,
                e.getUnitCode(),
                e.getUnitName(),
                e.getDescription(),
                e.getIsActive());
    }
}