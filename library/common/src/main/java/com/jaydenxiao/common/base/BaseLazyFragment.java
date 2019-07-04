package com.jaydenxiao.common.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

/**
 * author : Conan
 * time   : 2019/6/10
 * desc   : 懒加载Fragment
 */
public abstract class BaseLazyFragment<T extends BasePresenter, E extends BaseModel> extends BaseFragment<T, E> {
    private boolean isVisible;
    private boolean isPrepared;
    private boolean isFirst = true;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            lazyLoad();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isPrepared = true;
        lazyLoad();
    }

    private void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        _lazyLoad(isFirst);
        isFirst = false;
    }

    protected abstract void _lazyLoad(boolean isFirst);

    //do something
    protected void onInvisible() {
    }
}
