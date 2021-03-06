package com.example.manna_project;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.manna_project.MainAgreementActivity_Util.MannaUser;
import com.example.manna_project.MainAgreementActivity_Util.NoticeBoard.NoticeBoard_Chat;
import com.example.manna_project.MainAgreementActivity_Util.Promise;
import com.example.manna_project.MainAgreementActivity_Util.Routine;

import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class SettingPersonalRoutine extends AppCompatActivity implements View.OnClickListener {

    TableLayout routineTableLayout;
    Button backBtn, saveBtn;
    TimeTableButton timeTableBtn[][];
    FirebaseCommunicator firebaseCommunicator;
    MannaUser myInfo;
    ArrayList<Routine> routines;



    final static String TAG = "MANNAYC";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_personal_routine);

        timeTableBtn = new TimeTableButton[14][7];
        routineTableLayout = (TableLayout) findViewById(R.id.personal_routine_timetable_layout);
        setTimetable(routineTableLayout);
        backBtn = (Button) findViewById(R.id.setting_personal_routine_cancel);
        saveBtn = (Button) findViewById((R.id.setting_personal_routine_save));
        backBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);

        firebaseCommunicator = new FirebaseCommunicator(this);
        firebaseCommunicator.addCallBackListener(new FirebaseCommunicator.CallBackListener() {
            @Override
            public void afterGetUser(MannaUser mannaUser) {
                myInfo = mannaUser;
                routines = myInfo.getRoutineList();
                getUserData();
            }

            @Override
            public void afterGetPromise(Promise promise) {

            }

            @Override
            public void afterGetPromiseKey(ArrayList<String> promiseKeys) {

            }

            @Override
            public void afterGetFriendUids(ArrayList<String> friendList) {

            }

            @Override
            public void afterGetChat(NoticeBoard_Chat chat) {

            }
        });
        firebaseCommunicator.getUserById(firebaseCommunicator.getMyUid());
    }
    private void getUserData(){
        for(Routine routine : routines){
            int start = routine.getStartTime()-8;
            int end = routine.getEndTime()-8;
            int day = routine.getDay();

            while(start<end){
                timeTableBtn[start][day].isClicked = true;
                timeTableBtn[start][day].setBackgroundResource(R.drawable.edge_rectangular_blue);
                start++;
            }
        }

    }

    private void setTimetable(TableLayout table) {
        //먼저 사용자의 원래 일과정보를 가져와야함
        for (int i = 8; i < 22; i++) {
            TableRow row = new TableRow(this);
            LinearLayout.LayoutParams rowParams = new TableLayout.LayoutParams(MATCH_PARENT, 0);
            rowParams.weight = 1;
            rowParams.setMargins(0, 0, 0, 0);
            row.setLayoutParams(rowParams);
            table.addView(row);

            TextView time = new TextView(this);
            LinearLayout.LayoutParams textparams = new TableRow.LayoutParams(0, MATCH_PARENT);
            textparams.weight = 1;
            int t = i % 12;
            if (t == 0)
                t = 12;
            time.setText(t + "시");
            time.setTypeface(null, Typeface.BOLD);
            time.setGravity(1);
            textparams.setMargins(0, 0, 0, 0);
            time.setLayoutParams(textparams);
            row.addView(time);

            for (int j = 0; j < 7; j++) {
                TimeTableButton btn = new TimeTableButton(this);
                timeTableBtn[i - 8][j] = btn;
                LinearLayout.LayoutParams btnparams = new TableRow.LayoutParams(0, MATCH_PARENT);
                btnparams.weight = 1;
                btn.setBackgroundResource(R.drawable.edge_rectangular);
                btnparams.setMargins(0, 0, 0, 0);
                btn.setLayoutParams(btnparams);
                btn.setOnClickListener(this);
                row.addView(btn);
            }

        }

    }

    private class TimeTableButton extends AppCompatButton {

        public boolean isClicked;

        private TimeTableButton(Context context) {
            super(context);
            this.isClicked = false;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == backBtn) {
            finish();
        } else if (view == saveBtn) {
            //이벤트객체 만드는 함수 생성
            makeRoutineObject();
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            TimeTableButton temp = (TimeTableButton) view;
            if (temp.isClicked == true) {
                temp.setBackgroundResource(R.drawable.edge_rectangular);
                temp.isClicked = false;
            } else if (temp.isClicked == false) {
                temp.setBackgroundResource(R.drawable.edge_rectangular_blue);
                temp.isClicked = true;
            }
        }
    }

    private void makeRoutineObject() {       //버튼이 눌려있는 시간대에 시작시간과 끝시간을 가져옴
        int time = 8, day = 0;
        //시작 종료 넣을 자료구조
        ArrayList<Routine> routineArr = new ArrayList<>();

        while (time < 22 && day <= 6) {
            if (timeTableBtn[time - 8][day].isClicked) {
                int startTime = time, endTime;
                while (time < 22 && timeTableBtn[time - 8][day].isClicked) {
                    time++;
                }
                endTime = time;
                //Log.d("MANNAYC", startTime + "시 ~ " + endTime + "시");
                routineArr.add(new Routine(startTime,endTime,day));
            } else
                time++;

            if (time >= 22) {
                time = 8;
                day++;
            }
        }
        sendRoutine(routineArr);
    }
    private void sendRoutine(ArrayList<Routine> routineArr) {
        FirebaseCommunicator comunicator = new FirebaseCommunicator(null);
        String myUid = comunicator.getMyUid();
        comunicator.updateRoutine(myUid,routineArr);
    }
}

