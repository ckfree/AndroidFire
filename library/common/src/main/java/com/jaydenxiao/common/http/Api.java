package com.jaydenxiao.common.http;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.jaydenxiao.common.baseapp.BaseApplication;
import com.jaydenxiao.common.commonutils.HttpsUtils;
import com.jaydenxiao.common.commonutils.NetWorkUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

/**
 * des:retorfit api
 */
public class Api<T> {
    private static final String TAG = "OkGo";
    private static final long DEFAULT_MILLISECONDS = 60000;      //默认的超时时间
    private Retrofit retrofit;
    private T mService;
    private OkHttpClient okHttpClient;

    private static IApiProvider mProvider;
    private static HashMap<String, Api> sRetrofitManager = new HashMap<>();

    /*************************缓存设置*********************/
/*
   1. noCache 不使用缓存，全部走网络

    2. noStore 不使用缓存，也不存储缓存

    3. onlyIfCached 只使用缓存

    4. maxAge 设置最大失效时间，失效则不使用 需要服务器配合

    5. maxStale 设置最大失效时间，失效则不使用 需要服务器配合 感觉这两个类似 还没怎么弄清楚，清楚的同学欢迎留言

    6. minFresh 设置有效时间，依旧如上

    7. FORCE_NETWORK 只走网络

    8. FORCE_CACHE 只走缓存*/

    /**
     * 设缓存有效期为两天
     */
    private static final long CACHE_STALE_SEC = 60 * 60 * 24 * 2;
    /**
     * 查询缓存的Cache-Control设置，为if-only-cache时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
     * max-stale 指示客户机可以接收超出超时期间的响应消息。如果指定max-stale消息的值，那么客户机可接收超出超时期指定值之内的响应消息。
     */
    private static final String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;
    /**
     * 查询网络的Cache-Control设置，头部Cache-Control设为max-age=0
     * (假如请求了服务器并在a时刻返回响应结果，则在max-age规定的秒数内，浏览器将不会发送对应的请求到服务器，数据由缓存直接返回)时则不会使用缓存而请求服务器
     */
    private static final String CACHE_CONTROL_AGE = "max-age=0";

    //构造方法私有
    private Api(Class<T> tClass) {
        //开启Log
        LoggerInterceptor loggingInterceptor = new LoggerInterceptor(TAG);
        loggingInterceptor.setPrintLevel(LoggerInterceptor.Level.BODY);
        loggingInterceptor.setColorLevel(Level.INFO);
        //缓存
        File cacheFile = new File(BaseApplication.getAppContext().getCacheDir(), "cache");
        Cache cache = new Cache(cacheFile, 1024 * 1024 * 100); //100Mb
        //增加头部信息
        Interceptor headerInterceptor = chain -> {
            Headers headers = Headers.of(mProvider.globalHeaders(tClass));
            Request build = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .headers(headers)
                    .build();
            return chain.proceed(build);
        };

        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();

        okHttpClient = new OkHttpClient.Builder()
                .readTimeout(Api.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .writeTimeout(Api.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .connectTimeout(Api.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
                .addInterceptor(mRewriteCacheControlInterceptor)
                .addNetworkInterceptor(mRewriteCacheControlInterceptor)
                //header拦截器
                .addInterceptor(headerInterceptor)
                //日志拦截器
                .addInterceptor(loggingInterceptor)
                //信任所有证书,不安全有风险
                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
                //https的域名匹配规则
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .cache(cache)
                .build();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create((GsonConverterFactory.IPreHandler)mProvider))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(mProvider.baseUrl(tClass))
                .build();
        mService = retrofit.create(tClass);
    }

    public static boolean init(IApiProvider provider) {
        mProvider = provider;
        return true;
    }

    public static <T> T getService(Class<T> tClass) {
        String key = tClass.getCanonicalName();
        Api retrofitManager = sRetrofitManager.get(key);
        if (retrofitManager == null) {
            retrofitManager = new Api<>(tClass);
            sRetrofitManager.put(key, retrofitManager);
        }
        return (T) retrofitManager.mService;
    }

    /**
     * OkHttpClient
     *
     * @return
     */
    public static <T> OkHttpClient getOkHttpClient(Class<T> tClass) {
        String key = tClass.getCanonicalName();
        Api retrofitManager = sRetrofitManager.get(key);
        if (retrofitManager == null) {
            retrofitManager = new Api<>(tClass);
            sRetrofitManager.put(key, retrofitManager);
        }
        return retrofitManager.okHttpClient;
    }


    /**
     * 根据网络状况获取缓存的策略
     */
    @NonNull
    public static String getCacheControl() {
        return NetWorkUtils.isNetConnected(BaseApplication.getAppContext()) ? CACHE_CONTROL_AGE : CACHE_CONTROL_CACHE;
    }

    /**
     * 云端响应头拦截器，用来配置缓存策略
     * Dangerous interceptor that rewrites the server's cache-control header.
     */
    private final Interceptor mRewriteCacheControlInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            String cacheControl = request.cacheControl().toString();
            if (!NetWorkUtils.isNetConnected(BaseApplication.getAppContext())) {
                request = request.newBuilder()
                        .cacheControl(TextUtils.isEmpty(cacheControl) ? CacheControl.FORCE_NETWORK : CacheControl.FORCE_CACHE)
                        .build();
            }
            Response originalResponse = chain.proceed(request);
            if (NetWorkUtils.isNetConnected(BaseApplication.getAppContext())) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置
                return originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)
                        .removeHeader("Pragma")
                        .build();
            } else {
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + CACHE_STALE_SEC)
                        .removeHeader("Pragma")
                        .build();
            }
        }
    };
}