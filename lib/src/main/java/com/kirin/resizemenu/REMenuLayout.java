package com.kirin.resizemenu;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * com.lw.usadirect.ui
 * REMenuLayout
 * UsaMobile
 * <p/>
 * Created by alan on 22/1/15.
 * Copyright (c) 2015年 Alan Lam. All rights reserved.
 */
public class REMenuLayout extends RelativeLayout {

    private final double AUTO_OPEN_SPEED_LIMIT = 800.0;
    private int mDraggingState = 0;
    private View mFrontView;
    private ViewDragHelper mDragHelper;
    private int mDraggingBorder;
    private int mHRange;
    private boolean mIsOpen;
    private OnMenuListener mOnMenuListener;

    public void setOnMenuListener(OnMenuListener l){
        mOnMenuListener = l;
    }


    public class DragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public void onViewDragStateChanged(int state) {
            if (state == mDraggingState) { // no change
                return;
            }
            if ((mDraggingState == ViewDragHelper.STATE_DRAGGING || mDraggingState == ViewDragHelper.STATE_SETTLING) &&
                    state == ViewDragHelper.STATE_IDLE) {
                // the view stopped from moving.

                if (mDraggingBorder == 0) {
                    if(mOnMenuListener  != null) {
                        mOnMenuListener.onStopDRaggingToClosed();
                    }
                } else if (mDraggingBorder == mHRange) {
                    mIsOpen = true;
                }
            }
            if (state == ViewDragHelper.STATE_DRAGGING) {
                if(mOnMenuListener != null) {
                    mOnMenuListener.onStartDragging();
                }
            }
            mDraggingState = state;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mDraggingBorder = left;

            float mDragOffset = (float) left / mHRange ;
//            mFrontView.setPivotX(mFrontView.getWidth());
//            mFrontView.setPivotY(mFrontView.getHeight());
            mFrontView.setScaleX(1 - mDragOffset / 7);
            mFrontView.setScaleY(1 - mDragOffset / 7);

//            requestLayout();


        }

        public int getViewVerticalDragRange(View child) {
            return mHRange;
        }


        @Override
        public boolean tryCaptureView(View view, int i) {
            return (view.getId() == getChildAt(1).getId());
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int rightBound = mHRange;
            final int leftBound = getPaddingLeft();
            return Math.min(Math.max(left, leftBound), rightBound);
        }


        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final float rangeToCheck = mHRange;
            if (mDraggingBorder == 0) {
                mIsOpen = false;
                return;
            }
            if (mDraggingBorder == rangeToCheck) {
                mIsOpen = true;
                return;
            }
            boolean settleToOpen = false;
            if (yvel > AUTO_OPEN_SPEED_LIMIT) { // speed has priority over position
                settleToOpen = true;
            } else if (yvel < -AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = false;
            } else if (mDraggingBorder > rangeToCheck / 2) {
                settleToOpen = true;
            } else if (mDraggingBorder < rangeToCheck / 2) {
                settleToOpen = false;
            }

            final int settleDestX = settleToOpen ? mHRange : 0;

            if(mDragHelper.settleCapturedViewAt(settleDestX, 0)) {
                ViewCompat.postInvalidateOnAnimation(REMenuLayout.this);
            }
        }
    }

    public REMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIsOpen = false;
    }

    @Override
    protected void onFinishInflate() {
        mFrontView  = getChildAt(1);
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
        mDragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        mIsOpen = false;
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mHRange = (int) (w * 0.8);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public int getBackgroundWidth(){
        return mHRange;
    }

    private boolean isQueenTarget(MotionEvent event) {
        int[] queenLocation = new int[2];
        mFrontView.getLocationOnScreen(queenLocation);
        int upperLimit = queenLocation[1] + mFrontView.getMeasuredHeight();
        int lowerLimit = queenLocation[1];
        int y = (int) event.getRawY();
        return (y > lowerLimit && y < upperLimit);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isQueenTarget(event) && mDragHelper.shouldInterceptTouchEvent(event)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isQueenTarget(event) || isMoving()) {
            mDragHelper.processTouchEvent(event);
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public void computeScroll() { // needed for automatic settling.
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public boolean isMoving() {
        return (mDraggingState == ViewDragHelper.STATE_DRAGGING ||
                mDraggingState == ViewDragHelper.STATE_SETTLING);
    }

    public boolean isOpen() {
        return mIsOpen;
    }
}
