package com.jaydenxiao.common.http;

import java.util.HashMap;

/**
 * author : Conan
 * time   : 2019/6/4
 * desc   :
 */
public interface IApiProvider {
    HashMap<String, String> getHeader(Class service);

    String getBaseUrl(Class service);
}
