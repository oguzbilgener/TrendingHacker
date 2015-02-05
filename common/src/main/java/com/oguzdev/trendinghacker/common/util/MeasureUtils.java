package com.oguzdev.trendinghacker.common.util;

import android.content.res.Resources;

/**
 * Copyright 2015 Oğuz Bilgener
 * TrendingHacker
 */
public class MeasureUtils {

    public static float dpFromPx(float px)
    {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }

    public static float pxFromDp(float dp)
    {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }
}
