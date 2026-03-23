package lk.iit.nextora.module.lostandfound.service;

import lk.iit.nextora.common.dto.PagedResponse;
import lk.iit.nextora.module.lostandfound.dto.request.CreateFoundItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.SearchItemRequest;
import lk.iit.nextora.module.lostandfound.dto.request.UpdateItemRequest;
import lk.iit.nextora.module.lostandfound.dto.response.ItemResponse;
import lk.iit.nextora.module.lostandfound.dto.response.ItemListResponse;
import org.springframework.data.domain.Pageable;

public interface FoundItemService {

    ItemResponse createFoundItem(CreateFoundItemRequest request);

    ItemResponse updateFoundItem(Long id, UpdateItemRequest request);

    ItemResponse getFoundItemById(Long id);

    ItemListResponse searchFoundItems(SearchItemRequest request);

    PagedResponse<ItemResponse> searchFoundItems(String keyword, String category, Pageable pageable);

    ItemResponse adminUpdateFoundItem(Long id, UpdateItemRequest request);

    void adminDeleteFoundItem(Long id);
}
