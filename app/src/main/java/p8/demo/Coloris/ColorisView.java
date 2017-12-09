package p8.demo.Coloris;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

import static p8.demo.Coloris.Coloris.graillzart;
import static p8.demo.Coloris.Coloris.isGravity;
import static p8.demo.Coloris.Coloris.scoreAcc;
import static p8.demo.Coloris.Coloris.timer;

public class ColorisView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private MediaPlayer mpPop;
    boolean[] isAlligned = new boolean[3];
    boolean[] isMoved = new boolean[3];
    boolean isPlaying = false;
    boolean isPoped = false;
    boolean victoire = false;

    //niveau courant
    int current_level = 0;
    int default_level = 0;
    int randType = 0;
    int rand = 0;

    // constante modelisant les differentes types de cases
    final int CST_vide = 0;
    final int CST_mozart = -1;

    // tableau modelisant la carte du jeu
    int[][] carte;

    // tableau de reference du terrain
    int[][] ref = {
            {CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide},
            {CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide},
            {CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide},
            {CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide},
            {CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide},
            {CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide},
            {CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide},
            {CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide, CST_vide}
    };

    // position de reference des bonbons
    int[][][] refbonbons = {
            {{2, 0}, {3, 0}, {4, 0}, {0, 1, 2}},
            {{2, 3}, {3, 3}, {4, 3}, {3, 4, 5}},
            {{2, 6}, {3, 6}, {4, 6}, {6, 7, 0}}
    };

    int[] refbonbontype = new int[9];

    // Declaration des images
    private Bitmap[] block = new Bitmap[40];
    private Bitmap vide;
    private Bitmap sprite;
    private Bitmap mozart;

    // Declaration des objets Ressources et Context permettant d'accéder aux ressources de notre application et de les charger
    private Resources mRes;
    private Context mContext;

    // ancres pour pouvoir centrer la carte du jeu
    int carteTopAnchor;                   // coordonnées en Y du point d'ancrage de notre carte
    int carteLeftAnchor;                  // coordonnées en X du point d'ancrage de notre carte
    int[] x = new int[3];
    int[] y = new int[3];

    // taille de la carte
    static final int carteWidth = 8;
    static final int carteHeight = 8;
    static final int carteTileSize = 36;

    private int frameWidth = 73;
    private int frameHeight = 72;

    // thread utiliser pour animer les zones de depot des bonbons
    private boolean in = true;
    private Thread cv_thread;
    SurfaceHolder holder;

    Paint paint;
    Paint scorePaint;

    Canvas c = null;
    private Bitmap scaled;

    public ColorisView(Context context) {
        super(context);
    }

    public ColorisView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(mpPop == null)
            mpPop = MediaPlayer.create(getContext(), R.raw.mpop);

        // permet d'ecouter les surfaceChanged, surfaceCreated, surfaceDestroyed
        holder = getHolder();
        holder.addCallback(this);

        // chargement des images
        mContext = context;
        mRes = mContext.getResources();

        vide = BitmapFactory.decodeResource(mRes, R.drawable.vide);
        vide = Bitmap.createScaledBitmap(vide, carteTileSize+4, carteTileSize+4, false);
        sprite = BitmapFactory.decodeResource(mRes, R.drawable.candy);
        mozart = BitmapFactory.decodeResource(mRes, R.drawable.mozart_glasses);
        mozart = Bitmap.createScaledBitmap(mozart, carteTileSize+1, carteTileSize+4, false);

        //change le type de bonbon en fonction du level
        for(int i = 0; i < 8; i++) {
            block[i] = Bitmap.createBitmap(sprite, (i<3 ? 62 : 60) + frameWidth*i, 18,frameWidth, frameHeight);
            block[i] = Bitmap.createScaledBitmap(block[i], carteTileSize, carteTileSize, false);
            block[i+8] = Bitmap.createBitmap(sprite, (i<3 ? 60 : 61)   + 72*i, 90,  73, 71);
            block[i+8] = Bitmap.createScaledBitmap(block[i+8], carteTileSize, carteTileSize, false);
            block[i+16] = Bitmap.createBitmap(sprite, 64   + 72*i, 90+frameHeight,  73, 71);
            block[i+16] = Bitmap.createScaledBitmap(block[i+16], carteTileSize, carteTileSize, false);
            block[i+24] = Bitmap.createBitmap(sprite, 64   + 72*i, 90+2*frameHeight,  73, 71);
            block[i+24] = Bitmap.createScaledBitmap(block[i+24], carteTileSize, carteTileSize, false);
            block[i+32] = Bitmap.createBitmap(sprite, 64   + 72*i, 90+3*frameHeight+2,  73, 71);
            block[i+32] = Bitmap.createScaledBitmap(block[i+32], carteTileSize, carteTileSize, false);
        }

        // initialisation des parmametres du jeu
        initparameters();

        // creation du thread
        cv_thread = new Thread(this);
        // prise de focus pour gestion des touches
        setFocusable(true);
    }

    // chargement du niveau a partir du tableau de reference du niveau
    public void loadlevel() {
        if(victoire) {
            current_level++;
            victoire = false;
        }
        else if(!graillzart) {
            Random r = new Random();
            randType = r.nextInt(5);
            current_level = default_level;
        }

        for (int i = 0; i < carteHeight; i++) {
            for (int j = 0; j < carteWidth; j++) {
                carte[j][i] = ref[j][i];
            }
        }
        if(rand < 8)
            rand++;
        if(graillzart && rand%4 == 0 && randType < 5)
            randType++;
    }

    // initialisation du jeu
    public void initparameters() {
        paint = new Paint();
        scorePaint = new Paint();

        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        //paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(40f);

        scorePaint.setColor(Color.BLACK);
        scorePaint.setStrokeWidth(2);
        scorePaint.setTextAlign(Paint.Align.LEFT);
        scorePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        scorePaint.setTextSize(20f);

        carte = new int[carteHeight][carteWidth];
        loadlevel();
        carteTopAnchor = (getHeight() - carteHeight * carteTileSize) / 3;
        carteLeftAnchor = (getWidth() - carteWidth * carteTileSize) / 2;

        random_couleur(0);
        random_couleur(1);
        random_couleur(2);

        if ((cv_thread != null) && (!cv_thread.isAlive())) {
            cv_thread.start();
            Log.e("-FCT-", "cv_thread.start()");
        }
    }

    public void random_couleur(int nb){
        Random r = new Random();
        for(int i = 0; i<3; i++) {
            refbonbons[nb][3][i] = r.nextInt(rand);
            if(graillzart) {
                if(randType > 0)
                    refbonbontype[nb*3+i] = r.nextInt(randType+1);
                else
                    refbonbontype[nb*3+i] = 0;
            }
            else
                refbonbontype[nb*3+i] = randType;
        }
    }

    // dessin de la carte du jeu
    private void paintcarte(Canvas canvas) {
        for (int i = 0; i < carteHeight; i++)
            for (int j = 0; j < carteWidth; j++) {
                if(carte[i][j] > 0)
                    canvas.drawBitmap(block[carte[i][j]-1 ], carteLeftAnchor + j * (carteTileSize+1), carteTopAnchor + i * (carteTileSize+1), null);
                if(carte[i][j] == 0)
                    canvas.drawBitmap(vide, carteLeftAnchor + j * (carteTileSize+1), carteTopAnchor + i * (carteTileSize+1), null);
                if(carte[i][j] == -1)
                    canvas.drawBitmap(mozart, carteLeftAnchor + j * (carteTileSize+1), carteTopAnchor + i * (carteTileSize+1), null);
            }
    }

    // dessin des bonbons
    private void paintbonbons(Canvas canvas) {
        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < 3; i++) {
                //  Log.d("HAAAA", "refbonbontype = " + (refbonbons[i][3][j]));
                if(!isAlligned[i]) {
                    canvas.drawBitmap(block[refbonbons[i][3][j] + refbonbontype[j+i*3]*8],
                            x[i] + (carteLeftAnchor + refbonbons[i][j][1] * carteTileSize + carteTileSize / 2),
                            y[i] + ((carteTopAnchor + refbonbons[i][j][0] * carteTileSize) + getHeight() / 2),
                            null);
                }
                else {
                    canvas.drawBitmap(block[refbonbons[i][3][j] + refbonbontype[j+i*3]*8],
                            x[i] + (carteLeftAnchor/6 + (frameWidth+carteWidth*4)*i + refbonbons[j][i][1] * carteTileSize/3),
                            y[i] + ((carteTopAnchor + refbonbons[i][0][0] * carteTileSize) + carteTileSize + getHeight() / 2),
                            null);
                }
            }
        }
    }

    private void drop(int xCoord, int yCoord, int index, boolean allign) {
        int myX = xCoord/carteTileSize, myY = yCoord/carteTileSize;
        if((myX >= 0 && myX < carteWidth) && (myY + carteHeight >= 0 && myY < 0)) {
            if(allign && myX < 6) {
                if(ref[myY+carteHeight][myX] == CST_vide && ref[myY+carteHeight][myX+1] == CST_vide && ref[myY+carteHeight][myX+2] == CST_vide) {
                    for(int i=0; i<3; i++)
                        ref[myY+carteHeight][myX+i] = carte[myY+carteHeight][myX+i] = refbonbons[index][3][i]+1+refbonbontype[i+index*3]*8;
                    for(int i=0; i<3; i++) {
                        if(isMoved[i]) {
                            isAlligned[i] = !isAlligned[i];
                            x[i] = y[i] = 0;
                            random_couleur(i);
                        }
                    }
                }
            }
            else if(!allign && myY+carteHeight < 6) {
                if(ref[myY+carteHeight][myX] == CST_vide && ref[myY+1+carteHeight][myX] == CST_vide && ref[myY+2+carteHeight][myX] == CST_vide) {
                    for(int i=0; i<3; i++)
                        ref[myY+i+carteHeight][myX] = carte[myY+i+carteHeight][myX] = refbonbons[index][3][i]+1+refbonbontype[i+index*3]*8;
                    for(int i=0; i<3; i++) {
                        if(isMoved[i]) {
                            isAlligned[i] = !isAlligned[i];
                            x[i] = y[i] = 0;
                            random_couleur(i);
                        }
                    }
                }
            }
        }
    }

    //graille les bonbons qui formenr une ligne verticales ou horizontales de 3 ou +
    private void detection() {
        int[][] t = new int[64][2];
        int acc = 0;

        for (int i = 0; i < carteHeight; i++) {
            for (int j = 0; j < carteWidth; j++) {
                if(carte[j][i] > 0) {
                    if(j-1> -1 && j-2 > -1 && (carte[j][i] == carte[j-1][i]) && (carte[j][i] == carte[j-2][i])){
                        t[acc][0] = j; t[acc++][1] = i; t[acc][0] = j-1; t[acc++][1] = i;t[acc][0] = j-2; t[acc++][1] = i; isPoped = true;}
                    if(j+1< carteWidth && j+2 < carteWidth && (carte[j][i] == carte[j+1][i]) && (carte[j][i] == carte[j+2][i])){
                        t[acc][0] = j; t[acc++][1] = i;t[acc][0] = j+1; t[acc++][1] = i;t[acc][0] = j+2; t[acc++][1] = i; isPoped = true;}
                    if(i+1< carteHeight && i+2 < carteHeight && (carte[j][i] == carte[j][i+1]) && (carte[j][i] == carte[j][i+2])){
                        t[acc][0] = j; t[acc++][1] = i;t[acc][0]= j; t[acc++][1] = i+1;t[acc][0] = j; t[acc++][1] = i+2; isPoped = true;}
                    if(i-1> -1 && i-2 > -1 && (carte[j][i] == carte[j][i-1]) && (carte[j][i] == carte[j][i-2])){
                        t[acc][0] = j; t[acc++][1] = i;t[acc][0] = j; t[acc++][1] = i-1;t[acc][0] = j; t[acc++][1] = i-2; isPoped = true;}
                }
            }
        }
        for(int i = 0;i<acc;i++) {
            ref[t[i][0]][t[i][1]] = carte[t[i][0]][t[i][1]] = CST_vide;
            if(graillzart){
                if(t[i][0]-1 > -1 && ref[t[i][0]-1][t[i][1]] == CST_mozart){
                    ref[t[i][0]-1][t[i][1]] = carte[t[i][0]-1][t[i][1]] = CST_vide;scoreAcc += 1000; isPoped = true;}
                if(t[i][0] +1 < carteWidth && ref[t[i][0]+1][t[i][1]] == CST_mozart){
                    ref[t[i][0]+1][t[i][1]] = carte[t[i][0]+1][t[i][1]] = CST_vide;scoreAcc += 1000; isPoped = true;}
                if(t[i][1] +1 < carteHeight && ref[t[i][0]][t[i][1]+1] == CST_mozart){
                    ref[t[i][0]][t[i][1]+1] = carte[t[i][0]][t[i][1]+1] = CST_vide;scoreAcc += 1000; isPoped = true;}
                if(t[i][1]-1 > -1 && ref[t[i][0]][t[i][1]-1] == CST_mozart){
                    ref[t[i][0]][t[i][1]-1] = carte[t[i][0]][t[i][1]-1] = CST_vide;scoreAcc += 1000; isPoped = true;}
            }
        }
        if(isPoped) {
            isPoped = false;
            mpPop.seekTo(700);
            mpPop.start();
        }
        scoreAcc += acc/2*100;
    }

    //graille les espace vides ggwp
    public boolean isFull() {
        for (int i = 0; i < carteHeight; i++) {
            for (int j = 0; j < carteWidth; j++) {
                if(carte[j][i] == CST_vide) {
                    if(j-1> 0 && j-2 > 0 && (carte[j][i] == carte[j-1][i]) && (carte[j][i] == carte[j-2][i]))
                        return false;
                    if(j+1< carteWidth && j+2 < carteWidth && (carte[j][i] == carte[j+1][i]) && (carte[j][i] == carte[j+2][i]))
                        return false;
                    if(i+1< carteHeight && i+2 < carteHeight && (carte[j][i] == carte[j][i+1]) && (carte[j][i] == carte[j][i+2]))
                        return false;
                    if(i-1> 0 && i-2 > 0 && (carte[j][i] == carte[j][i-1]) && (carte[j][i] == carte[j][i-2]))
                        return false;
                }
            }
        }
        return true;
    }

    private void gravity() {
        for (int i = carteHeight-1; i > -1; i--) {
            for (int j = carteWidth-1; j > -1; j--) {
                if(carte[i][j] != CST_vide) {
                    if(i < carteHeight-1 && carte[i+1][j] == CST_vide) {
                        ref[i+1][j] = carte[i+1][j] = carte[i][j];
                        ref[i][j] = carte[i][j] = CST_vide;
                    }
                }
            }
        }
    }

    private boolean isWon() {
        if(scoreAcc >= 3000*(current_level+1))
            return true;
        else
            return false;
    }

    // dessin du jeu (fond image, dessin du plateau et des bonbons)
    private void nDraw(Canvas canvas) {
        canvas.drawBitmap(scaled, 0, 0, null); //draw the background
        paintcarte(canvas);
        paintbonbons(canvas);
        if(timer > 201) {
            canvas.drawText("Timer : 200", carteLeftAnchor, carteTopAnchor-40, scorePaint);
            canvas.drawText("get Ready !", getWidth()/4, getHeight() /2-frameHeight, paint);
        }
        else if(timer > 200) {
            canvas.drawText("Timer : 200", carteLeftAnchor, carteTopAnchor-40, scorePaint);
            canvas.drawText("Go !", getWidth()/4+carteTileSize, getHeight()/2-frameHeight, paint);
        }
        else if(timer <= 200)
            canvas.drawText("Timer : " + timer, carteLeftAnchor, carteTopAnchor-40, scorePaint);
        canvas.drawText("Score : " + scoreAcc, carteLeftAnchor, carteTopAnchor-10, scorePaint);
    }

    // callback sur le cycle de vie de la surfaceview
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("-> FCT <-", "surfaceChanged " + width + " - " + height);
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        Log.i("-> FCT <-", "surfaceCreated");
        if(in)
            initparameters();
        else if ((cv_thread != null) && (!cv_thread.isAlive())) {
            in = true;
            cv_thread = new Thread(this);
            cv_thread.start();
            Log.e("-FCT-", "cv_thread.start()");
        }
        if(scaled == null) {
            Random brand = new Random();
            int bval = brand.nextInt(8);
            Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.bcandy);
            if(bval == 1)
                background = BitmapFactory.decodeResource(getResources(), R.drawable.bcandy2);
            else if(bval == 2)
                background = BitmapFactory.decodeResource(getResources(), R.drawable.bcandy3);
            else if(bval == 3)
                background = BitmapFactory.decodeResource(getResources(), R.drawable.bcandy4);
            else if(bval == 4)
                background = BitmapFactory.decodeResource(getResources(), R.drawable.bloli);
            else if(bval == 5)
                background = BitmapFactory.decodeResource(getResources(), R.drawable.bloli2);
            else if(bval == 6)
                background = BitmapFactory.decodeResource(getResources(), R.drawable.bloli3);
            else if(bval == 7)
                background = BitmapFactory.decodeResource(getResources(), R.drawable.bloli4);
            float scale = (float)background.getHeight()/(float)getHeight();
            int newWidth = Math.round(background.getWidth()/scale);
            int newHeight = Math.round(background.getHeight()/scale);
            scaled = Bitmap.createScaledBitmap(background, newWidth, newHeight, true);
        }
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.i("-> FCT <-", "surfaceDestroyed");
        in = false;
    }

    public void getMatCoord(int x, int y) {
        Log.i("TAG", "{" + x/carteTileSize + "," + y/carteTileSize + "}");
    }

    /**
     * run (run du thread cr��)
     * on endort le thread, on modifie le compteur d'animation, on prend la main pour dessiner et on dessine puis on lib�re le canvas
     */
    public void run() {

        while (in) {
            if(timer > 0) {
                try {
                    victoire = isWon();
                    detection();
                    if(victoire) {
                        //todo
                    }
                    if(isGravity)
                        gravity();
                    cv_thread.sleep(40);
                    try {
                        c = holder.lockCanvas(null);
                        nDraw(c);
                    } finally {
                        if (c != null) {
                            holder.unlockCanvasAndPost(c);
                        }
                    }
                } catch (Exception e) {
                    Log.e("-> RUN <-", "PB DANS RUN");
                }
            }
        }
    }
    // fonction permettant de recuperer les evenements tactiles
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.i("-> FCT <-", "onTouchEvent: " + event.getX() + " " + event.getY());
        if(timer <= 200) {
            int xPos = (int)event.getX();
            int yPos = (int)event.getY();
            int height = (carteTopAnchor + refbonbons[0][0][1] * carteTileSize) + getHeight()/2;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    for(int i=0; i<3; i++) {
                        if((xPos > (getWidth()/3)*i && xPos < frameWidth+(getWidth()/3)*i) && (yPos > height+frameHeight)) {
                            x[i] = xPos - (getWidth()/3)*i-frameWidth+20;
                            y[i] = yPos - height-frameHeight-100;
                            isMoved[i] = true;
                        }
                        else {
                            x[i] = y[i] = 0;
                            isMoved[i] = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    for(int i=0; i<3; i++) {
                        if(isMoved[i]) {
                            x[i] = xPos - (getWidth()/3)*i-frameWidth+20;
                            y[i] = yPos - height-frameHeight-100;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(isMoved[0]) {
                        if(isAlligned[0])
                            drop(x[0]+frameWidth/2-50, y[0]+carteTileSize+10, 0, isAlligned[0]);
                        else
                            drop(x[0]+frameWidth/2, y[0]+10, 0, isAlligned[0]);
                    }

                    if(isMoved[1]) {
                        if(isAlligned[1])
                            drop(x[1]+(frameWidth/2)*4-50, y[1]+carteTileSize+10, 1, isAlligned[1]);
                        else
                            drop(x[1]+(frameWidth/2)*4, y[1]+10, 1, isAlligned[1]);
                    }

                    if(isMoved[2]) {
                        if(isAlligned[2])
                            drop(x[2]+(frameWidth/2)*7-50, y[2]+carteTileSize+10, 2, isAlligned[2]);
                        else
                            drop(x[2]+(frameWidth/2)*7, y[2]+10, 2, isAlligned[2]);
                    }

                    for(int i=0; i<3; i++) {
                        x[i] = y[i] = 0;
                        if(isMoved[i]) {
                            if(isAlligned[i]) {
                                if(graillzart){
                                    refbonbontype[i*3] += refbonbontype[i*3+2];
                                    refbonbontype[i*3+2] = refbonbontype[i*3] - refbonbontype[i*3+2];
                                    refbonbontype[i*3] -= refbonbontype[i*3+2];
                                }
                                refbonbons[i][3][0] += refbonbons[i][3][2];
                                refbonbons[i][3][2] = refbonbons[i][3][0] - refbonbons[i][3][2];
                                refbonbons[i][3][0] -= refbonbons[i][3][2];
                            }
                            isAlligned[i] = !isAlligned[i];
                        }
                    }

                    break;
            }
        }

        return true;
    }
}
