package uni.fmi.miroslav.carcompanion.customelements;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import java.util.Calendar;
import java.util.TimeZone;

public class CustomDatePicker implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    EditText _editText;
    private int _day;
    private int _month;
    private int _year;
    private final Context _context;

    public CustomDatePicker(Context context, EditText editText)
    {
        Activity act = (Activity)context;
        this._editText = editText;
        this._editText.setOnClickListener(this);
        this._context = context;
    }

    public CustomDatePicker(Context context, EditText editText, Button btn){
        this(context, editText);
        btn.setOnClickListener(this);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        _year = year;
        _month = monthOfYear;
        _day = dayOfMonth;
        updateDisplay();
    }
    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        DatePickerDialog dialog = new DatePickerDialog(_context, this,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();

    }

    // updates the date in the birth date EditText
    private void updateDisplay() {

        _editText.setText(new StringBuilder()
                // Month is 0 based so add 1
                .append(_year).append("/").append(_month + 1).append("/").append(_day).append(" "));
    }
}