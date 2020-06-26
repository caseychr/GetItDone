package com.ccasey.getitdone.viewextensions

import android.os.CountDownTimer
import android.widget.TextView
import com.ccasey.getitdone.ui.INTERVAL

class ViewExtensions {

}

fun TextView.setTextToLongTimer(): Long {
    var seconds = 0L
    var minutes = 0L
    var hours = 0L
    var temp =  text.toString()
    temp = temp.replace(".", "")
    temp = temp.replace(":", "")
    if(temp.length >= 2) {
        seconds = temp.substring(temp.length-2, temp.length).toLong() * 1000L
    }
    if(temp.length >= 4) {
        minutes = temp.substring(temp.length-4, temp.length-2).toLong() * 60000L
    }
    if(temp.length == 5) {
        hours = temp.substring(0, 1).toLong() * 360000L
    } else if(temp.length == 6) {
        hours = temp.substring(0, 2).toLong() * 360000L
    }
    return hours + minutes + seconds
}



fun TextView.setLongToTextTimer(l: Long, onlySeconds: Boolean  = false) {
    var l_seconds = (l % 60000).toString()
    var l_minutes = (l / 60000).toString()
    val l_hours = (l / 3600000).toString()
    l_seconds =
        if (l_seconds.length == 5) l_seconds.substring(
            0,
            2
        ) else if (l_seconds.length == 3) "00" else "0" + l_seconds.substring(
            0,
            1
        )
    val a = if(l_minutes.toInt() == 0) "$l_seconds" else "$l_minutes$l_seconds"
    if(l_minutes.toLong() > 59) {
        l_minutes = (l_minutes.toLong() % 60L).toString()
    }
    if (a.length == 1) {
        setText(":0${l_seconds.substring(0, 2)}")
    } else if (a.length == 2) {
        if(onlySeconds) {
            setText("${l_seconds.substring(1, 2)}")
        } else {
            setText(":${l_seconds.substring(0, 2)}")
        }
    } else if (a.length == 3) {
        setText("0$l_minutes:${l_seconds.substring(0, 2)}")
    } else if (a.length == 4) {
        setText("$l_minutes:${l_seconds.substring(0, 2)}")
    } else if (a.length >= 5) {
        if(l_minutes.length < 2) {
            setText("$l_hours:0$l_minutes:${l_seconds.substring(0, 2)}")
        } else {
            setText("$l_hours:$l_minutes:${l_seconds.substring(0, 2)}")
        }
    }
}

fun CountDownTimer.createCountDownTimer(textView: TextView, timer: Long) {
     object : CountDownTimer(timer, INTERVAL) {
        override fun onFinish() {
        }

        override fun onTick(millisUntilFinished: Long) {
            textView.setLongToTextTimer(millisUntilFinished)
            //layout_play.metricTimeNum.setLongToTextTimer((timer - millisUntilFinished)+ INTERVAL)
            //layout_play.tracker.setLongToTextTimer(millisUntilFinished)
        }

    }.start()
}

fun CountDownTimer.endTimer() {
    this.cancel()
}

