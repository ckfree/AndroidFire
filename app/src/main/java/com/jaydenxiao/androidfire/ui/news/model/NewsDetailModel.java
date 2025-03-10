package com.jaydenxiao.androidfire.ui.news.model;

import com.jaydenxiao.androidfire.api.NetEastService;
import com.jaydenxiao.androidfire.bean.NewsDetail;
import com.jaydenxiao.androidfire.ui.news.contract.NewsDetailContract;
import com.jaydenxiao.common.baserx.RxSchedulers;
import com.jaydenxiao.common.http.Api;

import java.util.List;

import rx.Observable;

/**
 * des:新闻详情
 * Created by xsf
 * on 2016.09.17:09
 */
public class NewsDetailModel implements NewsDetailContract.Model {

    @Override
    public Observable<NewsDetail> getOneNewsData(final String postId) {

        return Api.getService(NetEastService.class).getNewDetail(Api.getCacheControl(), postId)
                .map(map -> {
                    NewsDetail newsDetail = map.get(postId);
                    changeNewsDetail(newsDetail);
                    return newsDetail;
                }).compose(RxSchedulers.io_main());
    }

    private void changeNewsDetail(NewsDetail newsDetail) {
        List<NewsDetail.ImgBean> imgSrcs = newsDetail.getImg();
        if (isChange(imgSrcs)) {
            String newsBody = newsDetail.getBody();
            newsBody = changeNewsBody(imgSrcs, newsBody);
            newsDetail.setBody(newsBody);
        }
    }

    private boolean isChange(List<NewsDetail.ImgBean> imgSrcs) {
        return imgSrcs != null && imgSrcs.size() >= 2;
    }

    private String changeNewsBody(List<NewsDetail.ImgBean> imgSrcs, String newsBody) {
        for (int i = 0; i < imgSrcs.size(); i++) {
            String oldChars = "<!--IMG#" + i + "-->";
            String newChars;
            if (i == 0) {
                newChars = "";
            } else {
                newChars = "<img src=\"" + imgSrcs.get(i).getSrc() + "\" />";
            }
            newsBody = newsBody.replace(oldChars, newChars);

        }
        return newsBody;
    }
}
