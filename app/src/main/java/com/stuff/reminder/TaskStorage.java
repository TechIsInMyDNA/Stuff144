package com.stuff.reminder;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskStorage {
    private static final String PREF = "stuff_pref";
    private static final String KEY_TASKS = "tasks";
    private static final String KEY_TITLE = "title";

    public static class Item {
        public int id;
        public String text;
        public boolean done;
        public Item(int id, String text, boolean done) {
            this.id = id;
            this.text = text;
            this.done = done;
        }
    }

    public static String getWidgetTitle(Context context) {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_TITLE, "P1 - IMPORTANT AND URGENT");
    }

    public static void setWidgetTitle(Context context, String title) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY_TITLE, title).apply();
    }

    public static List<Item> getTasks(Context context) {
        Set<String> set = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getStringSet(KEY_TASKS, new HashSet<String>());
        List<Item> list = new ArrayList<>();
        for (String s : set) {
            String[] parts = s.split(":::", 3);
            if (parts.length >= 3) {
                list.add(new Item(Integer.parseInt(parts[0]), parts[1], Boolean.parseBoolean(parts[2])));
            }
        }
        return list;
    }

    public static void saveTasks(Context context, List<Item> list) {
        Set<String> set = new HashSet<>();
        for (Item item : list) {
            set.add(item.id + ":::" + item.text + ":::" + item.done);
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putStringSet(KEY_TASKS, set).apply();
    }

    public static void addTask(Context context, String text) {
        List<Item> list = getTasks(context);
        list.add(new Item((int) (System.currentTimeMillis() % 10000000), text, false));
        saveTasks(context, list);
    }

    public static void toggleTask(Context context, int id) {
        List<Item> list = getTasks(context);
        for (Item it : list) {
            if (it.id == id) {
                it.done = !it.done;
                break;
            }
        }
        saveTasks(context, list);
    }
}
