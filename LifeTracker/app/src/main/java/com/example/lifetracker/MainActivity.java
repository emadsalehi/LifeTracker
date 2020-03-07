package com.example.lifetracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.lifetracker.fragments.AddTimeFragment;
import com.example.lifetracker.fragments.SatisfactionFragment;
import com.example.lifetracker.fragments.TimeCategoryFragment;
import com.example.lifetracker.notification.NotificationReceiver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity {

    Fragment timeCategoryFragment, addTimeFragment, satisfictionFragment;
    EditText timeValue, timeType;

    private static final Logger LOGGER = Logger.getLogger(MainActivity.class.toString());
    private static final String CATEGORY_FILE_PATH = "categories.csv";
    private static final String SATISFACTION_FILE_PATH = "satisfaction.csv";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private List<String> categories = new ArrayList<>();
    private Map<String, List<String>> categoryTable = new TreeMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startNotificationService();
        // Set timezone
        dateFormat.setTimeZone(TimeZone.getDefault());
        timeValue = findViewById(R.id.value_number);
        timeType = findViewById(R.id.time_type);
        timeCategoryFragment = new TimeCategoryFragment();
        addTimeFragment = new AddTimeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.view_fragment, timeCategoryFragment);
        transaction.commit();
    }

    public void onCategoryClicked(String category) {
        addTimeFragment = new AddTimeFragment();
        Bundle arguments = new Bundle();
        arguments.putString("category", category);
        String endSplitTime = getEndSplitTime();
        String startSplitTime = getStartSplitTime();
        arguments.putString("startSplit", startSplitTime);
        arguments.putString("endSplit", endSplitTime);
        addTimeFragment.setArguments(arguments);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.view_fragment, addTimeFragment);
        ft.commit();
    }

    public void onAddTimePressed(View v) {
        EditText timeType = ((AddTimeFragment) addTimeFragment).getTimeType();
        EditText timeValue = ((AddTimeFragment) addTimeFragment).getTimeValue();
        String category = ((AddTimeFragment) addTimeFragment).getCategory();
        if (timeType.getText().toString().isEmpty() || timeValue.getText().toString().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Fields must be filled");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
            return;
        }

        String endSplitTime = getEndSplitTimeWithDate();
        int index = categories.indexOf(category);
        if (!categoryTable.containsKey(endSplitTime)) {
            List<String> newTimeList = new ArrayList<>();
            for (int i = 0; i < categories.size(); i++)
                newTimeList.add("None|0");
            categoryTable.put(endSplitTime, newTimeList);
        }
        String stringValue = Objects.requireNonNull(categoryTable.get(endSplitTime)).get(index);
        if (stringValue.equals("None|0"))
            stringValue = timeType.getText().toString() + "|" + timeValue.getText().toString();
        else
            stringValue =
                    stringValue.concat("&" + timeType.getText().toString() + "|" + timeValue.getText().toString());
        Objects.requireNonNull(categoryTable.get(endSplitTime)).set(index, stringValue);
        updateCsvFile();
        // show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Time Added");
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
        timeType.getText().clear();
        timeValue.getText().clear();
    }

    public void onSatisfactionPressed(View v) {
        satisfictionFragment = new SatisfactionFragment();
        Bundle arguments = new Bundle();

        String endSplitTime = getEndSplitTime();
        String startSplitTime = getStartSplitTime();
        arguments.putString("startSplit", startSplitTime);
        arguments.putString("endSplit", endSplitTime);
        satisfictionFragment.setArguments(arguments);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.view_fragment, satisfictionFragment);
        ft.commit();
    }

    public void onAddSatisfactionPressed(View v) {
        String endSplitTime = getEndSplitTimeWithDate();
        RatingBar ratingBar = ((SatisfactionFragment) satisfictionFragment).getRatingBar();
        float rate = ratingBar.getRating();
        // Open csv file
        File path = getFilesDir();
        File satisfactionFile = new File(path, SATISFACTION_FILE_PATH);
        try {
            if (!satisfactionFile.exists()) {
                boolean result = satisfactionFile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(satisfactionFile, false));
                bw.write("times,satisfaction\n" + endSplitTime + "," + rate);
                bw.flush();
                bw.close();
            } else {
                BufferedReader br = new BufferedReader(new FileReader(satisfactionFile));
                List<String> lines = new ArrayList<>();
                String currentLine;
                while ((currentLine = br.readLine()) != null) {
                    lines.add(currentLine);
                }
                br.close();
                String[] satisfactionParams = lines.get(lines.size() - 1).split(",");
                if (satisfactionParams[0].equals(endSplitTime)) {
                    lines.set(lines.size() - 1, endSplitTime + "," + rate);
                } else {
                    lines.add(lines.size() - 1, endSplitTime + "," + rate);
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(satisfactionFile, false));
                bw.write(lines.get(0));
                for (String line : lines.subList(1, lines.size())) {
                    bw.write("\n" + line);
                }
                bw.flush();
                bw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Show a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Satisfaction Added");
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
        ratingBar.setRating(0);
    }

    public void onLoadFilesPressed(View v) {
        File downloads = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File path = getFilesDir();
        File satisfactionFile = new File(path, SATISFACTION_FILE_PATH);
        File satisfactionFileDst = new File(downloads, SATISFACTION_FILE_PATH);
        File categoryFile = new File(path, CATEGORY_FILE_PATH);
        File categoryFileDst = new File(downloads, CATEGORY_FILE_PATH);
        if (downloads == null || !downloads.exists()) {
            // Show a dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Downloads directory is not exists");
            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        FileChannel source = null;
        FileChannel destination = null;
        if (satisfactionFile.exists()) {
            try {
                source = new FileInputStream(satisfactionFile).getChannel();
                destination = new FileOutputStream(satisfactionFileDst).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(getApplicationContext(),"Files Saved on Downloads", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (categoryFile.exists()) {
            try {
                source = new FileInputStream(categoryFile).getChannel();
                destination = new FileOutputStream(categoryFileDst).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(getApplicationContext(),"Files Saved on Downloads", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onFragmentBackPressed(View v) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.view_fragment, timeCategoryFragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onAddCategoryClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Category");
        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setPadding(15, 15, 15, 15);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((TimeCategoryFragment) timeCategoryFragment).addCategory(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void readCsvFile() {
        // Open csv file
        File path = getFilesDir();
        File categoryFile = new File(path, CATEGORY_FILE_PATH);
        List<String[]> rows = new ArrayList<>();
        try {
            if (!categoryFile.exists()) {
                boolean result = categoryFile.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(categoryFile));
                bw.write("times,");
                bw.flush();
                bw.close();
            }
            BufferedReader br = new BufferedReader(new FileReader(categoryFile));
            String line;
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] row = line.split(",");
                rows.add(row);
            }
            categories = new ArrayList<>();
            categories.addAll(Arrays.asList(rows.get(0)).subList(1, rows.get(0).length));
            categoryTable = new TreeMap<>();
            for (String[] row : rows.subList(1, rows.size())) {
                List<String> categoryTime = new ArrayList<>(Arrays.asList(row).subList(1, row.length));
                categoryTable.put(row[0], categoryTime);
            }
            br.close();
        } catch (FileNotFoundException e) {
            LOGGER.info("File isn't exist");
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.info("File can not be create");
            e.printStackTrace();
        }
    }

    public void updateCsvFile() {
        // Open csv file
        File path = getFilesDir();
        File categoryFile = new File(path, CATEGORY_FILE_PATH);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(categoryFile, false));
            bw.write("times");
            for(String category : categories) {
                bw.write("," + category);
            }
            bw.write("\n");
            for (Map.Entry mapElement : categoryTable.entrySet()) {
                String key = (String) mapElement.getKey();
                bw.write(key);
                for (String time : Objects.requireNonNull(categoryTable.get(key))) {
                    bw.write("," + time);
                }
                bw.write("\n");
                bw.flush();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getCategories() {
        return categories;
    }

    public Map<String, List<String>> getCategoryTable() {
        return categoryTable;
    }

    private String getEndSplitTime() {
        // get time chunk
        Date currentDate = new Date();
        String[] dateParams = dateFormat.format(currentDate).split(" ");
        int convertedTime = (Integer.parseInt(dateParams[1].split(":")[0]) / 2) * 2;
        return convertedTime + ":00";
    }

    private String getEndSplitTimeWithDate() {
        // get time chunk
        Date currentDate = new Date();
        String[] dateParams = dateFormat.format(currentDate).split(" ");
        int convertedTime = (Integer.parseInt(dateParams[1].split(":")[0]) / 2) * 2;
        return dateParams[0] + " " + convertedTime + ":00";
    }

    private String getStartSplitTime() {
        // get time chunk
        Date currentDate = new Date();
        String[] dateParams = dateFormat.format(currentDate).split(" ");
        int convertedTime = (Integer.parseInt(dateParams[1].split(":")[0]) / 2) * 2;
        if (convertedTime == 0)
            convertedTime = 24;
        return (convertedTime - 2) + ":00";
    }

    private void startNotificationService() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100
                , intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()
                , AlarmManager.INTERVAL_HOUR * 2, pendingIntent);

    }
}
