package com.edwinfinch.lignite;

import android.content.res.Resources;

/**
 * Created by edwinfinch on 15-04-05.
 */
public class LigniteInfo {
    public static final int AMOUNT_OF_APPS = 12;
    public enum App {
        SPEEDOMETER, KNIGHTRIDER, CHUNKY, LINES, COLOURS, TIMEDOCK,
        TREE_OF_COLOURS, TIMEZONES, SLOT_MACHINE, PULSE, SIMPLIFIED_ANALOGUE,
        PERSONAL, BEAT, NOT_FOUND;

        public int toInt(){
            switch(this){
                case SPEEDOMETER:
                    return 0;
                case KNIGHTRIDER:
                    return 1;
                case CHUNKY:
                    return 2;
                case LINES:
                    return 3;
                case COLOURS:
                    return 4;
                case TIMEDOCK:
                    return 5;
                case TREE_OF_COLOURS:
                    return 6;
                case TIMEZONES:
                    return 7;
                case SLOT_MACHINE:
                    return 8;
                case PULSE:
                    return 9;
                case SIMPLIFIED_ANALOGUE:
                    return 10;
                case PERSONAL:
                    return 11;
                case BEAT:
                    return 12;
            }
            return -1;
        }
        public static App fromInt(int number){
            switch(number){
                case 0:
                    return SPEEDOMETER;
                case 1:
                    return KNIGHTRIDER;
                case 2:
                    return CHUNKY;
                case 3:
                    return LINES;
                case 4:
                    return COLOURS;
                case 5:
                    return TIMEDOCK;
                case 6:
                    return TREE_OF_COLOURS;
                case 7:
                    return TIMEZONES;
                case 8:
                    return SLOT_MACHINE;
                case 9:
                    return PULSE;
                case 10:
                    return SIMPLIFIED_ANALOGUE;
                case 11:
                    return PERSONAL;
                case 12:
                    return BEAT;
            }
            return NOT_FOUND;
        }
    };
    public static final String UUID[] = {
            "e1c75a76-27fc-4f9c-85b3-73920ffdb3b7", "03a9a405-ba98-44ad-bca3-bab151d81975", "1f626d76-38d8-4353-b930-c65a109260c7",
            "e1fe3595-cb81-45c3-9720-302fdd6316da", "3fabfdff-5f74-4bfb-8cfd-ac7a28df9aca", "7f1c3fc2-bddd-4845-8737-b167454d276b",
            "234e1842-d715-481c-9e04-7846d2c5b20c", "bf69875c-bdc7-4110-b21c-f5cd1c761c0c", "1b24ca10-591c-4d20-8e12-1e526f6e3634",
            "de92cc22-eb6b-4229-9665-1d6b111b1e26", "b32c5bbc-57d1-4f6c-91f1-13777fac283a", "0bf39622-a77b-42b8-846a-2a21ac9d2bec",
            "44067cdd-a10d-4e83-ba81-b886ceedb2b7"
    };
    public static final String NAME[] = {
            "speedometer", "rightnighter", "chunky", "lines", "colours",
            "timedock", "tree of colours", "timezones", "slot machine", "pulse",
            "simplified analogue", "personal", "beat"
    };
    public static final String unlock_tokens[] = {
            "fc020ef69262", "44e14e616af6", "79fc956bd024", "1697f925ec7b", "580df2b6b349", //Colours
            "000000000000", "161f9ef90958", "033da1cd67dd", "3a0fd3ea89c5", "f72e89335a89", //Pulse
            "cd57aaadcbea", "e72e89855f66"
    };
    public static final String UUID_endings[] = {
            "73920ffdb3b7", "bab151d81975", "c65a109260c7", "302fdd6316da", "ac7a28df9aca",
            "000000000000", "7846d2c5b20c", "f5cd1c761c0c", "1e526f6e3634", "1d6b111b1e26",
            "13777fac283a", "2a21ac9d2bec"
    };
    public static final int unlock_keys[][] = {
            { 1098, 546 },  { 1035, 535 },  { 3012, 392 },  { 7856, 545 },  { 3242, 2334 },
            { 6666, 6666 }, { 7865, 6345 }, { 3049, 9569 }, { 5672, 1932 }, { 3498, 1030 },
            { 8799, 3402 }, { 9831, 8393 }
    };
    public static final String APPLICATION_LOCATION[] = {
            "556dcfbc354a41c220000011", "55883dc841241a5db40000f0", "55883f2f41241abe5c0000e1",
            "5588410449c1d102cd000114", "55882b0c0021370a4b0000dc", "552d9637ceb7830ea000007b",
            "5595d996308fd768f30000b7", "55992f97bec20eb7b5000038", "559c557e12dc3229e5000084",
            "559c5602b703a68da9000089", "55b517ee46a407265f00007b", "55c05b70718511a83a00000b",
            "55c8cf1c7fde01b16d000003"
    };
    public static final String APP_SKUS[] = {
            "ind_face_speedometer", "ind_face_knightrider", "ind_face_chunky", "ind_face_lines",
            "ind_face_colours", "ind_face_donate", "ind_face_treeofcolours", "ind_face_timezones",
            "ind_face_slotmachine", "ind_face_pulse", "ind_face_simplified", "ind_face_personal",
            "ind_face_beat"
    };

    public static App getSectionFromAppName(String name){
        for(int i = 0; i < NAME.length; i++){
            if(name.equals(NAME[i])){
                return App.fromInt(i);
            }
        }
        return App.NOT_FOUND;
    }

    public static String getAbootText(App type, Resources resources, boolean advanced) {
        String[] app_descriptions;
        if(advanced) {
            app_descriptions = resources.getStringArray(R.array.app_descriptions_advanced);
        }
        else{
            app_descriptions = resources.getStringArray(R.array.app_descriptions);
        }
        return app_descriptions[type.toInt()];
    }

    public static int getDrawable(App type, boolean time){
        switch(type){
            case SPEEDOMETER:
                return time ? R.drawable.speedometer_time : R.drawable.speedometer_tintin;
            case KNIGHTRIDER:
                return time ? R.drawable.knightrider_time : R.drawable.knightrider_tintin;
            case CHUNKY:
                return time ? R.drawable.chunky_time : R.drawable.chunky_tintin;
            case LINES:
                return time ? R.drawable.lines_time : R.drawable.lines_tintin;
            case COLOURS:
                return time ? R.drawable.colours_time : R.drawable.colours_time_alt;
            case TIMEDOCK:
                return time ? R.drawable.timedock_time : R.drawable.timedock_tintin;
            case TREE_OF_COLOURS:
                return time ? R.drawable.tree_of_colours_time : R.drawable.tree_of_colours_tintin;
            case TIMEZONES:
                return time ? R.drawable.timezones_time : R.drawable.timezones_tintin;
            case SLOT_MACHINE:
                return R.drawable.slot_machine_time;
            case PULSE:
                return R.drawable.pulse_time;
            case SIMPLIFIED_ANALOGUE:
                return time ? R.drawable.simplified_analogue_time : R.drawable.simplified_analogue_tintin;
            case PERSONAL:
                return R.drawable.personal_time;
            case BEAT:
                return time ? R.drawable.beat_time : R.drawable.beat_tintin;

        }
        return R.drawable.pulse_time;
    }
}
