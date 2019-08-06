package com.example.controlrobotstestapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener
{
    private float centerX;
    private float centerY;
    private float baseRadius;
    private float hatRadius;
    private JoystickListener joystickCallback;
    private final int ratio = 5;
    private float constrainedX;
    private float constrainedY;

    private void setupDimensions()
    {
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        baseRadius = Math.min(getWidth(), getHeight()) / 3;
        hatRadius = Math.min(getWidth(), getHeight()) / 8;
    }

    public JoystickView(Context context)
    {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    public JoystickView(Context context, AttributeSet attributes, int style)
    {
        super(context, attributes, style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    public JoystickView(Context context, AttributeSet attributes)
    {
        super(context, attributes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallback = (JoystickListener) context;
    }

    private void drawJoystick(float newX, float newY)
    {
        if(getHolder().getSurface().isValid())
        {
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            myCanvas.drawColor(Color.argb(255,50,50,50)); // очищаем задники

            // определяем синус и косинус угла под которым точка касания находится относительно центра джойстика
            float hypotenuse = (float) Math.sqrt(Math.pow(newX - centerX, 2) + Math.pow(newY - centerY, 2));
            float sin = (newY - centerY) / hypotenuse;
            float cos = (newX - centerX) / hypotenuse;
            
            colors.setARGB(255, 120, 120, 120);
            myCanvas.drawCircle(centerX, centerY, baseRadius, colors);
            for(int i = 1; i <= (int) (baseRadius / ratio); i++)
            {
                colors.setARGB(255, 30, 30, 30); // создаем эффект затенения
                myCanvas.drawCircle(newX - cos * hypotenuse * (ratio/baseRadius) * i,
                        newY - sin * hypotenuse * (ratio/baseRadius) * i, i * (hatRadius * ratio / baseRadius), colors);
            }

            // рисуем шляпку джойстика
            for(int i = 0; i <= (int) (hatRadius / ratio); i++)
            {
                colors.setARGB(255, 20, 20, 20);
                myCanvas.drawCircle(newX, newY, hatRadius - (float) i * (ratio) / 2 , colors);
            }

            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        setupDimensions();
        drawJoystick(centerX, centerY);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public boolean onTouch(View v, MotionEvent e)
    {
        if(v.equals(this))
        {
            if(e.getAction() != e.ACTION_UP)
            {
                float displacement = (float) Math.sqrt((Math.pow(e.getX() - centerX, 2)) + Math.pow(e.getY() - centerY, 2));
                if(displacement < baseRadius)
                {
                    drawJoystick(e.getX(), e.getY());
                    joystickCallback.onJoystickMoved(centerX, centerY, e.getX(), e.getY(),(e.getX() - centerX)/baseRadius, (e.getY() - centerY)/baseRadius, getId());
                }
                else
                {
                    float ratio = baseRadius / displacement;
                    constrainedX = centerX + (e.getX() - centerX) * ratio;
                    constrainedY = centerY + (e.getY() - centerY) * ratio;
                    drawJoystick(constrainedX, constrainedY);
                    joystickCallback.onJoystickMoved(centerX, centerY, constrainedX,constrainedY,(constrainedX-centerX)/baseRadius, (constrainedY-centerY)/baseRadius, getId());
                }
            }
            else
            {
                drawJoystick(centerX, centerY);
                joystickCallback.onJoystickMoved(centerX, centerY, centerX, centerY, 0, 0, getId());
            }

        }
        return true;
    }

    public interface JoystickListener
    {
        void onJoystickMoved(float xCenter, float yCenter, float xPosition, float yPosition, float xPercent, float yPercent, int id);
    }

    public float getValueX(){
        return this.constrainedX;
    }

    public float getValueY(){
        return this.constrainedY;
    }
}
