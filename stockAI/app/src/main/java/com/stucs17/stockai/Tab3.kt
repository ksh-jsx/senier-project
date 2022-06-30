package com.stucs17.stockai

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import com.commexpert.CommExpertMng
import com.commexpert.ExpertTranProc
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import com.truefriend.corelib.shared.ItemMaster.ItemCode
import kotlin.math.abs


class Tab3 : Fragment() {

    private lateinit var seekBar_for_risk : SeekBar
    private lateinit var textView_for_risk : TextView
    lateinit var tabActivity: TabActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

        tabActivity = context as TabActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v: View = inflater.inflate(R.layout.fragment_tab3, container, false)

        seekBar_for_risk = v.findViewById(R.id.seekBar_for_risk)
        textView_for_risk = v.findViewById(R.id.textView_for_risk)
        seekBar_for_risk.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            val strArr = arrayOf<String>("매우 낮음","낮음","중간","높음","매우 높음")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView_for_risk.text = strArr[progress]
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                textView_for_risk.text = strArr[seekBar!!.progress]
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                textView_for_risk.text = strArr[seekBar!!.progress]
            }
        })

        return v
    }



}