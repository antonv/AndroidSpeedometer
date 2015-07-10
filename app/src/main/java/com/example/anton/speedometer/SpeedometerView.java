package com.example.anton.speedometer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

public class SpeedometerView extends View {

    private static class VelocityPainter
    {
        private final static int   gradientSize = 400;
        private final static float decreaseArcAngle = 1.28f;
        private final static float maxArcAngle = 4f;

        private View     mParentView = null;
        private Drawable mBgDrawable = null;

        private Paint mArrowPaint = null;
        private Paint mGradientPaint = null;

        private RectF mGradientSize = new RectF();

        private double mOldAarrowAngle = 0;
        private float  mArcAngle = 0;

        public VelocityPainter(View parentView, Drawable bgImage) {
            mBgDrawable = bgImage;
            mParentView = parentView;

            init();
        }

        /**
         * init painter tools
         *
         */
        private void init() {
            mArrowPaint = new Paint();
            mArrowPaint.setColor(Color.RED);
            mArrowPaint.setStrokeWidth(5);
            mArrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);

            mGradientPaint = new Paint();
            mGradientPaint.setStrokeWidth(1);
            mGradientPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }

        /**
         * Draw control scene
         *
         */
        public  void draw(Canvas canvas, VelocityAnimator animator) {
            int paddingLeft   = mParentView.getPaddingLeft();
            int paddingRight  = mParentView.getPaddingRight();
            int paddingTop    = mParentView.getPaddingTop();
            int paddingBottom = mParentView.getPaddingBottom();

            int contentWidth  = mParentView.getWidth() - paddingLeft - paddingRight;
            int contentHeight = mParentView.getHeight() - paddingTop - paddingBottom;

            int centerX = contentWidth / 2;
            int centerY = contentHeight / 2;

            int arrowLength = (int)((contentWidth / 2) * 0.9f);

            if (mBgDrawable != null) {
                mBgDrawable.setBounds(paddingLeft, paddingTop,
                        paddingLeft + contentWidth, paddingTop + contentHeight);
                mBgDrawable.draw(canvas);
            }

            mGradientPaint.setShader(new RadialGradient(contentWidth / 2, contentWidth / 2, gradientSize,
                    Color.RED, Color.TRANSPARENT, Shader.TileMode.MIRROR));

            mGradientSize.set(contentWidth / 2 - gradientSize, contentHeight / 2 - gradientSize,
                    contentWidth / 2 + gradientSize, contentHeight / 2 + gradientSize);

            invalidateSpeedArrow(canvas, centerX, centerY, arrowLength, animator);
        }

        /**
         * Draw and animate speed arrow
         *
         */
        private void invalidateSpeedArrow(Canvas canvas, int centerX, int centerY, int arrowLength, VelocityAnimator animator) {

            double arrowAngle = animator.makeFrameAngle();
            double radAngle   = Math.toRadians(arrowAngle);
            double currAngle  = arrowAngle - animator.getMinAngle();

            double x = Math.cos(radAngle) * arrowLength + centerX;
            double y = Math.sin(radAngle) * arrowLength + centerY;

            canvas.drawLine((float) centerX, (float) centerY, (float) x, (float) y, mArrowPaint);

            if (arrowAngle == mOldAarrowAngle && arrowAngle > animator.getMinAngle()) {
                mArcAngle = mArcAngle > 0 ? mArcAngle - decreaseArcAngle : 0f;
            }
            else {
                mArcAngle = currAngle < maxArcAngle ? (float)currAngle : maxArcAngle;
            }

            canvas.drawArc(mGradientSize, (float)arrowAngle, -1f * mArcAngle, true, mGradientPaint);

            canvas.drawArc(mGradientSize, 0f, 360f, true, mGradientPaint);

            mOldAarrowAngle = arrowAngle;
        }

        public int getWidth() {
            return mBgDrawable.getMinimumWidth();
        }

        public int getHeight() {
            return mBgDrawable.getMinimumHeight();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    private static class VelocityAnimator
    {
        private final static double maxFluctuations = 0.1;
        private final static double animTime = 2500.0;

        private double mCurrVelocity = 0;
        private double mFutureVelocity = 0;

        private double mMinAngle = 0;
        private double mMinVelocity = 0;
        private double mMaxAngle = 0;
        private double mMaxVelocity = 0;

        private double mFinishAnimTime = 0;

        private Interpolator mAnimInterpolator = null;

        public VelocityAnimator(double minAngle, double minVelocity, double maxAngle, double maxVelocity, Interpolator interpolator) {
            mMinAngle    = minAngle;
            mMinVelocity = minVelocity;
            mMaxAngle    = maxAngle;
            mMaxVelocity = maxVelocity;
            mAnimInterpolator = interpolator;
        }

        /**
         * get min angle
         *
         */
        public double getMinAngle() {
            return mMinAngle;
        }

        /**
         * get max angle
         *
         */
        public double getMaxAngle() {
            return mMaxAngle;
        }

        /**
         * get current velocity on the speedometer
         *
         */
        public double getCurrVelocity() {
            return mCurrVelocity;
        }

        /**
         * setter for velocity variable from external objects
         *
         */
        public synchronized void setFutureVelocity(int futureVelocity) {
            mCurrVelocity   = 0;
            mFutureVelocity = futureVelocity > mMaxVelocity ? mMaxVelocity : futureVelocity;

            mFinishAnimTime = (double)System.currentTimeMillis() + animTime;
        }

        /**
         * calculate velocity on current timestamp
         *
         */
        private double getAccelerateVelocity(double currVelocity) {

            long currTime = (long)System.currentTimeMillis();
            double timeToFinishAnim = mFinishAnimTime - (double)currTime;

            if (currTime > mFinishAnimTime)
                return mCurrVelocity;

            return mFutureVelocity - mAnimInterpolator.getInterpolation((float)(timeToFinishAnim / animTime)) * mFutureVelocity;
        }

        private double getMaxVelocityWithFluctuations(double currVelocity) {
            return currVelocity - maxFluctuations;
        }

        /**
         * calculate arrow angle by current velocity
         *
         */
        public synchronized float makeFrameAngle()
        {
            if (mCurrVelocity < 0)
                return (float)mMinAngle;

            if (mCurrVelocity < mFutureVelocity)
                mCurrVelocity = getAccelerateVelocity(mCurrVelocity);
            else
                mCurrVelocity = getMaxVelocityWithFluctuations(mCurrVelocity);

            double anglePercent = mCurrVelocity / (mMaxVelocity - mMinVelocity);

            double mCurrAngle = mMinAngle + (mMaxAngle - mMinAngle) * anglePercent;

            return (float)mCurrAngle;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    private VelocityPainter mDrawer = null;
    private VelocityAnimator mAnimator = null;

    private final static float animFactor = 0.8f;

    private final static double angleMax = 340;
    private final static double angleMin = 95;

    private final static double velocityMin = 0;
    private final static double velocityMax = 130;

    public SpeedometerView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpeedometerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        Drawable bgDrawable = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            bgDrawable = getResources().getDrawable(R.drawable.speedbg, null);
        else
            bgDrawable = getResources().getDrawable(R.drawable.speedbg);

        mAnimator = new VelocityAnimator(angleMin, velocityMin, angleMax, velocityMax,
                new AccelerateInterpolator(animFactor));

        mDrawer   = new VelocityPainter(this, bgDrawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int viewDimension = Math.max(mDrawer.getWidth(), mDrawer.getHeight());

        this.setMeasuredDimension(viewDimension, viewDimension);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getVisibility() == VISIBLE) {
            mDrawer.draw(canvas, mAnimator);
        }
    }

    public void setVelocity(int kms) {
        if (mAnimator != null)
            mAnimator.setFutureVelocity(kms);
    }

    public double getVelocity() {
        if (mAnimator != null)
            return mAnimator.getCurrVelocity();

        return 0;
    }
}
