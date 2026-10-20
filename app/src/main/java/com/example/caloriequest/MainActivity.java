package com.example.caloriequest;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private EditText mealName, mealCalories, mealProtein, mealQuantity;
    private TextView calorieTotal, proteinTotal, xpTotal, mealHistory, streakText;
    private TextView highestCaloriesText, highestProteinText;
    private TextView calorieGoalText, proteinGoalText, levelText;
    private ProgressBar calorieProgress, proteinProgress, levelProgress;
    private LinearLayout previousDaysContainer;
    private Spinner foodSpinner;

    private double totalCalories = 0;
    private double totalProtein = 0;
    private int totalXP =0;

    private int streakCount = 0;
    private String lastLoggedDate = "";
    private JSONArray foodEntries = new JSONArray();

    private android.content.SharedPreferences prefs;
    private static final String PREFS_NAME = "CalorieQuestData";

    private final String[] foodNames = {
            "Select a food",
            "Guava",
            "Dry oats",
            "Dry green gram",
            "Almonds",
            "Low-calorie almonds",
            "Cashews",
            "Low-calorie cashews",
            "Dates",
            "Carrot",
            "Cucumber",
            "Cooked white rice",
            "Cooked chicken breast",
            "Boiled egg",
            "Raw papaya",
            "Boiled sweet potato",
            "Whole cow's milk",
            "Chapati (without oil/ghee)"
    };

    // Calories and protein are per 100 g (or 100 ml for milk).
    private final int[] foodCalories = {
            0, 68, 389, 347, 579, 70, 553, 85, 282,
            21, 15, 130, 165, 78, 43, 76, 65, 110
    };

    private final double[] foodProtein = {
            0.0, 2.6, 16.9, 24.0, 21.2, 2.5, 18.2, 2.7,
            2.5, 0.5, 0.7, 2.7, 31.0, 6.3, 0.5, 1.4, 3.4, 3.5
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Connect views from activity_main.xml.
        mealName = findViewById(R.id.mealName);
        mealCalories = findViewById(R.id.mealCalories);
        mealProtein = findViewById(R.id.mealProtein);
        mealQuantity = findViewById(R.id.mealQuantity);

        calorieTotal = findViewById(R.id.calorieTotal);
        proteinTotal = findViewById(R.id.proteinTotal);
        xpTotal = findViewById(R.id.xpTotal);
        mealHistory = findViewById(R.id.mealHistory);
        streakText = findViewById(R.id.streakText);

        calorieGoalText = findViewById(R.id.calorieGoalText);
        proteinGoalText = findViewById(R.id.proteinGoalText);
        levelText = findViewById(R.id.levelText);

        calorieProgress = findViewById(R.id.calorieProgress);
        proteinProgress = findViewById(R.id.proteinProgress);
        levelProgress = findViewById(R.id.levelProgress);

        highestCaloriesText = findViewById(R.id.highestCaloriesText);
        highestProteinText = findViewById(R.id.highestProteinText);
        previousDaysContainer = findViewById(R.id.previousDaysContainer);
        TextView previousDaysTitle = findViewById(R.id.previousDaysTitle);

        previousDaysTitle.setOnClickListener(v -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance();

            new android.app.DatePickerDialog(
                    MainActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        java.util.Calendar selectedDate =
                                java.util.Calendar.getInstance();

                        selectedDate.set(year, month, dayOfMonth);

                        java.text.SimpleDateFormat dateFormat =
                                new java.text.SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        java.util.Locale.getDefault()
                                );

                        String date = dateFormat.format(selectedDate.getTime());

                        showFoodForDate(date);
                    },
                    calendar.get(java.util.Calendar.YEAR),
                    calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
            ).show();
        });

        foodSpinner = findViewById(R.id.foodSpinner);

        Button addMealButton = findViewById(R.id.addMealButton);
        addMealButton.setOnClickListener(v -> addFood());

        setupFoodSpinner();
        loadData();


    }
    private void showFoodForDate(String selectedDate) {
        StringBuilder logs = new StringBuilder();

        for (int i = 0; i < foodEntries.length(); i++) {
            org.json.JSONObject entry = foodEntries.optJSONObject(i);

            if (entry == null) {
                continue;
            }

            if (selectedDate.equals(entry.optString("date", ""))) {
                logs.append(entry.optString("name", "Food"))
                        .append(" (")
                        .append(entry.optString("quantity", ""))
                        .append(") - ")
                        .append(entry.optDouble("calories", 0))
                        .append(" kcal, ")
                        .append(entry.optDouble("protein", 0))
                        .append(" g protein\n");
            }
        }

        String message = logs.length() == 0
                ? "No food logs found for " + selectedDate
                : logs.toString();

        new android.app.AlertDialog.Builder(this)
                .setTitle("🍀 Food Logs — " + selectedDate)
                .setMessage(message)
                .setPositiveButton("Close", null)
                .show();
    }
    private void setupFoodSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                foodNames
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        foodSpinner.setAdapter(adapter);

        foodSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id
                    ) {
                        if (position > 0) {
                            mealName.setText(foodNames[position]);
                            mealCalories.setText(String.valueOf(foodCalories[position]));
                            mealProtein.setText(String.valueOf(foodProtein[position]));
                        } else {
                            mealName.setText("");
                            mealCalories.setText("");
                            mealProtein.setText("");
                        }
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent
                    ) {
                        // No action needed.
                    }
                }
        );
    }

    private void addFood() {
        String name = mealName.getText().toString().trim();
        String caloriesText = mealCalories.getText().toString().trim();
        String proteinText = mealProtein.getText().toString().trim();
        String quantityText = mealQuantity.getText().toString().trim();

        if (name.isEmpty() || caloriesText.isEmpty()
                || proteinText.isEmpty() || quantityText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double caloriesPer100g = Double.parseDouble(caloriesText);
            double proteinPer100g = Double.parseDouble(proteinText);
            double quantity = Double.parseDouble(quantityText);

            if (caloriesPer100g < 0 || proteinPer100g < 0) {
                Toast.makeText(this, "Values cannot be negative",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (quantity <= 0) {
                Toast.makeText(this, "Quantity must be greater than 0",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            double actualCalories = caloriesPer100g * quantity / 100.0;
            double actualProtein = proteinPer100g * quantity / 100.0;

            totalCalories += actualCalories;
            totalProtein += actualProtein;
            totalXP += 10;

            JSONObject entry = new JSONObject();
            String today = getTodayDate();

            entry.put("date", today);
            entry.put("name", name);
            entry.put("quantity", quantity);
            entry.put("calories", actualCalories);
            entry.put("protein", actualProtein);
            foodEntries.put(entry);

            updateStreak();
            saveData();
            refreshDisplays();

            mealName.setText("");
            mealCalories.setText("");
            mealProtein.setText("");
            mealQuantity.setText("");
            foodSpinner.setSelection(0);

            Toast.makeText(this, "Food added! +10 XP 🎉",
                    Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numbers",
                    Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText(this, "Could not save this food entry",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }

    private void refreshDisplays() {
        calorieTotal.setText(String.format(
                Locale.getDefault(), "🔥 Calories: %.0f kcal", totalCalories
        ));

        proteinTotal.setText(String.format(
                Locale.getDefault(), "💪 Protein: %.1f g", totalProtein
        ));

        xpTotal.setText("⭐ XP: " + totalXP);
        mealHistory.setText(getTodaysFood());
        streakText.setText("🔥 Daily Streak: " + streakCount + " days");

        updateGoals();
        updatePersonalRecords();
    }

    private void updateGoals() {
        double todayCalories = 0;
        double todayProtein = 0;

        String today = new java.text.SimpleDateFormat(
                "yyyy-MM-dd",
                java.util.Locale.getDefault()
        ).format(new java.util.Date());

        for (int i = 0; i < foodEntries.length(); i++) {
            org.json.JSONObject entry =
                    foodEntries.optJSONObject(i);

            if (entry == null) {
                continue;
            }

            if (today.equals(entry.optString("date", ""))) {
                todayCalories += entry.optDouble("calories", 0);
                todayProtein += entry.optDouble("protein", 0);
            }
        }

        calorieGoalText.setText(
                "Calories: "
                        + String.format(
                        java.util.Locale.getDefault(),
                        "%.0f", todayCalories)
                        + " / 2000 kcal"
        );

        proteinGoalText.setText(
                "Protein: "
                        + String.format(
                        java.util.Locale.getDefault(),
                        "%.1f", todayProtein)
                        + " / 60 g"
        );

        calorieProgress.setProgress(
                (int) Math.min(100,
                        todayCalories / 2000.0 * 100)
        );

        proteinProgress.setProgress(
                (int) Math.min(100,
                        todayProtein / 60.0 * 100)
        );

        int level = (totalXP / 100) + 1;
        int xpInLevel = totalXP % 100;

        String levelName;

        if (level == 1) {
            levelName = "Beginner";
        } else if (level == 2) {
            levelName = "Explorer";
        } else if (level == 3) {
            levelName = "Nutrition Hero";
        } else {
            levelName = "Master";
        }

        levelText.setText(
                "🌱 Level " + level + " - " + levelName
        );

        levelProgress.setProgress(xpInLevel);
    }

    private void updatePersonalRecords() {
        HashMap<String, Double> dailyCalories = new HashMap<>();
        HashMap<String, Double> dailyProtein = new HashMap<>();

        for (int i = 0; i < foodEntries.length(); i++) {
            JSONObject entry = foodEntries.optJSONObject(i);
            if (entry == null) {
                continue;
            }

            String date = entry.optString("date", "");
            if (date.isEmpty()) {
                continue;
            }

            dailyCalories.put(date,
                    dailyCalories.getOrDefault(date, 0.0)
                            + entry.optDouble("calories", 0));

            dailyProtein.put(date,
                    dailyProtein.getOrDefault(date, 0.0)
                            + entry.optDouble("protein", 0));
        }

        double highestCalories = 0;
        double highestProtein = 0;

        for (double value : dailyCalories.values()) {
            highestCalories = Math.max(highestCalories, value);
        }
        for (double value : dailyProtein.values()) {
            highestProtein = Math.max(highestProtein, value);
        }



        highestCaloriesText.setText(String.format(
                Locale.getDefault(),
                "🏆 Highest Daily Calories: %.0f kcal",
                highestCalories
        ));

        highestProteinText.setText(String.format(
                Locale.getDefault(),
                "💪 Highest Daily Protein: %.1f g",
                highestProtein
        ));

    }

    private void saveData() {
        prefs.edit()
                .putFloat("totalCalories", (float) totalCalories)
                .putFloat("totalProtein", (float) totalProtein)
                .putInt("totalXP", totalXP)
                .putString("foodEntries", foodEntries.toString())
                .putInt("streakCount", streakCount)
                .putString("lastLoggedDate", lastLoggedDate)
                .apply();
    }

    private void loadData() {
        // Read the calorie total as a float. The catch supports older
        // app data where this value may have been stored as an integer.
        try {
            totalCalories = prefs.getFloat("totalCalories", 0f);
        } catch (ClassCastException e) {
            totalCalories = prefs.getInt("totalCalories", 0);
        }

        totalProtein = prefs.getFloat("totalProtein", 0f);
        totalXP = prefs.getInt("totalXP", 0);
        streakCount = prefs.getInt("streakCount", 0);
        lastLoggedDate = prefs.getString("lastLoggedDate", "");

        try {
            foodEntries = new JSONArray(
                    prefs.getString("foodEntries", "[]")
            );
        } catch (JSONException e) {
            foodEntries = new JSONArray();
        }

        refreshDisplays();
        updatePersonalRecords();
    }

    private String getTodaysFood() {
        StringBuilder todayFood = new StringBuilder();
        String today = getTodayDate();

        for (int i = 0; i < foodEntries.length(); i++) {
            JSONObject entry = foodEntries.optJSONObject(i);

            if (entry != null && today.equals(entry.optString("date"))) {
                todayFood.append(entry.optString("name"))
                        .append(" (")
                        .append(entry.optString("quantity"))
                        .append(" g) - ")
                        .append(String.format(
                                Locale.getDefault(),
                                "%.1f kcal, %.1f g protein",
                                entry.optDouble("calories", 0),
                                entry.optDouble("protein", 0)
                        ))
                        .append("\n");
            }
        }

        if (todayFood.length() == 0) {
            return "No food logged today yet 🍓";
        }
        return todayFood.toString();
    }

    private void updateStreak() {
        String today = getTodayDate();

        if (today.equals(lastLoggedDate)) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(calendar.getTime());

        if (lastLoggedDate.isEmpty()) {
            streakCount = 1;
        } else if (lastLoggedDate.equals(yesterday)) {
            streakCount++;
        } else {
            streakCount = 1;
        }

        lastLoggedDate = today;
    }
}
