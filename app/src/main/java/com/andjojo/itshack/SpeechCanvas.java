package com.andjojo.itshack;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


public class SpeechCanvas extends View {

    float val=0;

    public SpeechCanvas(Context context) {
        super(context);
    }

    public SpeechCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public SpeechCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void onRmsChanged(Float val) {
        //do stuff that was in your original constructor...
        if (val >8 ){
            this.val = (float)(Math.exp(val))/500;
        }
        else{
            Handler handler = new Handler();
            final Runnable periodicUpdate = new Runnable (){
                int i = 0;
                @Override
                public void run() {
                    // scheduled another events to be in 10 seconds later
                    if (i<10)handler.postDelayed(this, 20);
                    i++;
                    decreaseVal();

                    //milliseconds);
                    // below is whatever you want to do

                }
            };
            handler.post(periodicUpdate);
        }

        invalidate();



    }

    private void decreaseVal(){
        val = val *0.99f;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        int height = canvas.getHeight();
        int width = canvas.getWidth();


        Paint RidePaint = new Paint();
        RidePaint.setColor(Color.rgb(34,68,89));
        RidePaint.setStrokeWidth(5);
        RidePaint.setTextSize(40);
        RidePaint.setAntiAlias(true);

        Paint SecondPaint = new Paint();
        SecondPaint.setColor(Color.argb(100,34,68,89));
        SecondPaint.setStrokeWidth(5);
        SecondPaint.setTextSize(40);
        SecondPaint.setAntiAlias(true);

        Path path = new Path();
        path.moveTo(0,height/2);
        path.lineTo(width,height/2);
        int res = 200;
        for (int i=5;i<res-5;i++){
            float x1 = i*width/res;
            float x2 = (i+1)*width/res;
            canvas.drawLine(x1,height/2-val*func(x1,width,height),x2,height/2-val*func(x2,width,height),RidePaint);
            canvas.drawLine(x1,height/2+val*func(x1,width,height),x2,height/2+val*func(x2,width,height),SecondPaint);
        }


        if (val != 0.0){
            this.setVisibility(VISIBLE);
            canvas.drawPath(path,RidePaint);
        }


    }

    private float func(float x,float width,float height){

        float y = (float)(Math.sin((x-width/2)/width*Math.PI*(val/3))*(Math.sin(x/width*Math.PI)));
        return y;
    }


}
