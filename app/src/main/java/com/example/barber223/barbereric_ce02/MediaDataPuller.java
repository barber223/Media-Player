//Eric Barber
//MDF3-1804
//MediaDataPuller.java

package com.example.barber223.barbereric_ce02;

import android.net.Uri;

import java.util.ArrayList;


public class MediaDataPuller {

    private ArrayList<MediaData> mediafiles = new ArrayList<>();

    public MediaDataPuller(){
        getList();
    }
    //set the list of media files
    private void getList(){
        //Yummmy screw pulling from bytes :)
        //All of the raw files have the same album art so no need to get to fanscy with it
        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.away_in_a_manger_full_wsr41817), "Away in a Manger", "Bobby Cloe", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
        R.raw.belle_and_bean_full_wsr1550501), "Belle and Bean", "Terry Gadsden", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.bill_bailey_wont_you_please_come_home_full_wsr1802001), "Bill Baily Won't You Please Come Home",
                "Rhodes & Pelfrey", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.bitter_fruit_alt_wsr4321701), "Bitter Fruit", "Kelly Richmond", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.getting_up_to_speed_full_wsr3341101), "Getting Up to Speed", "Haene & Michel", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.lullaby_full_wsr3220501), "Lullaby", "Xiao Li Wang", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.lying_in_wait_full_wsr2832901), "Lying in Wait", "John Massari", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.prelude_amaj_chopin_full_wcm060201), "Prelude in A Major Op 28 No. 7", "Albert Marlowe", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.string_sonata_2_rossini_full_wcm020701), "String Sonata No 2 in A Major 1", "Albert Marlow", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.to_be_alive_alt_wsr2930801), "To Be Alive", "Franz Kramer", R.drawable.albumart));

        mediafiles.add(new MediaData(Uri.parse("android.resource://" + "com.example.barber223.barbereric_ce02" + "/" +
                R.raw.valse_opus_64_no_1_in_d_flat_major_minute_waltz_full_wsr3271101), "Valse Opus 64 No 1 in D Flat Major: Minute Waltz",
                "Tzvi Erez", R.drawable.albumart));
    }

    public ArrayList<MediaData> getMediafiles(){
        return mediafiles;
    }

}
