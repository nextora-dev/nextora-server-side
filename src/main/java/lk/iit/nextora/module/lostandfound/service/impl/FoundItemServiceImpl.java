package lk.iit.nextora.module.lostandfound.service.impl;

import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.entity.FoundItem;
import lk.iit.nextora.module.lostandfound.entity.ItemCategory;
import lk.iit.nextora.module.lostandfound.mapper.FoundItemMapper;
import lk.iit.nextora.module.lostandfound.repository.FoundItemRepository;
import lk.iit.nextora.module.lostandfound.repository.ItemCategoryRepository;
import lk.iit.nextora.module.lostandfound.service.FoundItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoundItemServiceImpl implements FoundItemService {

    private final FoundItemRepository foundItemRepository;
    private final ItemCategoryRepository categoryRepository;

    @Override
    public ItemResponse createFoundItem(CreateFoundItemRequest request) {
        ItemCategory category = categoryRepository.findAll()
                .stream()
                .filter(c -> c.getName().equalsIgnoreCase(request.getCategory()))
                .findFirst()
                .orElseThrow();

        FoundItem item = new FoundItem();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setLocation(request.getLocation());
        item.setContactNumber(request.getContactNumber());
        item.setCategory(category);

        return FoundItemMapper.toResponse(
                foundItemRepository.save(item)
        );
    }

    @Override
    public ItemResponse updateFoundItem(Long id, UpdateItemRequest request) {
        FoundItem item = foundItemRepository.findById(id).orElseThrow();

        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getLocation() != null) item.setLocation(request.getLocation());
        if (request.getContactNumber() != null) item.setContactNumber(request.getContactNumber());
        if (request.getActive() != null) item.setActive(request.getActive());

        return FoundItemMapper.toResponse(
                foundItemRepository.save(item)
        );
    }

    @Override
    public ItemListResponse searchFoundItems(SearchItemRequest request) {
        var items = foundItemRepository.findAll();

        return ItemListResponse.builder()
                .items(items.stream()
                        .map(FoundItemMapper::toResponse)
                        .collect(Collectors.toList()))
                .totalElements(items.size())
                .build();
    }
}
