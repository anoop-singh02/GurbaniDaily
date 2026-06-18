package com.anoop.gurbanidaily

import java.util.Calendar

object ShabadPicker {

    /** Day-of-year index → same shabad for the whole calendar day. */
    fun shabadForToday(): Shabad {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = dayOfYear % GurbaniData.shabads.size
        return GurbaniData.shabads[index]
    }

    /** A random shabad — used by the shuffle button. */
    fun randomShabad(): Shabad {
        return GurbaniData.shabads.random()
    }

    /** Stable index for today, used by the widget so app + widget agree. */
    fun todayIndex(): Int {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return dayOfYear % GurbaniData.shabads.size
    }
}
