package com.example.benoit.shopify_summer2018mobile;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RequestInterfaceProductInfo {

    @GET("admin/products/{id}.json?page=1&access_token=c32313df0d0ef512ca64d5b336a0d7c6")
    Call<JSONResponseProductInfo> getJSON(@Path("id") String groupId);
}
