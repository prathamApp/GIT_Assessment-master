package com.pratham.assessment.ui.choose_assessment.data_push_status;

import com.pratham.assessment.domain.Modal_Log;

import java.util.List;

public interface PushStatusContract {
    interface PushStatusView {
        void setPushStatusToRecycler(List<Modal_Log> log);

        void setRecentPushed(Modal_Log log, Modal_Log recentDataPushed);
    }

    interface PushStatusPresenter {
        void setView(PushStatusView pushStatusView);

        void getAllPushLog(String date);

        void getRecentPushed();
    }
}
