package com.example.samiu.health_monitoring_assistant;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static com.google.android.gms.internal.zzagr.runOnUiThread;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private LineChart mChart;
    DatabaseReference mDatabase;
    DatabaseReference notificationDatabase;
    DatabaseReference findRelativeDatabase;
    FirebaseAuth mAuth;
    String user_id, relativeMoileNo;
    ArrayList<Long> data;
    TextView heart_rate, blood_pressure, temperature;
    String relative_user_id;
    Long time;
    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        user_id = this.getArguments().getString("user_id");
        //relativeMoileNo = this.getArguments().getString("relativeMobileNo");

        heart_rate = view.findViewById(R.id.heart_rate_value_id);
        blood_pressure = view.findViewById(R.id.blood_pressure_value_id);
        temperature = view.findViewById(R.id.temperature_value_id);


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        findRelativeDatabase = FirebaseDatabase.getInstance().getReference().child("Users");


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    if(child.getKey().equals("relativeMobileNo")){
                        relativeMoileNo = (String) child.getValue();
                        System.out.println("relative_dash_mobile: " + relativeMoileNo);
                        findRelativeDatabase.orderByChild("ownMobileNo").equalTo(relativeMoileNo).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot child: dataSnapshot.getChildren()){
                                    relative_user_id = child.getKey();
                                    System.out.println("relative id: " + relative_user_id);
                                    HashMap<String, String> notificationData = new HashMap<>();
                                    notificationData.put("from", user_id);
                                    notificationData.put("type", "request");
                                    notificationDatabase.child(relative_user_id).push().setValue(notificationData)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                    }
                                System.out.println("key: " + dataSnapshot.getKey() + " " + dataSnapshot.getChildrenCount());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//        findRelativeDatabase.orderByChild("ownMobileNo").equalTo(relativeMoileNo).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot child: dataSnapshot.getChildren()){
//                    //relative_user_id = child.getKey();
//                    System.out.println("relative id: " + child.getKey());
//                }
//                System.out.println("key: " + dataSnapshot.getKey() + " " + dataSnapshot.getChildrenCount());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        //data = new ArrayList<>();
        mChart = view.findViewById(R.id.lineChartId);
        //mChart.setOnChartGestureListener(this);
        //mChart.setOnChartValueSelectedListener(this);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(false);
        mChart.setHighlightPerTapEnabled(true);
        mChart.setHighlightPerDragEnabled(true);
        mChart.setTouchEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        //mChart.setBackgroundColor(Color.LTGRAY);

        /*ArrayList<Entry> yValues = new ArrayList<>();
        yValues.add(new Entry(0, 60f));
        yValues.add(new Entry(1, 65f));
        yValues.add(new Entry(2, 70f));
        yValues.add(new Entry(3, 80f));
        yValues.add(new Entry(4, 40f));
        yValues.add(new Entry(5, 90f));


        LineDataSet set1 = new LineDataSet(yValues, "Data set 1");
        set1.setFillAlpha(110);
        set1.setLineWidth(2f);
        set1.setValueTextSize(10f);
        set1.setValueTextColor(Color.GREEN);
        ArrayList<ILineDataSet> datasets = new ArrayList<>();
        datasets.add(set1);
        LineData data = new LineData(datasets);
        mChart.setData(data);*/

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        Legend l = mChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis x1 = mChart.getXAxis();
        x1.setEnabled(false);
        x1.setTextColor(Color.WHITE);
        x1.setDrawGridLines(false);
        x1.setAvoidFirstLastClipping(true);

        YAxis y1 = mChart.getAxisLeft();
        y1.setTextColor(Color.WHITE);
        y1.setDrawGridLines(true);
        //y1.setAxisMaxValue(550f);

        YAxis y2 = mChart.getAxisRight();
        y2.setEnabled(false);
        return view;
    }
    private void addEntryTo(Entry entry){
        LineData data = mChart.getData();
        if(data != null){
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            if(set == null){
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(entry, 0);
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRange(1000, 1000);
            mChart.moveViewToX(data.getXMax());
        }
    }


    @Override
    public void onResume() {
        super.onResume();
//        final Long sec = time / 1000;
//        Long sampleRate = data.size() / sec;
//        final Long stepSize = 1 / sampleRate;

        data = new ArrayList<>();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mDatabase.keepSynced(true);
        mDatabase.child("ECG Data").child("Data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("children count: " + dataSnapshot.getChildrenCount());
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    data.add((Long) child.getValue());
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < data.size(); i++){
                            final int j = i;
                            runOnUiThread(new Runnable(){

                                @Override
                                public void run() {
                                    addEntryTo(new Entry(j, data.get(j)));
                                    System.out.println(data.get(j));
                                }
                            });
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //System.out.println(data.size());

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//            for (int i = 0; i < data.size(); i++){
//                final int j = i;
//                runOnUiThread(new Runnable(){
//
//                    @Override
//                    public void run() {
//                    addEntryTo(new Entry(j, data.get(j)));
//                        System.out.println(data.get(j));
//                    }
//                });
//                try {
//                    Thread.sleep(600);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            }
//        }).start();
    }

    private LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null, "ECG graph");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        //set.setColor(ColorTemplate.getHoloBlue());
        //set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        set.setCircleSize(0f);
        //set.setFillAlpha(65);
        //set.setFillColor(ColorTemplate.getHoloBlue());
        //set.setHighLightColor(Color.rgb(244, 117, 177));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(0f);
        return set;
    }
}
