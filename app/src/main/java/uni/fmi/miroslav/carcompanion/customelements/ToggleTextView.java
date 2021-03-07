package uni.fmi.miroslav.carcompanion.customelements;

import android.widget.TextView;


public class ToggleTextView {

    private final String modeOn;
    private final String modeOff;
    private boolean mode;

    public TextView tv;

    public ToggleTextView(TextView tv, String on, String off){
        mode = true;
        modeOn = on;
        modeOff = off;
        tv.setText(modeOn);
        this.tv = tv;
    }
    public ToggleTextView(TextView tv){
        this.tv = tv;
        modeOn = null;
        modeOff = null;
        mode = true;
    }

    public void toggle(){
        mode = !mode;
        if (modeOn == null | modeOff == null) return;
        if (mode){
            tv.setText(modeOn);
        } else {
            tv.setText(modeOff);
        }
    }

    public boolean isOn(){
        return mode;
    }

    public void switchTo(boolean bool){
        if (mode != bool){
            toggle();
        }
    }
    



}
