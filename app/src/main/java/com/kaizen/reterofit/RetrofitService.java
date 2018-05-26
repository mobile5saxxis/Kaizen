package com.kaizen.reterofit;

import com.kaizen.models.CategoryResponse;
import com.kaizen.models.ChildCategoryResponse;
import com.kaizen.models.ListChildCategory;
import com.kaizen.models.ListChildCategoryResponse;
import com.kaizen.models.SubcategoryResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by FAMILY on 14-12-2017.
 */

public interface RetrofitService {

    @GET(APIUrls.CATEGORY)
    Call<CategoryResponse> getCategories();

    @GET(APIUrls.SUB_CATEGORY)
    Call<SubcategoryResponse> getSubCategories(@Query("menuid") String menuid);

    @GET(APIUrls.CHILD_CATEGORY)
    Call<ChildCategoryResponse> getChildCategory(@Query("menuid") String menuid, @Query("subid") String subid);

    @GET(APIUrls.LIST_CHILD_CATEGORY)
    Call<ListChildCategoryResponse> getListChildCategory(@Query("menuid") String menuid, @Query("subid") String subid, @Query("childid") String childid);
}