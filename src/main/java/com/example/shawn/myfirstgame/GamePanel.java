package com.example.shawn.myfirstgame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by shawn on 4/24/2016.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missleStartTime;
    private long missleElapsed;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private Random rand = new Random();

    public GamePanel(Context context)
    {
        super(context);

        // add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;

            }catch(InterruptedException e){e.printStackTrace();}

        }

    }

    @Override
        public void surfaceCreated(SurfaceHolder holder) {

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        smokeStartTime= System.nanoTime();
        missleStartTime = System.nanoTime();


        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN) {
            if(!player.getPlaying())
            {
                player.setPlaying(true);
            }
            else
            {
                player.setUp(true);
            }
            return true;
        }

        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            player.setUp(false);
            return true;
        }


        return super.onTouchEvent(event);
    }


    public void update()
    {
        if(player.getPlaying())
        {
            bg.update();
            player.update();

            //add missiles on timer
            long missilesElapsed = (System.nanoTime()-missleStartTime)/1000000;
            if (missilesElapsed >(2000 - player.getScore()/4)){

                //first missile always go down the middle
                if (missiles.size()==0)
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.
                            missile), WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13));
                }
                else
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10,(int)(rand.nextDouble()*(HEIGHT)),45,15, player.getScore(),13));
                }

                //reset timer
                missleStartTime = System.nanoTime();
            }

            for (int i = 0; i<missiles.size();i++)
            {
                //loops through every missile and checks collision and remove

                // updates missile
                missiles.get(i).update();
                if (collision(missiles.get(i),player))
                {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //remove missiles if they are way off of the screen
                if(missiles.get(i).getX()<-100)
                {
                    missiles.remove(i);
                    break;
                }
            }

            //add smoke puffs
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120){
                smoke.add(new Smokepuff(player.getX(), player.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            for(int i = 0; i<smoke.size();i++)
            {
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }
        }

    }
    public boolean collision(GameObject a, GameObject b)
    {
        if (Rect.intersects(a.getRectangle(),b.getRectangle()))
        {
            return true;
        }
        return false;
    }
    @Override
    public void draw(Canvas canvas)
    {
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);


        if(canvas!=null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);
            for(Smokepuff sp: smoke)
            {
                sp.draw(canvas);
            }
            for (Missile m: missiles)
            {
                m.draw(canvas);
            }

            canvas.restoreToCount(savedState);
        }
    }

}
