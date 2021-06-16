package com.pratham.assessment.ui.bottom_fragment.add_student.create_profile;

import com.pratham.assessment.domain.ModalProgram;
import com.pratham.assessment.domain.Village;

import java.util.ArrayList;
import java.util.List;

public interface AddStudentContract {
    interface AddStudentView {
        void showProgram(List<ModalProgram> prgrmList);

        void showStatesSpinner(ArrayList states);

        void showProgressDialog(String msg);

        void showDistrictSpinner(List<String> blockList);

        void closeProgressDialog();

        void showBlockSpinner(List<String> blockList);

        void showVillageSpinner(ArrayList<Village> villageName);
    }

    interface AddStudentPresenter {
        void setView(AddStudentView addStudentFragment);

        void loadPrograms();

        void pullStates(String selectedProgram);

        void loadDistrictSpinner(int pos, String selectedProgram);

        void loadBlocks(String district);

        void loadVillages(String block);
    }
}
