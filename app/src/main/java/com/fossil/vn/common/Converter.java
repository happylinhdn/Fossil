package com.fossil.vn.common;

import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Converter {
    static DateFormat df = new SimpleDateFormat(Constants.TIME_STAMP_FORMAT);

    @TypeConverter
    public static Date fromTimestamp(String value) {
        if (!TextUtils.isEmpty(value)) {
            try {
                return df.parse(value);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    @TypeConverter
    public static String fromDate(Date date) {
        String dateInString = "";
        if (date != null) {
            dateInString = df.format(date.getTime());
        }
        return dateInString;
    }

    @TypeConverter
    public static List<Node> fromString(String value) {
        if (value == null) {
            return Collections.emptyList();
        }

        Type listType = new TypeToken<List<Node>>() {}.getType();

        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String nodesToString(List<Node> nodes) {
        return new Gson().toJson(nodes);
    }
}
