package com.example.pitchdetection;


import static com.example.pitchdetection.TuningsList.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.Manifest;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;

import android.graphics.Color;
import android.os.Bundle;


import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PitchDetector.PitchDetectionListener, View.OnClickListener {
    public TextView pitchTextView;
    public TextView noteTextView;
    private PitchDetector pitchDetector;


    private Button s1, s2, s3, s4, s5, s6;

    private String tuningText = "Standard E (E2 A2 D3 G3 B3 e4)";

    private Button selectedB;
    private Fragment currentFragment = null;

    private List<String> data=new ArrayList<>();
    public static String[][] notesList = {


            {"B1", "61.74"},
            {"C2", "65.41"},
            {"C#2", "69.30"},
            {"D2", "73.42"},
            {"D#2", "77.78"},
            {"E2", "82.41"},
            {"F2", "87.31"},
            {"F#2", "92.50"},
            {"G2", "98.00"},
            {"G#2", "103.83"},
            {"A2", "110.00"},
            {"A#2", "116.54"},
            {"B2", "123.47"},
            {"C3", "130.81"},
            {"C#3", "138.59"},
            {"D3", "146.83"},
            {"D#3", "155.56"},
            {"E3", "164.81"},
            {"F3", "174.61"},
            {"F#3", "185.00"},
            {"G3", "196.00"},
            {"G#3", "207.65"},
            {"A3", "220.00"},
            {"A#3", "233.08"},
            {"B3", "246.94"},
            {"C4", "261.63"},
            {"C#4", "277.18"},
            {"D4", "293.66"},
            {"D#4", "311.13"},
            {"E4", "329.63"},
            {"F4", "349.23"},
            {"F#4", "369.99"},
            {"G4", "392.00"},
            {"G#4", "415.30"},
            {"A4", "440.00"},


    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        s1 = findViewById(R.id.s1);
        s2 = findViewById(R.id.s2);
        s3 = findViewById(R.id.s3);
        s4 = findViewById(R.id.s4);
        s5 = findViewById(R.id.s5);
        s6 = findViewById(R.id.s6);

        selectedB=s1;
        changeColor(s1);
        s1.setText("E2");
        s2.setText("A2");
        s3.setText("D3");
        s4.setText("G3");
        s5.setText("B3");
        s6.setText("e4");


        for (Button button : Arrays.asList(s1, s2, s3, s4, s5, s6)) {

            button.setOnClickListener(this);
        }






        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 123);
        } else {
            pitchDetector = new PitchDetector();
            pitchDetector.start(this);
        }

        pitchTextView = findViewById(R.id.freq);
        noteTextView = findViewById(R.id.note);
        pitchDetector = new PitchDetector();
        pitchDetector.setPitchDetectionListener(this);

        data = getList(this);
        if(data.isEmpty()){
            data = TuningsList.fillList(this);
            saveList(this, data);
        }






        CardView cardView = findViewById(R.id.tuningCardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                onClickTuningsView();


            }
        });

        ImageView options = findViewById(R.id.options);

        options.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {

                Context wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.PopupMenu);
                PopupMenu popupMenu = new PopupMenu(wrapper, options);


                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                       if(menuItem.toString().equals("Add Tuning")){
                           onClickCustomTView("",0);
                       }else if(menuItem.toString().equals("Edit Tuning")){
                          onClickEditTView();
                       }

                        return true;
                    }
                });

                popupMenu.show();


            }
        });

    }

    public void onClickCustomTView(String string, int position) {
        if (currentFragment instanceof CustomTuningFragment) {
            closeFragment(currentFragment);
        } else {
            openFragment(new CustomTuningFragment(string, position), R.id.fragmentCustomTuning);
        }
    }
    public void onClickTuningsView() {
        if (currentFragment instanceof TuningFragment) {
            closeFragment(currentFragment);
        } else {
            openFragment(new TuningFragment(data), R.id.fragmentTuning);
        }
    }

    public void onClickEditTView(){
        if (currentFragment instanceof EditTuningFragment) {
            closeFragment(currentFragment);
        } else {
            openFragment(new EditTuningFragment(data), R.id.fragmentEditTuning);
        }
    }


    private void openFragment(Fragment fragment, int containerViewId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (currentFragment != null) {
            fragmentTransaction.remove(currentFragment);
        }
        pitchDetector.stop();
        fragmentTransaction.replace(containerViewId, fragment).commit();
        currentFragment = fragment;
    }

    private void closeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment).commit();
        pitchDetector.start(getApplicationContext());
        currentFragment = null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        pitchDetector.start(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pitchDetector.stop();
    }

    @Override
    public void onPitchDetected(final double pitchFrequency) {

        if (selectedB != null) {

            noteTextView.setText(selectedB.getText().toString().replaceAll("\\d", ""));
        }
        if (pitchFrequency > 50 && pitchFrequency < 4000) {
            double cents;
            double targetFrequency;
            for (String[] strings : notesList) {


                targetFrequency = Double.parseDouble(strings[1]);
                cents = 1200 * Math.log(pitchFrequency / targetFrequency) / Math.log(2);

                if (tuningText.equals("Automatic Tuning")) {

                    if (Math.abs(cents) <= 50) {
                        pitchTextView.setText(String.format("%.2f", pitchFrequency) + " Hz");
                        noteTextView.setText(strings[0]);

                        setPosition(cents);
                    }


                } else {

                    if (strings[0].equalsIgnoreCase(selectedB.getText().toString()) && (Math.abs(cents) <= 500)) {
                        pitchTextView.setText("");
                        targetFrequency = Double.parseDouble(strings[1]);
                        cents = 1200 * Math.log(pitchFrequency / targetFrequency) / Math.log(2);
                        setPosition(cents);

                    }

                }


            }
        }


    }


    private void setPosition(double cents) {

        int maxOffset = 900;
        double maxCents = 50.0;
        double minCents = -50.0;

        double rangeCents = maxCents - minCents;
        double offset = (cents - minCents) / rangeCents * (2 * maxOffset) - maxOffset;
        ConstraintLayout visibleArea = findViewById(R.id.constraintL);
        ImageView pointer = findViewById(R.id.pointer);
        int visibleWidth = visibleArea.getWidth();
        int indicatorWidth = pointer.getWidth();
        int maxVisibleOffset = visibleWidth - indicatorWidth;

        offset = Math.max(-maxVisibleOffset / 2.0, Math.min(maxVisibleOffset / 2.0, offset));


        float centerX = visibleArea.getX() + visibleWidth / 2f;
        float targetX = centerX + (float) offset;



        pointer.setTranslationX(targetX);


    }


    public void setTuning(String[] notes, String text) {
        onClickTuningsView();
        setBackground();
        TextView tuning = findViewById(R.id.tuningText);
        tuning.setText(text);
        for (Button button : Arrays.asList(s1, s2, s3, s4, s5, s6)) {
            button.setText("");
        }
        tuningText = text;


        if (!tuningText.equals("Automatic Tuning")) {
            changeColor(s1);
            s1.setText(notes[0]);
            s2.setText(notes[1]);
            s3.setText(notes[2]);
            s4.setText(notes[3]);
            s5.setText(notes[4]);
            s6.setText(notes[5]);


            for (Button button : Arrays.asList(s1, s2, s3, s4, s5, s6)) {

                button.setOnClickListener(this);
            }
        }


    }


    @Override
    public void onClick(View v) {


        setBackground();


        if (tuningText != null) {

            for (Button button : Arrays.asList(s1, s2, s3, s4, s5, s6)) {
                if (!tuningText.equals("Automatic Tuning")) {

                    if (v.getId() == button.getId()) {
                        selectedB = button;
                        changeColor(button);
                    }
                } else {
                    button.setOnClickListener(null);
                    selectedB = null;
                    return;
                }
            }

        }


    }

    private void setBackground() {
        for (Button button : Arrays.asList(s1, s2, s3, s4, s5, s6)) {
            int color = Color.parseColor("#404040");
            button.setBackgroundTintList(ColorStateList.valueOf(color));
        }


    }
    private void changeColor(Button button){
        int color;
        color = Color.parseColor("#A2FF86");
        button.setBackgroundTintList(ColorStateList.valueOf(color));
    }

   public void addTuning(String string, Context context){
       TuningsList.addToList(string, context);
       data = TuningsList.getList(context);
       onClickTuningsView();

   }

   public void replaceTuning(String string, Context context, int position){
       TuningsList.replaceToList(string, context,position);
       data = TuningsList.getList(context);
       onClickTuningsView();
   }

   public void removeTuning(Context context, int position){

       data = TuningsList.removeFromList(context,position);
       onClickTuningsView();
   }

}

