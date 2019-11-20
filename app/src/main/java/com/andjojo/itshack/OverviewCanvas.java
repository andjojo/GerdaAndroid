package com.andjojo.itshack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;


public class OverviewCanvas extends View {

    String stations[];
    String steps[];
    boolean real_draw=false;

    public OverviewCanvas(Context context) {
        super(context);
        init(context);
    }

    public OverviewCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OverviewCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        //do stuff that was in your original constructor...
    }

    public void setStations(String[] stations){
        this.stations = stations;
    }

    public void setSteps(String[] steps){
        this.steps = steps;
    }
    public void draw(){
        real_draw=true;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        paint.setStrokeWidth(3);
        paint.setAntiAlias(true);
        float width = canvas.getWidth();
        float height = canvas.getHeight();
        float stationHeight=height/(stations.length+1);

        if(real_draw){
            float prevHeight=0;
            for (int i=0;i<stations.length;i++) {
                float currentHeight=stationHeight * (i+1);
                Rect bounds = new Rect();
                paint.getTextBounds(stations[i], 0, stations[i].length(), bounds);
                canvas.drawText(stations[i], 2*width / 5-bounds.width(), currentHeight+bounds.height()/3, paint);
                canvas.drawCircle(width/2,currentHeight,width/50,paint);
                if (i!=0){
                    canvas.drawText(steps[i-1], 3*width / 5, (currentHeight+prevHeight)/2, paint);
                    canvas.drawLine(width/2,prevHeight,width/2,currentHeight,paint);
                }
                prevHeight=currentHeight;
            }
        }
    }


}
