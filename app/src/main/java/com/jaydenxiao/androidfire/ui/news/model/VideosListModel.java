package com.jaydenxiao.androidfire.ui.news.model;

import com.jaydenxiao.androidfire.api.NetEastService;
import com.jaydenxiao.androidfire.bean.VideoData;
import com.jaydenxiao.androidfire.ui.news.contract.VideosListContract;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.jaydenxiao.common.commonutils.TimeUtil;
import com.jaydenxiao.common.http.Api;

import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;

/**
 * des:视频列表model
 * Created by xsf
 * on 2016.09.14:54
 */
public class VideosListModel implements VideosListContract.Model {

    @Override
    public Observable<List<VideoData>> getVideosListData(final String type, int startPage) {
        return Api.getService(NetEastService.class).getVideoList(Api.getCacheControl(), type, startPage)
                .flatMap((Func1<Map<String, List<VideoData>>, Observable<VideoData>>) map -> Observable.from(map.get(type)))
                //转化时间
                .map(videoData -> {
                    String ptime = TimeUtil.formatDate(videoData.getPtime());
                    videoData.setPtime(ptime);
                    return videoData;
                })
                .distinct()//去重
                .toSortedList((videoData, videoData2) -> videoData2.getPtime().compareTo(videoData.getPtime()))
                //声明线程调度
                .compose(RxSchedulers.<List<VideoData>>io_main());
    }
}
