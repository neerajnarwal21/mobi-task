package com.task.mobiadmin.fragment.home

/**
 * Created by neeraj on 12/4/18.
 */
interface TabCountListener {
    interface NewTabCount {
        fun onCountChangeInNew()
    }

    interface OngoingTabCount {
        fun onCountChangeInOngoing()
    }

    interface CompleteTabCount {
        fun onCountChangeInComplete()
    }
}