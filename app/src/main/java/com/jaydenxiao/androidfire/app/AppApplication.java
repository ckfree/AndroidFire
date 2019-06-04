package com.jaydenxiao.androidfire.app;

import com.jaydenxiao.androidfire.BuildConfig;
import com.jaydenxiao.androidfire.api.ApiProvider;
import com.jaydenxiao.common.baseapp.BaseApplication;
import com.jaydenxiao.common.commonutils.LogUtils;
import com.jaydenxiao.common.http.Api;

/**
 * APPLICATION
 */
public class AppApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化logger
        LogUtils.logInit(BuildConfig.LOG_DEBUG);
        Api.init(new ApiProvider());
    }
}
