package com.example.markwang.inspire;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PathMeasure;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.hitomi.cmlibrary.OnMenuSelectedListener;
import com.hitomi.cmlibrary.OnMenuStatusChangeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class CircleFoatingMenu extends View {

    private int partSize;
    private int iconSize;
    private float circleMenuRadius;
    private int itemNum;
    private float itemMenuRadius;
    private float fraction;
    private float rFraction;
    private float pathLength;
    private int mainMenuColor;
    private Drawable openMenuIcon;
    private Drawable closeMenuIcon;
    private List<Integer> subMenuColorList;
    private List<Drawable> subMenuDrawableList;
    private List<RectF> menuRectFList;
    private int centerX;
    private int centerY;
    private int clickIndex;
    private int rotateAngle;
    private int itemIconSize;
    private int pressedColor;
    private int status;
    private boolean pressed;
    private Paint oPaint;
    private Paint cPaint;
    private Paint sPaint;
    private PathMeasure pathMeasure;
    private Path path;
    private Path dstPath;
    private OnMenuSelectedListener onMenuSelectedListener;
    private OnMenuStatusChangeListener onMenuStatusChangeListener;

    private int screenWidth;
    private int screenHeight;
    private int screenWidthHalf;
    private int statusBarHeight;
    private int virtualBarHeight;

    private int downX;
    private int downY;
    private int upX;
    private int upY;
    private int lastX;
    private int lastY;

    private boolean isDrag;
    public CircleFoatingMenu(Context context) {
        this(context, (AttributeSet) null);
    }

    public CircleFoatingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleFoatingMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.status = 16;
        this.init();
    }

    private void init() {
        this.initTool();
        this.mainMenuColor = Color.parseColor("#CDCDCD");
        this.subMenuColorList = new ArrayList();
        this.subMenuDrawableList = new ArrayList();
        this.menuRectFList = new ArrayList();

        this.screenWidth = ScreenUtils.getScreenWidth(getContext());
        this.screenWidthHalf = screenWidth / 2;
        this.screenHeight = ScreenUtils.getScreenHeight(getContext());
        this.statusBarHeight = ScreenUtils.getStatusBarHeight(getContext());
        this.virtualBarHeight = ScreenUtils.getVirtualBarHeight(getContext());

    }

    private void initTool() {
        this.oPaint = new Paint(1);
        this.oPaint.setStyle(Style.FILL_AND_STROKE);
        this.cPaint = new Paint(1);
        this.cPaint.setStyle(Style.STROKE);
        this.cPaint.setStrokeCap(Cap.ROUND);
        this.sPaint = new Paint(1);
        this.sPaint.setStyle(Style.FILL);
        this.path = new Path();
        this.dstPath = new Path();
        this.pathMeasure = new PathMeasure();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int measureWidthSize = MeasureSpec.getSize(widthMeasureSpec);
        int measureHeightSize = MeasureSpec.getSize(heightMeasureSpec);

        measureWidthSize = (widthMode == MeasureSpec.AT_MOST) ? this.dip2px(20.0F) * 10 : measureWidthSize;
        measureHeightSize = (heightMode == MeasureSpec.AT_MOST) ? this.dip2px(20.0F) * 10 : measureHeightSize;

        this.setMeasuredDimension(measureWidthSize, measureHeightSize);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int minSize = Math.min(this.getMeasuredWidth(), this.getMeasuredHeight());
        this.partSize = minSize / 10;
        this.iconSize = this.partSize * 4 / 5;
        this.circleMenuRadius = (float) (this.partSize * 3);
        this.centerX = this.getMeasuredWidth() / 2;
        this.centerY = this.getMeasuredHeight() / 2;
        this.resetMainDrawableBounds();
        this.path.addCircle((float) this.centerX, (float) this.centerY, this.circleMenuRadius, Direction.CW);
        this.pathMeasure.setPath(this.path, true);
        this.pathLength = this.pathMeasure.getLength();
        RectF mainMenuRectF = new RectF((float) (this.centerX - this.partSize), (float) (this.centerY - this.partSize), (float) (this.centerX + this.partSize), (float) (this.centerY + this.partSize));
        this.menuRectFList.add(mainMenuRectF);
    }

    protected void onDraw(Canvas canvas) {
        switch (this.status) {
            case 1:
                this.drawMainMenu(canvas);
                this.drawSubMenu(canvas);
                break;
            case 2://draw main and sub circle
                this.drawMainMenu(canvas);
                this.drawSubMenu(canvas);
                break;
            case 4:
                this.drawMainMenu(canvas);
                this.drawSubMenu(canvas);
                this.drawCircleMenu(canvas);
                break;
            case 8:
                this.drawMainMenu(canvas);
                this.drawCircleMenu(canvas);
                break;
            case 16://only draw main menu circle
                this.drawMainMenu(canvas);
                break;
            case 32:
                this.drawMainMenu(canvas);
                this.drawSubMenu(canvas);
        }

    }

    /*
    while pressing the subCircle,draw circle around the path.
     */
    private void drawCircleMenu(Canvas canvas) {
        if (this.status == 4) {
            this.drawCirclePath(canvas);
            this.drawCircleIcon(canvas);
        } else {
            this.cPaint.setStrokeWidth((float) (this.partSize * 2) + (float) this.partSize * 0.5F * this.fraction);
            this.cPaint.setColor(this.calcAlphaColor(this.getClickMenuColor(), true));
            canvas.drawCircle((float) this.centerX, (float) this.centerY, this.circleMenuRadius + (float) this.partSize * 0.5F * this.fraction, this.cPaint);
        }

    }

    private int getClickMenuColor() {
        return this.clickIndex == 0 ? this.mainMenuColor : (Integer) this.subMenuColorList.get(this.clickIndex - 1);
    }

    private void drawCircleIcon(Canvas canvas) {
        canvas.save();
        Drawable selDrawable = (Drawable) this.subMenuDrawableList.get(this.clickIndex - 1);
        if (selDrawable != null) {
            int startAngle = (this.clickIndex - 1) * (360 / this.itemNum);
            int endAngle = 360 + startAngle;
            int itemX = (int) ((double) this.centerX + Math.sin(Math.toRadians((double) ((float) (endAngle - startAngle) * this.fraction + (float) startAngle))) * (double) this.circleMenuRadius);
            int itemY = (int) ((double) this.centerY - Math.cos(Math.toRadians((double) ((float) (endAngle - startAngle) * this.fraction + (float) startAngle))) * (double) this.circleMenuRadius);
            canvas.rotate(360.0F * this.fraction, (float) itemX, (float) itemY);
            selDrawable.setBounds(itemX - this.iconSize / 2, itemY - this.iconSize / 2, itemX + this.iconSize / 2, itemY + this.iconSize / 2);
            selDrawable.draw(canvas);
            canvas.restore();
        }
    }

    private void drawCirclePath(Canvas canvas) {
        canvas.save();
        canvas.rotate((float) this.rotateAngle, (float) this.centerX, (float) this.centerY);
        this.dstPath.reset();
        this.dstPath.lineTo(0.0F, 0.0F);
        this.pathMeasure.getSegment(0.0F, this.pathLength * this.fraction, this.dstPath, true);
        this.cPaint.setStrokeWidth((float) (this.partSize * 2));
        this.cPaint.setColor(this.getClickMenuColor());
        canvas.drawPath(this.dstPath, this.cPaint);
        canvas.restore();
    }

    private void drawSubMenu(Canvas canvas) {
        float offsetRadius = 1.5F;

        for (int i = 0; i < this.itemNum; ++i) {
            int angle = i * (360 / this.itemNum);
            int itemX;
            int itemY;
            if (this.status == 1) {
                itemX = (int) ((double) this.centerX + Math.sin(Math.toRadians((double) angle)) * (double) (this.circleMenuRadius - (1.0F - this.fraction) * (float) this.partSize * 1.5F));
                itemY = (int) ((double) this.centerY - Math.cos(Math.toRadians((double) angle)) * (double) (this.circleMenuRadius - (1.0F - this.fraction) * (float) this.partSize * 1.5F));
                this.oPaint.setColor(this.calcAlphaColor((Integer) this.subMenuColorList.get(i), false));
                this.sPaint.setColor(this.calcAlphaColor((Integer) this.subMenuColorList.get(i), false));
            } else if (this.status == 32) {
                itemX = (int) ((double) this.centerX + Math.sin(Math.toRadians((double) angle)) * (double) (this.circleMenuRadius - this.fraction * (float) this.partSize * 1.5F));
                itemY = (int) ((double) this.centerY - Math.cos(Math.toRadians((double) angle)) * (double) (this.circleMenuRadius - this.fraction * (float) this.partSize * 1.5F));
                this.oPaint.setColor(this.calcAlphaColor((Integer) this.subMenuColorList.get(i), true));
                this.sPaint.setColor(this.calcAlphaColor((Integer) this.subMenuColorList.get(i), true));
            } else {
                itemX = (int) ((double) this.centerX + Math.sin(Math.toRadians((double) angle)) * (double) this.circleMenuRadius);
                itemY = (int) ((double) this.centerY - Math.cos(Math.toRadians((double) angle)) * (double) this.circleMenuRadius);
                this.oPaint.setColor((Integer) this.subMenuColorList.get(i));
                this.sPaint.setColor((Integer) this.subMenuColorList.get(i));
            }

            if (this.pressed && this.clickIndex - 1 == i) {
                this.oPaint.setColor(this.pressedColor);
            }

            this.drawMenuShadow(canvas, itemX, itemY, this.itemMenuRadius);
            canvas.drawCircle((float) itemX, (float) itemY, this.itemMenuRadius, this.oPaint);
            this.drawSubMenuIcon(canvas, itemX, itemY, i);
            RectF menuRectF = new RectF((float) (itemX - this.partSize), (float) (itemY - this.partSize), (float) (itemX + this.partSize), (float) (itemY + this.partSize));
            if (this.menuRectFList.size() - 1 > i) {
                this.menuRectFList.remove(i + 1);
            }

            this.menuRectFList.add(i + 1, menuRectF);
        }

    }

    private void drawSubMenuIcon(Canvas canvas, int centerX, int centerY, int index) {
        int diff;
        if (this.status != 1 && this.status != 32) {
            diff = this.iconSize / 2;
        } else {
            diff = this.itemIconSize / 2;
        }

        this.resetBoundsAndDrawIcon(canvas, (Drawable) this.subMenuDrawableList.get(index), centerX, centerY, diff);
    }

    private void resetBoundsAndDrawIcon(Canvas canvas, Drawable drawable, int centerX, int centerY, int diff) {
        if (drawable != null) {
            drawable.setBounds(centerX - diff, centerY - diff, centerX + diff, centerY + diff);
            drawable.draw(canvas);
        }
    }

    private void drawMainMenu(Canvas canvas) {
        float centerMenuRadius;
        float realFraction;
        if (this.status == 4) {
            realFraction = 1.0F - this.fraction * 2.0F == 0.0F ? 0.0F : 1.0F - this.fraction * 2.0F;
            centerMenuRadius = (float) this.partSize * realFraction;
        } else if (this.status == 8) {
            realFraction = this.fraction * 4.0F >= 1.0F ? 1.0F : this.fraction * 4.0F;
            centerMenuRadius = (float) this.partSize * realFraction;
        } else if (this.status != 16 && this.status != 32) {
            centerMenuRadius = (float) this.partSize;
        } else {
            centerMenuRadius = (float) this.partSize;
        }

        if (this.status != 1 && this.status != 2 && this.status != 4) {
            if (this.pressed && this.clickIndex == 0) {
                this.oPaint.setColor(this.pressedColor);
            } else {
                this.oPaint.setColor(this.mainMenuColor);
                this.sPaint.setColor(this.mainMenuColor);
            }
        } else {
            this.oPaint.setColor(this.calcPressedEffectColor(0, 0.5F));
        }

        this.drawMenuShadow(canvas, this.centerX, this.centerY, centerMenuRadius);
        canvas.drawCircle((float) this.centerX, (float) this.centerY, centerMenuRadius, this.oPaint);
        this.drawMainMenuIcon(canvas);
    }

    private void drawMainMenuIcon(Canvas canvas) {
        canvas.save();
        switch (this.status) {
            case 1:
                canvas.rotate(45.0F * (this.fraction - 1.0F), (float) this.centerX, (float) this.centerY);
                this.resetBoundsAndDrawIcon(canvas, this.closeMenuIcon, this.centerX, this.centerY, this.iconSize / 2);
                break;
            case 2:
                this.resetBoundsAndDrawIcon(canvas, this.closeMenuIcon, this.centerX, this.centerY, this.iconSize / 2);
                break;
            case 4:
                this.resetBoundsAndDrawIcon(canvas, this.closeMenuIcon, this.centerX, this.centerY, this.itemIconSize / 2);
                break;
            case 8:
                canvas.rotate(90.0F * (this.rFraction - 1.0F), (float) this.centerX, (float) this.centerY);
                this.resetBoundsAndDrawIcon(canvas, this.openMenuIcon, this.centerX, this.centerY, this.itemIconSize / 2);
                break;
            case 16:
                if (this.openMenuIcon != null) {
                    this.openMenuIcon.draw(canvas);
                }
                break;
            case 32:
                canvas.rotate(-45.0F * this.fraction, (float) this.centerX, (float) this.centerY);
                if (this.closeMenuIcon != null) {
                    this.closeMenuIcon.draw(canvas);
                }
        }

        canvas.restore();
    }

    private void drawMenuShadow(Canvas canvas, int centerX, int centerY, float radius) {
        if (radius + 5.0F > 0.0F) {
            this.sPaint.setShader(new RadialGradient((float) centerX, (float) centerY, radius + 5.0F, 0, 0, TileMode.CLAMP));
            canvas.drawCircle((float) centerX, (float) centerY, radius + 5.0F, this.sPaint);
        }

    }


    public boolean onTouchEvent(MotionEvent event) {
        int rawX=(int)event.getRawX();
        int rawY=(int)event.getRawY();

        int dx=rawX-lastX;
        int dy=rawY-lastY;

        if (this.status != 4 && this.status != 8) {
            int index = this.clickWhichRectF(event.getX(), event.getY());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    this.pressed = true;
                    if(status==16){
                        getParent().requestDisallowInterceptTouchEvent(true);
                        this.downX=this.lastX = rawX;
                        this.downY=this.lastY = rawY;

                    }
                    if (index != -1) {
                        this.clickIndex = index;
                        this.updatePressEffect(index, this.pressed);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    Log.e("ACTION_UP","ACTION_UP");
                    this.pressed = false;
                    this.upX=this.lastX;
                    this.upY=this.lastY;
                    int distance=(int)Math.sqrt(Math.pow(downX-upX,2)+Math.pow(downY-upY,2));

                    if (index != -1) {
                        this.clickIndex = index;
                        this.updatePressEffect(index, this.pressed);
                    }

                    if (index == 0) {
                        if (this.status == 16 && distance<2) {
                            this.status = 1;
                            this.startOpenMenuAnima();
                        } else if (this.status == 2) {
                            this.status = 32;
                            this.startCancelMenuAnima();
                        }
                    } else if (this.status == 2 && index != -1) {
                        this.status = 4;
                        if (this.onMenuSelectedListener != null) {
                            this.onMenuSelectedListener.onMenuSelected(index - 1);
                        }

                        this.rotateAngle = this.clickIndex * (360 / this.itemNum) - 360 / this.itemNum - 90;
                        this.startCloseMeunAnima();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(status==16){
                        float x = getX() + dx;
                        float y = getY() + dy;

                        x = x < 0 ? 0 : x > screenWidth - getWidth() ? screenWidth - getWidth() : x;
                        if (y<0){
                            y=0;
                        }
                        if (y>screenHeight-statusBarHeight-getHeight()){
                            y=screenHeight-statusBarHeight-getHeight();
                        }
                        setX(x);
                        setY(y);
                        lastX = rawX;
                        lastY = rawY;
                        this.invalidate();
                    }
                    if (index == -1) {
                        this.pressed = false;
                        this.invalidate();
                    }
            }
            return true;
        } else {
            return true;
        }
    }

    private void updatePressEffect(int menuIndex, boolean press) {
        if (press) {
            this.pressedColor = this.calcPressedEffectColor(menuIndex, 0.15F);
        }

        this.invalidate();
    }

    private int calcPressedEffectColor(int menuIndex, float depth) {
        int color = menuIndex == 0 ? this.mainMenuColor : (Integer) this.subMenuColorList.get(menuIndex - 1);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.0F - depth;
        return Color.HSVToColor(hsv);
    }

    private int calcAlphaColor(int color, boolean reverse) {
        int alpha;
        if (reverse) {
            alpha = (int) (255.0F * (1.0F - this.fraction));
        } else {
            alpha = (int) (255.0F * this.fraction);
        }

        if (alpha >= 255) {
            alpha = 255;
        }

        if (alpha <= 0) {
            alpha = 0;
        }

        return ColorUtils.setAlphaComponent(color, alpha);
    }

    private void startOpenMenuAnima() {
        ValueAnimator openAnima = ValueAnimator.ofFloat(new float[]{1.0F, 100.0F});
        openAnima.setDuration(500L);
        openAnima.setInterpolator(new OvershootInterpolator());
        openAnima.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                CircleFoatingMenu.this.fraction = valueAnimator.getAnimatedFraction();
                CircleFoatingMenu.this.itemMenuRadius = CircleFoatingMenu.this.fraction * (float) CircleFoatingMenu.this.partSize;
                CircleFoatingMenu.this.itemIconSize = (int) (CircleFoatingMenu.this.fraction * (float) CircleFoatingMenu.this.iconSize);
                CircleFoatingMenu.this.invalidate();
            }
        });
        openAnima.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                CircleFoatingMenu.this.status = 2;
                if (CircleFoatingMenu.this.onMenuStatusChangeListener != null) {
                    CircleFoatingMenu.this.onMenuStatusChangeListener.onMenuOpened();
                }

            }
        });
        openAnima.start();
    }

    private void startCancelMenuAnima() {
        ValueAnimator cancelAnima = ValueAnimator.ofFloat(new float[]{1.0F, 100.0F});
        cancelAnima.setDuration(500L);
        cancelAnima.setInterpolator(new AnticipateInterpolator());
        cancelAnima.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                CircleFoatingMenu.this.fraction = valueAnimator.getAnimatedFraction();
                CircleFoatingMenu.this.itemMenuRadius = (1.0F - CircleFoatingMenu.this.fraction) * (float) CircleFoatingMenu.this.partSize;
                CircleFoatingMenu.this.itemIconSize = (int) ((1.0F - CircleFoatingMenu.this.fraction) * (float) CircleFoatingMenu.this.iconSize);
                CircleFoatingMenu.this.invalidate();
            }
        });
        cancelAnima.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                CircleFoatingMenu.this.status = 16;
                if (CircleFoatingMenu.this.onMenuStatusChangeListener != null) {
                    CircleFoatingMenu.this.onMenuStatusChangeListener.onMenuClosed();
                }

            }
        });
        cancelAnima.start();
    }

    private void startCloseMeunAnima() {
        ValueAnimator aroundAnima = ValueAnimator.ofFloat(new float[]{1.0F, 100.0F});
        aroundAnima.setDuration(600L);
        aroundAnima.setInterpolator(new AccelerateDecelerateInterpolator());
        aroundAnima.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                CircleFoatingMenu.this.fraction = valueAnimator.getAnimatedFraction();
                float animaFraction = CircleFoatingMenu.this.fraction * 2.0F >= 1.0F ? 1.0F : CircleFoatingMenu.this.fraction * 2.0F;
                CircleFoatingMenu.this.itemIconSize = (int) ((1.0F - animaFraction) * (float) CircleFoatingMenu.this.iconSize);
                CircleFoatingMenu.this.invalidate();
            }
        });
        aroundAnima.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                CircleFoatingMenu.this.status = 8;
            }
        });
        ValueAnimator spreadAnima = ValueAnimator.ofFloat(new float[]{1.0F, 100.0F});
        spreadAnima.setInterpolator(new LinearInterpolator());
        spreadAnima.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                CircleFoatingMenu.this.fraction = valueAnimator.getAnimatedFraction();
            }
        });
        ValueAnimator rotateAnima = ValueAnimator.ofFloat(new float[]{1.0F, 100.0F});
        rotateAnima.setInterpolator(new OvershootInterpolator());
        rotateAnima.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                CircleFoatingMenu.this.rFraction = valueAnimator.getAnimatedFraction();
                CircleFoatingMenu.this.itemIconSize = (int) (CircleFoatingMenu.this.rFraction * (float) CircleFoatingMenu.this.iconSize);
                CircleFoatingMenu.this.invalidate();
            }
        });
        AnimatorSet closeAnimaSet = new AnimatorSet();
        closeAnimaSet.setDuration(500L);
        closeAnimaSet.play(spreadAnima).with(rotateAnima);
        closeAnimaSet.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                CircleFoatingMenu.this.status = 16;
                if (CircleFoatingMenu.this.onMenuStatusChangeListener != null) {
                    CircleFoatingMenu.this.onMenuStatusChangeListener.onMenuClosed();
                }

            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(aroundAnima).before(closeAnimaSet);
        animatorSet.start();
    }

    private int clickWhichRectF(float x, float y) {
        int which = -1;
        Iterator var4 = this.menuRectFList.iterator();

        while (var4.hasNext()) {
            RectF rectF = (RectF) var4.next();
            if (rectF.contains(x, y)) {
                which = this.menuRectFList.indexOf(rectF);
                break;
            }
        }

        return which;
    }

    private Drawable convertDrawable(int iconRes) {
        return this.getResources().getDrawable(iconRes);
    }

    private Drawable convertBitmap(Bitmap bitmap) {
        return new BitmapDrawable(this.getResources(), bitmap);
    }

    private void resetMainDrawableBounds() {
        this.openMenuIcon.setBounds(this.centerX - this.iconSize / 2, this.centerY - this.iconSize / 2, this.centerX + this.iconSize / 2, this.centerY + this.iconSize / 2);
        this.closeMenuIcon.setBounds(this.centerX - this.iconSize / 2, this.centerY - this.iconSize / 2, this.centerX + this.iconSize / 2, this.centerY + this.iconSize / 2);
    }

    public CircleFoatingMenu setMainMenu(int mainMenuColor, int openMenuRes, int closeMenuRes) {
        this.openMenuIcon = this.convertDrawable(openMenuRes);
        this.closeMenuIcon = this.convertDrawable(closeMenuRes);
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    public CircleFoatingMenu setMainMenu(int mainMenuColor, Bitmap openMenuBitmap, Bitmap closeMenuBitmap) {
        this.openMenuIcon = this.convertBitmap(openMenuBitmap);
        this.closeMenuIcon = this.convertBitmap(closeMenuBitmap);
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    public CircleFoatingMenu setMainMenu(int mainMenuColor, Drawable openMenuDrawable, Drawable closeMenuDrawable) {
        this.openMenuIcon = openMenuDrawable;
        this.closeMenuIcon = closeMenuDrawable;
        this.mainMenuColor = mainMenuColor;
        return this;
    }

    public CircleFoatingMenu addSubMenu(int menuColor, int menuRes) {
        if (this.subMenuColorList.size() < 8 && this.subMenuDrawableList.size() < 8) {
            this.subMenuColorList.add(menuColor);
            this.subMenuDrawableList.add(this.convertDrawable(menuRes));
            this.itemNum = Math.min(this.subMenuColorList.size(), this.subMenuDrawableList.size());
        }

        return this;
    }

    public CircleFoatingMenu addSubMenu(int menuColor, Bitmap menuBitmap) {
        if (this.subMenuColorList.size() < 8 && this.subMenuDrawableList.size() < 8) {
            this.subMenuColorList.add(menuColor);
            this.subMenuDrawableList.add(this.convertBitmap(menuBitmap));
            this.itemNum = Math.min(this.subMenuColorList.size(), this.subMenuDrawableList.size());
        }

        return this;
    }

    public CircleFoatingMenu addSubMenu(int menuColor, Drawable menuDrawable) {
        if (this.subMenuColorList.size() < 8 && this.subMenuDrawableList.size() < 8) {
            this.subMenuColorList.add(menuColor);
            this.subMenuDrawableList.add(menuDrawable);
            this.itemNum = Math.min(this.subMenuColorList.size(), this.subMenuDrawableList.size());
        }

        return this;
    }

    public void openMenu() {
        if (this.status == 16) {
            this.status = 1;
            this.startOpenMenuAnima();
        }

    }

    public void closeMenu() {
        if (this.status == 2) {
            this.status = 32;
            this.startCancelMenuAnima();
        }

    }

    public boolean isOpened() {
        return this.status == 2;
    }

    public CircleFoatingMenu setOnMenuSelectedListener(OnMenuSelectedListener listener) {
        this.onMenuSelectedListener = listener;
        return this;
    }

    public CircleFoatingMenu setOnMenuStatusChangeListener(OnMenuStatusChangeListener listener) {
        this.onMenuStatusChangeListener = listener;
        return this;
    }

    private int dip2px(float dpValue) {
        float scale = this.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
    }
}
