package com.andjojo.itshack;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


public class RideCanvas extends View {

    Step currentStep;
    Float UserPos=0f;
    Float startPunkt=20f;

    public RideCanvas(Context context) {
        super(context);
        init(context);
    }

    public RideCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RideCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        //do stuff that was in your original constructor...
    }

    public void setStep(Step step){
        currentStep = step;
    }

    public void setUserPos(Float percent){
       UserPos = percent;
    }

    public void draw(){
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(242,242,242));
        float dip = 20f;
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        paint.setTextSize(40);
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRoundRect(new RectF(0, 0, width, height), px, px, paint);

        Paint RidePaint = new Paint();
        RidePaint.setColor(Color.rgb(34,68,89));
        RidePaint.setStrokeWidth(5);
        RidePaint.setTextSize(40);
        RidePaint.setAntiAlias(true);


        if(currentStep!=null) {
            if (!currentStep.getTransportType().equals("Laufen")) {
                Paint bgPaint = new Paint();
                bgPaint.setColor(Color.WHITE);
                canvas.drawRoundRect(new RectF(0, 0, width, height), px, px, bgPaint);
                Float lineWidth=18*width/20;
                startPunkt = width/20;

                canvas.drawLine(startPunkt+0,2*height/3+20,startPunkt+lineWidth,2*height/3+20,RidePaint);

                //canvas.drawCircle(startPunkt+lineWidth*UserPos,3*height/3-50,20,RidePaint);
                Bitmap MyBitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.smile);

                canvas.drawBitmap(MyBitmap, null,new Rect((int)(startPunkt+lineWidth*UserPos-60),(int)(3*height/3-120),(int)(startPunkt+lineWidth*UserPos+60),(int)(3*height/3)), null);

                for (int i=0;i<currentStep.getStationNames().length;i++){
                    canvas.drawCircle(startPunkt+lineWidth*currentStep.getStationPercentage()[i],2*height/3+20,20,RidePaint);
                    canvas.rotate(-90, startPunkt+lineWidth*currentStep.getStationPercentage()[i],2*height/3);
                    canvas.drawText(currentStep.getStationNames()[i],startPunkt+lineWidth*currentStep.getStationPercentage()[i],2*height/3,RidePaint);
                    canvas.rotate(90, startPunkt+lineWidth*currentStep.getStationPercentage()[i],2*height/3);
                }

            }
        }


    }


}
