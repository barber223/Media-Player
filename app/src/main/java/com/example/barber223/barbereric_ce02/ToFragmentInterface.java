//Eric Barber
//MDF3-1804
//ToFragmentInterface.java

package com.example.barber223.barbereric_ce02;

public interface ToFragmentInterface {
    void passingSeekInformation(int maxTime, int _currentPosition, boolean _running);
    void resetProgressBar();
    void setUIForNewImage(String _title, String _author, int _photoLocation);
    void stopAllUI();
    void setUpCheckBoxes(Boolean _shuffling, Boolean _looping);
}
