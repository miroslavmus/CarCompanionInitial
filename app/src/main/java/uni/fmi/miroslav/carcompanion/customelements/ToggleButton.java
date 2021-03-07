package uni.fmi.miroslav.carcompanion.customelements;

import android.widget.Button;

public class ToggleButton {

    private final String modeOn;
    private final String modeOff;
    private boolean mode;

    public Button btn;

    public ToggleButton(Button btn, String on, String off){
        mode = true;
        modeOn = on;
        modeOff = off;
        btn.setText(modeOn);
        this.btn = btn;
    }
    public ToggleButton(Button btn){
        this.btn = btn;
        modeOn = null;
        modeOff = null;
        mode = true;
    }

    public void toggle(){
        mode = !mode;
        if (modeOn == null | modeOff == null) return;
        if (mode){
            btn.setText(modeOn);
        } else {
            btn.setText(modeOff);
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
    public String text(){
        return btn.getText().toString();
    }


}
