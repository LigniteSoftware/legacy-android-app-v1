package com.edwinfinch.lignite;

import android.content.res.Resources;

/**
 * Created by edwinfinch on 15-04-05.
 */
public class PebbleInfo {
    public static final int AMOUNT_OF_APPS = 10;
    public static final int SPEEDOMETER = 0, KNIGHTRIDER = 1, CHUNKY = 2, LINES = 3, COLOURS = 4, TIMEDOCK = 5, TREE_OF_COLOURS = 6,
            TIMEZONES = 7, SLOT_MACHINE = 8, PULSE = 9;
    public static final String UUID[] = {
            "e1c75a76-27fc-4f9c-85b3-73920ffdb3b7", "03a9a405-ba98-44ad-bca3-bab151d81975", "1f626d76-38d8-4353-b930-c65a109260c7",
            "e1fe3595-cb81-45c3-9720-302fdd6316da", "3fabfdff-5f74-4bfb-8cfd-ac7a28df9aca", "7f1c3fc2-bddd-4845-8737-b167454d276b",
            "234e1842-d715-481c-9e04-7846d2c5b20c", "bf69875c-bdc7-4110-b21c-f5cd1c761c0c", "1b24ca10-591c-4d20-8e12-1e526f6e3634",
            "de92cc22-eb6b-4229-9665-1d6b111b1e26"
    };
    public static final String unlock_tokens[] = {
            "fc020ef69262", "44e14e616af6", "79fc956bd024", "1697f925ec7b", "580df2b6b349", null, "161f9ef90958", "033da1cd67dd",
            "3a0fd3ea89c5", "f72e89335a89"
    };
    public static final String UUID_endings[] = {
            "73920ffdb3b7", "bab151d81975", "c65a109260c7", "302fdd6316da", "ac7a28df9aca", null, "7846d2c5b20c", "f5cd1c761c0c",
            "1e526f6e3634", "1d6b111b1e26"
    };
    public static final int unlock_keys[][] = {
            { 1098, 546 },
            { 1035, 535 }, //RightNighter
            { 3012, 392 },
            { 7856, 545 },
            { 3242, 2334 },
            { 666, 666 }, //Timedock (no unlock)
            { 7865, 6345 },
            { 3049, 9569 },
            { 5672, 1932 },
            { 3498, 1030 }
    };
    public static final int SETTINGS_COUNT[] = {
            7, 6, 4, 4, 6, 0, 3, 10, 10, 10
    };
    public static final String SETTINGS_KEYS[][] = {
            {
                    "spe_show_borders", "spe_general_boot_animation", "spe_general_bluetooth_icon", "spe_general_antialiased", "spe_invert_checkbox", "spe_btdisalert_checkbox", "spe_btrealert_checkbox"
            },
            {
                    "knig_constant_checkbox", "knig_btdisalert_checkbox", "knig_btrealert_checkbox", "knig_bootanim_checkbox", "knig_antialiased_checkbox", "knig_inverted_checkbox"
            },
            {
                    "chu_btdisalert_checkbox", "chu_btrealert_checkbox", "chu_invert", "chu_battery_bar"
            },
            {
                    "lin_btdisalert_checkbox", "lin_btrealert_checkbox", "lin_show_date", "lin_alt_date_format"
            },
            {
                    "col_btdisalert_checkbox", "col_btrealert_checkbox", "col_outline_numbers", "col_constant_anim", "col_organize", "col_randomize_width"
            },
            {
                    "edgy topix"
            },
            {
                    "tre_btdisalert_checkbox", "tre_btrealert_checkbox", "tre_randomize"
            },
            {
                    "tim_btdisalert_checkbox", "tim_btrealert_checkbox", "tim_name_1", "tim_colour_1", "tim_analogue_1", "tim_timezone_2", "tim_subtract_hour", "tim_name_1", "tim_colour_1", "tim_analogue_1"
            },
            {
                    "slo_btdisalert_checkbox", "slo_btrealert_checkbox", "slo-invert", "slo-shake-to-animate", "slo-seconds"
            },
            {
                    "pul_btdisalert_checkbox", "pul_btrealert_checkbox", "pul-invert", "pul-constant-anim", "pul-shake", "pul-circle-colour", "pul-background_colour"
            }
    };
    public static final boolean SETTINGS_ENABLED[] = {
            true, true, true, true, true, false, true, true, true, true
    };
    public static final String APPLICATION_LOCATION[] = {
            "556dcfbc354a41c220000011", "55883dc841241a5db40000f0", "55883f2f41241abe5c0000e1",
            "5588410449c1d102cd000114", "55882b0c0021370a4b0000dc", "552d9637ceb7830ea000007b",
            "5595d996308fd768f30000b7", "55992f97bec20eb7b5000038", "559c557e12dc3229e5000084",
            "559c5602b703a68da9000089"
    };
    public static final String APP_SKUS[] = {
            "ind_face_speedometer", "ind_face_knightrider", "ind_face_chunky", "ind_face_lines",
            "ind_face_colours", "ind_face_donate", "ind_face_treeofcolours", "ind_face_timezones",
            "ind_face_slotmachine", "ind_face_pulse"
    };

    public static String getAbootText(int type, Resources resources, boolean advanced) {
        String[] app_descriptions;
        if(advanced) {
            app_descriptions = resources.getStringArray(R.array.app_descriptions_advanced);
        }
        else{
            app_descriptions = resources.getStringArray(R.array.app_descriptions);
        }
        return app_descriptions[type];
    }

    public static int getDrawable(int type, boolean time){
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

        }
        return -1;
    }
}
