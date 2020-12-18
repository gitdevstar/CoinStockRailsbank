package com.brian.stocks.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.brian.stocks.R;
import com.brian.stocks.adapters.BottomCoinAdapter;
import com.brian.stocks.helper.BigDecimalDouble;
import com.brian.stocks.helper.SharedHelper;
import com.brian.stocks.helper.URLHelper;
import com.brian.stocks.home.adapters.OrderAdapter;
import com.brian.stocks.home.adapters.OrderHistoryAdapter;
import com.brian.stocks.home.adapters.OrderBookAsksAdapter;
import com.brian.stocks.home.adapters.OrderBookBidsAdapter;
import com.brian.stocks.model.CoinInfo;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import net.steamcrafted.loadtoast.LoadToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener;

public class CoinExchangeFragment extends Fragment {
    private Button mBtnTrade;
    private TabLayout tabLayout;
    private EditText mEditQuantity, mEditPrice;
    private TextView mTextCoinBuy2, mTextCoinSell2, mTextCoinBuyBalance, mTextCoinSellBalance, mTextOutputTrade, mTextAsksTotalUSD, mTextBidsTotalUSD, mTextPriceUSD;
    private static String CoinSymbol;
    private View mView;
    private DecimalFormat df = new DecimalFormat("#.########");
    private LoadToast loadToast;
    private RecyclerView orderView, orderHistoryView, orderbookAsksView, orderbookBidsView;
    OrderAdapter orderAdapter;
    OrderHistoryAdapter orderHistoryAdapter;
    OrderBookBidsAdapter orderBookBidsAdapter2;
    OrderBookAsksAdapter orderbookAsksAdapter;
    private ArrayList<JSONObject> bidsList = new ArrayList<>();
    private ArrayList<JSONObject> asksList = new ArrayList<>();
    private ArrayList<JSONObject> ordersList = new ArrayList<>();
    private ArrayList<JSONObject> ordersHistoryList = new ArrayList<>();
    private String mPair, selType;
    private Float mBTCUSD_rate, mBTCXMT_rate;
    double buyCoinPrice=0, sellCoinPrice=0;
    private JSONArray bids = null;
    private JSONArray asks = null;
    final Handler h = new Handler();
    int select = 0;

    public CoinExchangeFragment() {
        // Required empty public constructor
    }

    public static CoinExchangeFragment newInstance(String symbol) {
        CoinExchangeFragment fragment = new CoinExchangeFragment();
        CoinSymbol = symbol;
        return fragment;
    }

    public static CoinExchangeFragment newInstance() {
        CoinExchangeFragment fragment = new CoinExchangeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadToast = new LoadToast(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_coin_exchange, container, false);

        tabLayout = mView.findViewById(R.id.tabLayout);
        TabLayout.Tab tabsel = tabLayout.getTabAt(0);
        tabsel.select();
        selType = "buy";


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() > 0) {
                    selType = "sell";
                } else {
                    selType = "buy";
                }
                try {
                    mEditPrice.setText(df.format(getPrice()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        initComponents();

        initListeners();

        h.postDelayed(new Runnable()
        {
            //private long time = 0;

            @Override
            public void run()
            {
                // do stuff then
                // can call h again after work!
                //Log.d("TimerExample", "Going for... " + time);
                getData();
                h.postDelayed(this, 10000);
            }
        }, 10000); // 1 second delay (takes millis)
        return mView;
    }

    private void getData() {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("pair", mPair);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("exchange param", jsonObject.toString()+" -> "+URLHelper.COIN_REALEXCHANGE_DATA);
        Log.d("token",SharedHelper.getKey(getContext(),"access_token"));
        if(getContext() != null)
            AndroidNetworking.post(URLHelper.COIN_REALEXCHANGE_DATA)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getContext(),"access_token"))
                    .addJSONObjectBody(jsonObject)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("coin assets response", "" + response.toString());
                            if (!response.has("success")) {
                                return;
                            }


                            ordersList.clear();
                            ordersHistoryList.clear();
                            bidsList.clear();
                            asksList.clear();

                            JSONObject responseObj = null;
                            try {
                                mTextCoinBuyBalance.setText(df.format(Float.parseFloat(response.getString("coin2_balance"))));
                                mTextCoinSellBalance.setText(df.format(Float.parseFloat(response.getString("coin1_balance"))));
                                responseObj = response.getJSONObject("orders");
                                if(responseObj != null) {
                                    JSONArray orders = responseObj.getJSONArray("active");
                                    JSONArray ordersHistory = responseObj.getJSONArray("history");
                                    for (int i = 0; i < orders.length(); i++) {
                                        try {
                                            ordersList.add(orders.getJSONObject(i));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    for (int i = 0; i < ordersHistory.length(); i++) {
                                        try {
                                            ordersHistoryList.add(ordersHistory.getJSONObject(i));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    orderHistoryAdapter.notifyDataSetChanged();
                                    orderAdapter.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                bids = response.getJSONArray("bids");
                                if(bids != null) {
                                    for (int i = 0; i < bids.length(); i++) {
                                        try {
                                            bidsList.add(bids.getJSONObject(i));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    orderBookBidsAdapter2.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                asks = response.getJSONArray("asks");
                                if(asks != null) {
                                    for (int i = 0; i < asks.length(); i++) {
                                        try {
                                            asksList.add(asks.getJSONObject(i));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    orderbookAsksAdapter.notifyDataSetChanged();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                mBTCUSD_rate = Float.parseFloat(response.getString("btc_rate"));
                                mBTCXMT_rate = mBTCUSD_rate * Float.parseFloat(String.valueOf(mEditPrice.getText()));
                                mTextPriceUSD.setText("$"+df.format(mBTCXMT_rate));
                                mTextAsksTotalUSD.setText("Asks ($"+df.format(Float.parseFloat(response.getString("asks_total")))+")");
                                mTextBidsTotalUSD.setText("Bids ($"+df.format(Float.parseFloat(response.getString("bids_total")))+")");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            try {
                                mEditPrice.setText(df.format(getPrice()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                        @Override
                        public void onError(ANError error) {
                            // handle error

                            Log.d("errorm", "" + error.getMessage()+" responde: "+error.getResponse());
                        }
                    });
    }


    private void sendData() {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("pair", mPair);
            jsonObject.put("type", selType);
            jsonObject.put("quantity", df.format(Float.parseFloat(mEditQuantity.getText().toString())));
            jsonObject.put("price", df.format(Float.parseFloat(mEditPrice.getText().toString())));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(getContext() != null)
            AndroidNetworking.post(URLHelper.COIN_REALEXCHANGE)
                    .addHeaders("Content-Type", "application/json")
                    .addHeaders("accept", "application/json")
                    .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getContext(),"access_token"))
                    .addJSONObjectBody(jsonObject)
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("coin post response", "" + response.toString());
                            if (!response.has("success")) {
                                /*
                                try {

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                 */
                                return;
                            }

                            if (response.has("filled")) {
                                Toast.makeText(getContext(), "Order filled.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Order created, waiting to be filled.", Toast.LENGTH_SHORT).show();
                            }


                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error

                            Log.d("errorpost", "" + error.getMessage()+" responde: "+error.getResponse());
                        }
                    });
    }

    private void initComponents() {
        mBtnTrade = mView.findViewById(R.id.btn_coin_trade);
        mPair = "BTC-XMT";

        mTextPriceUSD = mView.findViewById(R.id.price_usd);
        mEditQuantity = mView.findViewById(R.id.edit_quantity);
        mEditPrice = mView.findViewById(R.id.edit_price);
        mTextAsksTotalUSD = mView.findViewById(R.id.asks_total_usd);
        mTextBidsTotalUSD = mView.findViewById(R.id.bids_total_usd);
        try {
            mEditPrice.setText(df.format(getPrice()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTextOutputTrade = mView.findViewById(R.id.output_trade);
        mTextCoinBuy2 = mView.findViewById(R.id.coin_buyy);
        mTextCoinSell2 = mView.findViewById(R.id.coin_selll);

        mTextCoinBuyBalance = mView.findViewById(R.id.coin_buy_balance);
        mTextCoinSellBalance = mView.findViewById(R.id.coin_sell_balance);

        orderView = mView.findViewById(R.id.orders_view);
        orderAdapter = new OrderAdapter(ordersList);
        orderView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderView.setAdapter(orderAdapter);

        orderHistoryView = mView.findViewById(R.id.orders_history_view);
        orderHistoryAdapter = new OrderHistoryAdapter(ordersHistoryList);
        orderHistoryView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderHistoryView.setAdapter(orderHistoryAdapter);

        orderbookAsksView = mView.findViewById(R.id.orderbook_asks_view);
        orderbookAsksAdapter = new OrderBookAsksAdapter(asksList);
        orderbookAsksView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderbookAsksView.setAdapter(orderbookAsksAdapter);

        orderbookBidsView = mView.findViewById(R.id.orderbook_bids_view);
        orderBookBidsAdapter2 = new OrderBookBidsAdapter(bidsList);
        orderbookBidsView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderbookBidsView.setAdapter(orderBookBidsAdapter2);


    }



    private void initListeners() {

        mBtnTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
                /*
                if(mEditSellingAmount.getText().toString().equals("")||
                        mEditBuyingCoin.getText().toString().equals("") || mEditSellingCoin.getText().toString().equals("")) {
                    Toast.makeText(getContext(), "Please fillout all inputs", Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setIcon(R.mipmap.ic_launcher_round)
                            .setTitle("Confirm Exchange")
                            .setMessage("Please confirm your transaction? Exchange fees is 0.50%. If you hold 200XMT fees is 0.25%.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doExchange();
                                }
                            }).show();
                }
                */

            }
        });

        mEditQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String price=charSequence.toString();
                if(!price.equalsIgnoreCase(".")) {
                    //            if (!price.equalsIgnoreCase("")) {
                    //                mtvBuyingEstQty.setText(BigDecimalDouble.newInstance().multify(price, buyCoinPrice));
                    //            } else mtvBuyingEstQty.setText("0.00");
                }
                calculate();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mEditQuantity.getText().toString().equalsIgnoreCase("")) {
                    return;
                }
                calculate();

            }
        });

        mEditPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mEditPrice.getText().toString().equalsIgnoreCase("")) {
                    return;
                }
                calculate();

            }
        });
    }

    private double getPrice() throws JSONException {
        if(selType.equalsIgnoreCase("buy")) {
            if (asksList.isEmpty()) {
                return 0.00000001;
            } else {
                JSONObject item = asksList.get(bidsList.size() - 1);
                return Float.parseFloat(df.format(Float.parseFloat(item.optString("price"))));
            }
        } else {
            if (bidsList.isEmpty()) {
                return 0.00000001;
            } else {
                JSONObject item = bidsList.get(0);
                return Float.parseFloat(df.format(Float.parseFloat(item.optString("price"))));
            }
        }
    }

    private void calculate() {
        Log.d("calculate","Quantity: "+mEditQuantity.getText().toString()+" Price: "+mEditPrice.getText().toString());
        String fixval1, fixval2;
        fixval1 = mEditQuantity.getText().toString();
        if (fixval1.isEmpty()) {
            fixval1 = "0.00000001";
        }
        fixval2 = mEditPrice.getText().toString();
        if (fixval2.isEmpty()) {
            fixval2 = "0.00000001";
        }
        Float calc = Float.parseFloat(fixval1) * Float.parseFloat(fixval2);
    //Buying "+df.format(Float.parseFloat(fixval1))+" XMT, s
        if(selType.equalsIgnoreCase("buy")) {
            mTextOutputTrade.setText("Spending "+df.format(calc)+" BTC ($"+df.format(calc * mBTCUSD_rate)+")");
        } else {
            mTextOutputTrade.setText("Getting "+df.format(calc)+" BTC ($"+df.format(calc * mBTCUSD_rate)+")");
        }
    }

    private void initFormat() {
        //mPair.setText("BTC-XMT");
        /*
        mEditSellingAmount.setText("");
        mtvBuyRateQty.setText("1.00");
        mtvBuyRateCoin.setText("BTC");
        mEditBuyingCoin.setText("");
        mtvBuyingEstQty.setText("0.00");

         */
    }

    private void doExchange() {
        loadToast.show();
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("pair", mPair);
            jsonObject.put("quantity", mEditQuantity.getText().toString());
            jsonObject.put("price", mEditPrice.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("exchange param", jsonObject.toString());
        if(getContext() != null)
            AndroidNetworking.post(URLHelper.COIN_REALEXCHANGE)
                .addHeaders("Content-Type", "application/json")
                .addHeaders("accept", "application/json")
                .addHeaders("Authorization", "Bearer " + SharedHelper.getKey(getContext(),"access_token"))
                .addJSONObjectBody(jsonObject)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response", "" + response);
                        loadToast.success();
                        if(response.optBoolean("success")) {
                            Toast.makeText(getContext(), response.optString("message"), Toast.LENGTH_SHORT).show();

                        }else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setIcon(R.mipmap.ic_launcher_round)
                                    .setTitle("Error")
                                    .setMessage(response.optString("message"))
                                    .show();
                        }
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


    @Override
    public void onStart() {
        super.onStart();
    }
}