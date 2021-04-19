package com.brian.stocks.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.brian.stocks.helper.URLHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;

public class CoinInfo implements Parcelable {
    private JSONObject data;
    private String name;
    private String symbol;
    private String id;
    private String icon;
    private String balance;
    private Double price;
    private String changeRate;
    private String type;
    private int buynow;
    public CoinInfo(JSONObject item) {
        data = item;
        name = data.optString("coin_name");
        symbol = data.optString("coin_symbol");
        id = data.optString("id");
        icon = data.optString("icon");
        if(!icon.startsWith("http"))
            icon = URLHelper.base + icon;
        balance = new DecimalFormat("#,###.####").format(data.optDouble("balance"));
        price = data.optDouble("coin_rate");
        changeRate = new DecimalFormat("#.####").format(data.optDouble("change_rate"));
        type = data.optString("type");
        buynow = data.optInt("buy_now");
    }

    public JSONObject getData() {
        return data;
    }

    public String getCoinName() {
        return name;
    }

    public String getCoinId() {
        return id;
    }

    public String getCoinIcon() {
        return icon;
    }

    public String getCoinSymbol() {
        return symbol;
    }

    public String getCoinBalance() {
        return balance;
    }

    public Double getCoinRate() {
        try {
            return data.getDouble("coin_rate");
        } catch (JSONException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public Double getCoinExchangeRate() {
        try {
            return data.getDouble("exchange_rate");
        } catch (JSONException e) {
            return 0.0;
        }
    }

    public String getCoinUsdc() {
        String balance = "0.0";
        try {
            balance = new DecimalFormat("#,###.##").format(data.getDouble("est_usdc"));
        } catch (JSONException e) {
        }
        return balance;
    }

    public String getWithdrawalFee() {
        try {
            return new DecimalFormat("#.######").format(data.getDouble("withdrawal_fee"));
        } catch (JSONException e) {
            return "";
        }
    }

    public String getCoinEffect() {
        return changeRate;
    }

    public Boolean getTradable() {
        try {
            return data.getBoolean("tradable");
        } catch (JSONException e) {
            return false;
        }
    }

    public String getType() {
        return type;
    }

    public String getStellarSecret() {
        try {
            return data.getString("stellar_secret");
        } catch (JSONException e) {
            return "";
        }
    }

    public int getBuyNowOption() {

            return buynow;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(symbol);
        dest.writeString(id);
        dest.writeString(icon);
        dest.writeString(balance);
        dest.writeDouble(price);
        dest.writeString(changeRate);
        dest.writeString(type);
        dest.writeInt(buynow);
    }

    public static final Parcelable.Creator<CoinInfo> CREATOR = new Parcelable.Creator<CoinInfo>() {

        @Override
        public CoinInfo createFromParcel(Parcel source) {
            return new CoinInfo(source);
        }

        @Override
        public CoinInfo[] newArray(int size) {
            return new CoinInfo[size];
        }
    };

    private CoinInfo(Parcel in) {
        name = in.readString();
        symbol = in.readString();
        id = in.readString();
        icon = in.readString();
        balance = in.readString();
        price = in.readDouble();
        changeRate = in.readString();
        type = in.readString();
        buynow = in.readInt();
    }
}

