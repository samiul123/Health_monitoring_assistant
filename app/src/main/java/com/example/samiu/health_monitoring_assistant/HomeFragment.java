package com.example.samiu.health_monitoring_assistant;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;

import static com.google.android.gms.internal.zzagr.runOnUiThread;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    ImageView imageView;
    String user_id;
    DatabaseReference dataRef;
    String imageUri;
    TextView greetView;
    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        imageView = view.findViewById(R.id.imageId);
        greetView = view.findViewById(R.id.greetId);
        user_id = this.getArguments().getString("user_id");
        //System.out.println("ID: " + user_id);
        dataRef = FirebaseDatabase.getInstance().getReference().child("Users");
        dataRef.keepSynced(true);
        dataRef.child(user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child: dataSnapshot.getChildren()){
                    if(child.getKey().equals("image")){
                        imageUri = (String) child.getValue();
                        Picasso.with(view.getContext()).load(imageUri).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(view.getContext()).load(imageUri).into(imageView);
                            }
                        });
                    }
                    else if(child.getKey().equals("name")){
                        greetView.append("Hi, " + child.getValue());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return view;
    }



}
