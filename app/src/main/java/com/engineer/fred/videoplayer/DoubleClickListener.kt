package com.engineer.fred.videoplayer

import android.view.View

class DoubleClickListener( private val doubleClickTimeLimitMills: Long = 500, private val callBack: CallBack ) :  View.OnClickListener {
    private var lastClick: Long = -1L

    override fun onClick(p0: View?) {
        lastClick = when {
            lastClick == -1L -> System.currentTimeMillis()
            isDoubleClicked() -> {
                callBack.doubleClicked()
                -1L
            }
            else -> System.currentTimeMillis()
        }
    }

    private fun getTimeDiff( from: Long, to: Long ): Long {
        return to - from
    }

    private fun isDoubleClicked(): Boolean {
        return getTimeDiff(  lastClick, System.currentTimeMillis() ) <= doubleClickTimeLimitMills
    }

    interface CallBack {
        fun doubleClicked()
    }

}