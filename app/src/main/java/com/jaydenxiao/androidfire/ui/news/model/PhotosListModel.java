package com.jaydenxiao.androidfire.ui.news.model;

import com.jaydenxiao.androidfire.api.SinaPhotoService;
import com.jaydenxiao.androidfire.bean.PhotoGirl;
import com.jaydenxiao.androidfire.ui.news.contract.PhotoListContract;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.jaydenxiao.common.http.Api;

import java.util.List;

import rx.Observable;

/**
 * des:图片
 * Created by xsf
 * on 2016.09.12:02
 */
public class PhotosListModel implements PhotoListContract.Model {
    @Override
    public Observable<List<PhotoGirl>> getPhotosListData(int size, int page) {
        return Api.getService(SinaPhotoService.class)
                .getPhotoList(Api.getCacheControl(), size, page)
                .map(girlData -> girlData.getResults())
                .compose(RxSchedulers.<List<PhotoGirl>>io_main());
    }
}
