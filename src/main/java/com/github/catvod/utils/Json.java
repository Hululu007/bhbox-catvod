package com.github.catvod.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Json {

    private static class Loader {
        static volatile Json INSTANCE = new Json();
    }

    private static Json get() {
        return Json.Loader.INSTANCE;
    }

    private Gson gson;

    private Gson gsonWithNull;

    private Gson gsonLenient;

    public static Gson gson() {
        return get().gson==null?get().gson=new Gson():get().gson;
    }

    public static Gson gsonWithNull() {
        return get().gsonWithNull==null?get().gsonWithNull=new GsonBuilder()
                .serializeNulls()
                .create():get().gsonWithNull;
    }

    public static Gson gsonLenient() {
        return get().gsonLenient==null?get().gsonLenient=new GsonBuilder()
                .registerTypeAdapter(int.class, LENIENT_INT)
                .registerTypeAdapter(Integer.class, LENIENT_INT)
                .create():get().gsonLenient;
    }

    public static JsonElement parse(String json) {
        try {
            return JsonParser.parseString(json);
        } catch (Throwable e) {
            return new JsonParser().parse(json);
        }
    }

    public static boolean valid(String text) {
        try {
            new JSONObject(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean invalid(String text) {
        return !valid(text);
    }

    public static String safeString(JsonObject obj, String key) {
        try {
            return obj.getAsJsonPrimitive(key).getAsString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public static String safeString(JsonObject obj, String key, String defaultVal) {
        try {
            if (obj.has(key))
                return obj.getAsJsonPrimitive(key).getAsString().trim();
            else
                return defaultVal;
        } catch (Throwable th) {
        }
        return defaultVal;
    }

    public static int safeInt(JsonObject obj, String key, int defaultVal) {
        try {
            if (obj.has(key))
                return obj.getAsJsonPrimitive(key).getAsInt();
            else
                return defaultVal;
        } catch (Throwable th) {
        }
        return defaultVal;
    }

    public static List<String> safeListString(JsonObject obj, String key) {
        List<String> result = new ArrayList<>();
        if (!obj.has(key)) return result;
        if (obj.get(key).isJsonObject()) result.add(safeString(obj, key));
        else for (JsonElement opt : obj.getAsJsonArray(key)) result.add(opt.getAsString());
        return result;
    }

    public static List<JsonElement> safeListElement(JsonObject obj, String key) {
        List<JsonElement> result = new ArrayList<>();
        if (!obj.has(key)) return result;
        if (obj.get(key).isJsonObject()) result.add(obj.get(key).getAsJsonObject());
        for (JsonElement opt : obj.getAsJsonArray(key)) result.add(opt.getAsJsonObject());
        return result;
    }

    public static JsonObject safeObject(JsonElement element) {
        try {
            if (element.isJsonPrimitive()) element = parse(element.getAsJsonPrimitive().getAsString());
            return element.getAsJsonObject();
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    public static JsonObject toObject(String json) {
        try {
            JsonElement element = parse(json);
            if (element.isJsonPrimitive()) element = parse(element.getAsJsonPrimitive().getAsString());
            return element.getAsJsonObject();
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    public static Map<String, String> toMap(String json) {
        return TextUtils.isEmpty(json) ? null : toMap(parse(json));
    }

    public static Map<String, String> toMap(JsonElement element) {
        Map<String, String> map = new HashMap<>();
        JsonObject object = safeObject(element);
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) map.put(entry.getKey(), safeString(object, entry.getKey()));
        return map;
    }

    public static Map<String, String> toMap(Object obj) {
        return toMap(toJson(obj));
    }

    public static JsonObject toObject(Map<String, String> map) {
        JsonObject object = new JsonObject();
        for (String key : map.keySet()) object.addProperty(key, map.get(key));
        return object;
    }

    public static JsonObject toObject(Object obj) {
        return gson().toJsonTree(obj).getAsJsonObject();
    }

    public static String toJson(Object obj) {
        return gson().toJson(obj);
    }

    public static <T> T fromJson(String his, Type type) {
        return gson().fromJson(his,type);
    }
    public static boolean isObj(String text) {
        try {
            if (TextUtils.isEmpty(text)) return false;
            new JSONObject(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isArray(String text) {
        try {
            if (TextUtils.isEmpty(text)) return false;
            new JSONArray(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 部分源把 total/page 等数值字段输出为空字符串，Gson 默认按 int 解析会抛
    // NumberFormatException 导致整包 JSON 失败；int 字段遇空串/null/非数字一律按 0
    private static final TypeAdapter<Number> LENIENT_INT = new TypeAdapter<Number>() {
        @Override
        public void write(JsonWriter out, Number value) throws IOException {
            if (value == null) out.nullValue();
            else out.value(value.intValue());
        }

        @Override
        public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return 0;
            }
            String s = in.nextString().trim();
            if (s.isEmpty()) return 0;
            try {
                return (int) Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    };
}
