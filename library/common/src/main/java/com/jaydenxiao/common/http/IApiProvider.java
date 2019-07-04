package com.jaydenxiao.common.http;

import java.util.HashMap;

/**
 * author : Conan
 * time   : 2019/6/4
 * desc   :
 */
public interface IApiProvider extends GsonConverterFactory.IPreHandler{

    String baseUrl(Class service);

    HashMap<String, String> globalHeaders(Class service);
}
