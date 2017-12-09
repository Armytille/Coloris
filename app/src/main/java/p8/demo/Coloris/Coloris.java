package p8.demo.Coloris;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.System.exit;

import com.plattysoft.leonids.ParticleSystem;

// declaration de notre activity héritée de Activity
public class Coloris extends Activity {

    public ColorisView mColorisView;

    private Handler handler;
    private AttributeSet attrs;
    private MediaPlayer mediaPlayer;
    private BestScores adapter;
    private ListView lv;
    private User val;
    private int scoreVal = 0;

    public static CountDownTimer mCountDown;
    public static long timer;
    public static long tick_mozart = 0;
    public static int scoreAcc = 0;
    public static int musicPos = 0;

    private static boolean isOptions = false;
    private static boolean isMenueZic = false;
    private static boolean isMuted = false;
    public static boolean isGravity = false;
    public static boolean graillzart = false;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("music", musicPos);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        musicPos = savedInstanceState.getInt("music");
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // initialise notre activity avec le constructeur parent
        super.onCreate(savedInstanceState);
        // charge le fichier menu.xml comme vue de l'activité
        setContentView(R.layout.menue);

        //mediaplayer for menue music
        if((mediaPlayer == null || !isMenueZic) && !isMuted) {
            isMenueZic = true;
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mmenue);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            mediaPlayer.seekTo(musicPos);
        }

        //Menue Buttons
        final Button play = (Button) findViewById(R.id.Play);
        final Button scores = (Button) findViewById(R.id.Scores);
        final Button options = (Button) findViewById(R.id.Options);
        final Button credits = (Button) findViewById(R.id.Credits);

        //get stored data from file
        scoreVal = getFromPrefs(getApplicationContext());

        // Construct the data source
        ArrayList<User> arrayOfUsers = new ArrayList<User>();

        // Create the adapter to convert the array to views
        adapter = new BestScores(this, arrayOfUsers);
        val = new User("Best Score : ", scoreVal);
        adapter.add(val);
        scoreAcc = 0;

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //initializing
                mColorisView = new ColorisView(getApplicationContext(), attrs);
                setContentView(R.layout.main);
                // recuperation de la vue une voie cree à partir de son id
                mColorisView = (ColorisView) findViewById(R.id.ColorisView);
                // charge le fichier menu.xml comme vue de l'activité
                mColorisView.isPlaying = true;
                //music game
                if(!isMuted && graillzart) {
                    isMenueZic = false;
                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mmozart);
                }
                else if (!isMuted) {
                    isMenueZic = false;
                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mcandy);
                }
                if(!isMuted) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();
                }
                //timer
                mCountDown = new CountDownTimer(203*1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        timer = millisUntilFinished / 1000;

                        tick_mozart++;
                        Random r = new Random();
                        int x = r.nextInt(7), y = r.nextInt(7);
                        if(graillzart && tick_mozart % 20 == 0) {
                            while (mColorisView.carte[x][y] == mColorisView.CST_mozart) {
                                x = r.nextInt(7);
                                y = r.nextInt(7);
                            }
                            mColorisView.ref[x][y] = mColorisView.carte[x][y] = mColorisView.CST_mozart;
                        }

                        //check board is full
                        if(mColorisView.isFull()) {
                            mCountDown.cancel();
                            mCountDown.onFinish();
                        }

                        //+1 level every 3000 score points
                        if(mColorisView.victoire)
                            mColorisView.loadlevel();
                    }

                    public void onFinish() {
                        if(mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mvictoire);
                            mediaPlayer.start();
                        }

                        //animations
                        FrameLayout mLl = (FrameLayout) findViewById(R.id.mainview);
                        doAnimation(mLl);

                        //alertdialog endgame
                        AlertDialog alertDialog = new AlertDialog.Builder(Coloris.this).create();
                        alertDialog.setTitle("FINISHED");
                        alertDialog.setMessage("SCORE = " + scoreAcc);
                        alertDialog.setCanceledOnTouchOutside(false);

                        WindowManager.LayoutParams wmlp = alertDialog.getWindow().getAttributes();
                        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
                        wmlp.x = 100;   //x position
                        wmlp.y = 100;   //y position
                        tick_mozart = 0;

                        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                musicPos = 0;
                                try {
                                    if(scoreAcc > scoreVal)
                                        storeScore(scoreAcc);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                finish();
                                Intent intent = new Intent().setClass(getApplicationContext(), Coloris.class);
                                startActivity(intent);
                            }
                        });
                        alertDialog.show();
                    }

                }.start();
            }
        });

        scores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOptions = true;
                setContentView(R.layout.scores);
                final Button backBtn = (Button) findViewById(R.id.backbtn);
                // Attach the adapter to a ListView
                lv = (ListView) findViewById(R.id.listview);
                lv.setAdapter(adapter);

                backBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
        });
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOptions = true;
                setContentView(R.layout.options);
                //Options Checkboxes
                final CheckBox muteMode = (CheckBox) findViewById(R.id.mute);
                final CheckBox gravityMode = (CheckBox) findViewById(R.id.gravity);
                final CheckBox mozartMode = (CheckBox) findViewById(R.id.mozart);
                final Button reset = (Button) findViewById(R.id.resetscore);
                final Button backBtn2 = (Button) findViewById(R.id.backbtn2);

                if(isMuted)
                    muteMode.setChecked(true);
                if(isGravity)
                    gravityMode.setChecked(true);
                if(graillzart)
                    mozartMode.setChecked(true);

                backBtn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
                muteMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(muteMode.isChecked()) {
                            isMuted = true;
                            mediaPlayer.stop();
                            mediaPlayer.release();
                        }
                        else {
                            isMuted = false;
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mmenue);
                            mediaPlayer.setLooping(true);
                            mediaPlayer.start();
                        }
                    }}
                );
                gravityMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(gravityMode.isChecked())
                            isGravity = true;
                        else
                            isGravity = false;
                    }}
                );
                mozartMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(mozartMode.isChecked())
                            graillzart = true;
                        else
                            graillzart = false;
                    }}
                );
                reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            storeScore(0);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(mColorisView != null) {
            if(mColorisView.isPlaying) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Quit");
                alertDialog.setMessage("Are you sure you want to quit ?");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(!isMuted) {
                            musicPos = mediaPlayer.getCurrentPosition();
                            mediaPlayer.stop();
                            mediaPlayer.release();
                        }
                        mCountDown.cancel();
                        mColorisView.isPlaying = false;
                        finish();
                        Intent intent = new Intent().setClass(getApplicationContext(), Coloris.class);
                        startActivity(intent);
                    }
                });
                alertDialog.show();
            }
            else
                exit(0);
        }
        else if(isOptions) {
            isOptions = false;
            if(!isMuted) {
                musicPos = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            finish();
            Intent intent = new Intent().setClass(getApplicationContext(), Coloris.class);
            startActivity(intent);
        }
        else
            exit(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(mediaPlayer == null && !isMuted) {
            if(!isMenueZic) {
                if(graillzart) {
                    isMenueZic = false;
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mmozart);
                }
                else {
                    isMenueZic = false;
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mcandy);
                }
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
            else {
                isMenueZic = true;
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.mmenue);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer != null)
            mediaPlayer.seekTo(musicPos);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mColorisView != null){
            if(mediaPlayer != null && mColorisView.isPlaying)
                musicPos = mediaPlayer.getCurrentPosition();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void doAnimation(View v){
        ParticleSystem ps = new ParticleSystem(this, 50, R.drawable.star, 1000, R.id.mainview);
        ps.setSpeedRange(0.1f, 0.25f);
        ps.setScaleRange(1.0f, 1.0f);
        ps.setSpeedRange(0.1f, 0.25f);
        ps.setAcceleration(0.0001f, 90);
        ps.setRotationSpeedRange(90, 180);
        ps.setFadeOut(200, new AccelerateInterpolator());
        ps.emit(v, 100);
    }

    public void storeScore(int score) throws IOException {
        FileOutputStream fileout = openFileOutput("scores.txt", MODE_PRIVATE);
        OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
        outputWriter.write(String.valueOf(score));
        outputWriter.flush();
        outputWriter.close();
    }

    public int getFromPrefs(Context context) {
        try {
            InputStream inputStream = context.openFileInput("scores.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;

                while ((receiveString = bufferedReader.readLine()) != null) {
                    return Integer.parseInt(receiveString);
                }
                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return 0;
    }
}
