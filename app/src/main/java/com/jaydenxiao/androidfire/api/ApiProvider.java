package com.jaydenxiao.androidfire.api;

import com.jaydenxiao.common.http.IApiProvider;

import java.util.HashMap;

/**
 * author : Conan
 * time   : 2019/6/4
 * desc   :
 */
public class ApiProvider implements IApiProvider {

    @Override
    public HashMap<String, String> getHeader(Class service) {
        return new HashMap<>();
    }

    @Override
    public String getBaseUrl(Class service) {
        String host;
        if (NetEastService.class.equals(service)) {
            host = ApiConstants.NETEAST_HOST;
        } else if (SinaPhotoService.class.equals(service)) {
            host = ApiConstants.SINA_PHOTO_HOST;
        } else if (KakuService.class.equals(service)) {
            host = "http://kaku.com/";
        } else {
            host = "";
        }
        return host;
    }
}
