package com.jaydenxiao.androidfire.api;

import com.jaydenxiao.androidfire.bean.GirlData;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import rx.Observable;

/**
 * author : Conan
 * time   : 2019/6/4
 * desc   :
 */
public interface SinaPhotoService {
    @GET("data/福利/{size}/{page}")
    Observable<GirlData> getPhotoList(
            @Header("Cache-Control") String cacheControl,
            @Path("size") int size,
            @Path("page") int page);

}
