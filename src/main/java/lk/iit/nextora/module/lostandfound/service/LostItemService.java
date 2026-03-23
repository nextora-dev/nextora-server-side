package lk.iit.nextora.module.lostandfound.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.lostandfound.dto.request.CreateLostItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import org.springframework.data.domain.Pageable;

public interface LostItemService {

    ItemResponse createLostItem(CreateLostItemRequest request);

    ItemResponse updateLostItem(Long id, UpdateItemRequest request);

    ItemResponse getLostItemById(Long id);

    ItemListResponse searchLostItems(SearchItemRequest request);

    PagedResponse<ItemResponse> searchLostItems(String keyword, String category, Pageable pageable);

    ItemResponse adminUpdateLostItem(Long id, UpdateItemRequest request);

    void adminDeleteLostItem(Long id);
}
