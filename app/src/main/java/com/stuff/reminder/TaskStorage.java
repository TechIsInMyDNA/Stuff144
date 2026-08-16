package com.stuff.reminder;

import android.content.Context;
import java.util.*;

public class TaskStorage {
    private static final String PREF = "stuff_pref";
    private static final String KEY_TASKS = "tasks";

    public static class Item {
        public int id;
        public String text;
        public boolean done;
        public long dueDate;
        public Item(int id, String text, boolean done, long dueDate) {
            this.id = id; this.text = text; this.done = done; this.dueDate = dueDate;
        }
    }

    public static List<Item> getTasks(Context context) {
        Set<String> set = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getStringSet(KEY_TASKS, new HashSet<>());
        List<Item> list = new ArrayList<>();
        for (String s : set) {
            String[] p = s.split(":::", 4);
            if (p.length >= 4) list.add(new Item(Integer.parseInt(p[0]), p[1], Boolean.parseBoolean(p[2]), Long.parseLong(p[3])));
        }
        return list;
    }

    public static void saveTasks(Context context, List<Item> list) {
        Set<String> set = new HashSet<>();
        for (Item i : list) set.add(i.id + ":::" + i.text + ":::" + i.done + ":::" + i.dueDate);
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putStringSet(KEY_TASKS, set).apply();
    }

    public static void addTask(Context context, String text, long due) {
        List<Item> list = getTasks(context);
        list.add(new Item((int)(System.currentTimeMillis()%100000), text, false, due));
        saveTasks(context, list);
    }
}
