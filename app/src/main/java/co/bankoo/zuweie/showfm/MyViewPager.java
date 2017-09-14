package co.bankoo.zuweie.showfm;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {
    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //重载此函数
    // 当横向滑动距离大于纵向滑动距离，viewpager截取event自己处理。然后不让父控件处理
    // 相反，则让父控件处理，自己不处理。

    @Override
    public boolean dispatchTouchEvent (MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                last_dx = event.getX();
                last_dy = event.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX()-last_dx) > Math.abs(event.getY()-last_dy)){
                    getParent().requestDisallowInterceptTouchEvent(true);
                }else{
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    float last_dx;
    float last_dy;
}