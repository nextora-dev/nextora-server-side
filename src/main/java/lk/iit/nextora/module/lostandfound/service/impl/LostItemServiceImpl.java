package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;
import lk.iit.nextora.module.lostandfound.entity.LostItem;
import lk.iit.nextora.module.lostandfound.mapper.LostItemMapper;
import lk.iit.nextora.module.lostandfound.repository.ItemCategoryRepository;
import lk.iit.nextora.module.lostandfound.repository.LostItemRepository;
import lk.iit.nextora.module.lostandfound.service.LostItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LostItemServiceImpl implements LostItemService {

    private final LostItemRepository lostItemRepository;
    private final ItemCategoryRepository categoryRepository;

    @Override
    public ItemResponse createLostItem(CreateLostItemRequest request) {
        ItemCategory category = categoryRepository
                .findAll()
                .stream()
                .filter(c -> c.getName().equalsIgnoreCase(request.getCategory()))
                .findFirst()
                .orElseThrow();

        LostItem item = new LostItem();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setLocation(request.getLocation());
        item.setContactNumber(request.getContactNumber());
        item.setCategory(category);

        return LostItemMapper.toResponse(
                lostItemRepository.save(item)
        );
    }

    @Override
    public ItemResponse updateLostItem(Long id, UpdateItemRequest request) {
        LostItem item = lostItemRepository.findById(id).orElseThrow();

        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getLocation() != null) item.setLocation(request.getLocation());
        if (request.getContactNumber() != null) item.setContactNumber(request.getContactNumber());
        if (request.getActive() != null) item.setActive(request.getActive());

        return LostItemMapper.toResponse(
                lostItemRepository.save(item)
        );
    }

    @Override
    public ItemListResponse searchLostItems(SearchItemRequest request) {
        var items = lostItemRepository.findAll();

        return ItemListResponse.builder()
                .items(items.stream()
                        .map(LostItemMapper::toResponse)
                        .collect(Collectors.toList()))
                .totalElements(items.size())
                .build();
    }
}
