package com.brian.stocks.predict;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.brian.stocks.R;
import com.brian.stocks.helper.SharedHelper;
import com.brian.stocks.helper.URLHelper;
import com.brian.stocks.predict.adapters.PredictAdapter;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyPredictsFragment extends Fragment {
    JSONArray data = new JSONArray();
    ArrayList<JSONObject> dataList = new ArrayList<JSONObject>();
    RecyclerView recyclerView;
    PredictAdapter mAdapter;
    LinearLayout emptyLayout;
    private LoadToast loadToast;

    public MyPredictsFragment(JSONArray my_post) {
        // Required empty public constructor
        this.data = my_post;
    }

    public static MyPredictsFragment newInstance(JSONArray my_post) {
        MyPredictsFragment fragment = new MyPredictsFragment(my_post);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_predict, container, false);

        loadToast = new LoadToast(getActivity());

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dataList.clear();
        for (int i = 0; i < data.length(); i ++) {
            try {
                dataList.add(data.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mAdapter = new PredictAdapter(dataList, 2);
        recyclerView.setAdapter(mAdapter);
        mAdapter.seListener(new PredictAdapter.Listener() {
            @Override
            public void onSelect(int position) {

            }

            @Override
            public void onCancel(final int position) {

                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setIcon(R.mipmap.ic_launcher_round)
                        .setTitle("Confirm Prediction Cancel")
                        .setMessage("Are you sure you want to cancel this prediction?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendCancel(position);
                            }
                        })
                        .show();
            }

            @Override
            public void onHandle(int position, int est) {

            }
        });

        emptyLayout = view.findViewById(R.id.empty_layout);
        if(data.length() > 0)
            emptyLayout.setVisibility(View.GONE);

        return view;
    }

    private void sendCancel(final int idx) {
        loadToast.show();
        JSONObject object = new JSONObject();
        try {
            object.put("id", data.getJSONObject(idx).getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AndroidNetworking.post(URLHelper.REQUEST_PREDICT_CANCEL)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("accept", "application/json")
                .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getContext(),"access_token"))
                .addJSONObjectBody(object)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response", "" + response);
                        loadToast.success();
                        if(response.optBoolean("success")) {
                            Toast.makeText(getContext(), response.optString("message"), Toast.LENGTH_SHORT).show();
                            data.remove(idx);
                        }
                        else
                            Toast.makeText(getContext(), response.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(ANError error) {
                        loadToast.error();
                        // handle error
                        Toast.makeText(getContext(), "Please try again. Network error.", Toast.LENGTH_SHORT).show();
                        Log.d("errorm", "" + error.getMessage());
                    }
                });
    }
}
