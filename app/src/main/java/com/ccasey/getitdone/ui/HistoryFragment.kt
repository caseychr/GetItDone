package com.ccasey.getitdone.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.ccasey.getitdone.R
import com.ccasey.getitdone.model.Run
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment: Fragment() {

    lateinit var historyAdapter: HistoryRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.setTitle("History")
        initRecycler(mockRuns())
    }

    private fun initRecycler(runs: List<Run>) {
        historyRecyclerView.apply {
            historyAdapter = HistoryRecyclerAdapter(runs)
            layoutManager = LinearLayoutManager(this@HistoryFragment.context)
            adapter = historyAdapter
        }
    }

    private fun mockRuns(): List<Run> {
        val runs = mutableListOf<Run>()
        for(x in 0..10) {
            runs.add(Run(25f, 23L, 12f, System.currentTimeMillis(), mutableListOf(1f, 3f, 4f), mutableListOf(1f, 3f, 4f)))
        }
        return runs
    }
}