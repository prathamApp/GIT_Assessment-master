package com.pratham.assessment.ui.bottom_fragment.add_student.create_profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pratham.assessment.R;
import com.pratham.assessment.async.API_Content;
import com.pratham.assessment.constants.APIs;
import com.pratham.assessment.constants.Assessment_Constants;
import com.pratham.assessment.domain.ModalProgram;
import com.pratham.assessment.domain.Village;
import com.pratham.assessment.interfaces.API_Content_Result;

import org.androidannotations.annotations.EBean;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@EBean
public class AddStudentPresenter implements AddStudentContract.AddStudentPresenter, API_Content_Result {
    Context context;
    AddStudentContract.AddStudentView addStudentView;
    String selectedState;
    ArrayList stateCodes;
    String selectedProgram;
    API_Content api_content;
    ArrayList<Village> villageList = new ArrayList<>();

    public AddStudentPresenter(Context context) {
        this.context = context;
        api_content = new API_Content(context, this);

    }

    @Override
    public void setView(AddStudentContract.AddStudentView addStudentFragment) {
        this.addStudentView = addStudentFragment;
    }

    @Override
    public void loadPrograms() {
        AndroidNetworking.get(Assessment_Constants.URL.PULL_PROGRAMS.toString())
                .addHeaders("Content-Type", "application/json").build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        List<ModalProgram> programList = new ArrayList<>();
                        Type listType = new TypeToken<List<ModalProgram>>() {
                        }.getType();
                        Gson gson = new Gson();
                        programList = gson.fromJson(response.toString(), listType);
                        if (programList != null) {
                            ModalProgram modalProgram = new ModalProgram();
                            modalProgram.setProgramId("-1");
                            modalProgram.setProgramName("Select Program");
                            LinkedHashSet hs = new LinkedHashSet(programList);//to remove redundant values
                            programList.clear();
                            programList.addAll(hs);
                            programList.add(0, modalProgram);
                            addStudentView.showProgram(programList);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void pullStates(String selectedProgram) {
        ArrayList states = new ArrayList();
        stateCodes = new ArrayList();
        states.add("Select State");
        stateCodes.add("Select State");
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading...");
        AndroidNetworking.get(APIs.pullStateAPI + selectedProgram)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {

                                states.add(response.getJSONObject(i).getString("StateName"));
                                stateCodes.add(response.getJSONObject(i).getString("StateCode"));
                            }
                            if (states.size() > 0) {
                                addStudentView.showStatesSpinner(states);
                            } else
                                Toast.makeText(context, R.string.no_states, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(context, R.string.error_in_loading_check_internet_connection, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

    }

    @Override
    public void loadDistrictSpinner(int pos, String selectedProgram) {

        addStudentView.showProgressDialog("loading Districts");

//        String[] statesCodes = context.getResources().getStringArray(R.array.india_states_shortcode);
//        selectedBlock = statesCodes[pos];
        selectedState = (String) stateCodes.get(pos);
        this.selectedProgram = selectedProgram;
        String url;

        url = APIs.pullVillagesServerURL + selectedProgram + APIs.SERVER_STATE + selectedState;
        api_content.pullFromInternet(Assessment_Constants.SERVER_BLOCK, url);

    }

    @Override
    public void loadBlocks(String district) {
        List<String> blockList = new ArrayList<>();

        if (villageList != null) {
            if (villageList.isEmpty()) {
                blockList.add("NO BLOCKS");
            } else {
                blockList.add("Select Block");
                for (Village vill : villageList) {
                    if (vill.getDistrict().trim().equalsIgnoreCase(district.trim()) /*&& !blockList.contains(district)*/)
                        blockList.add(vill.getBlock());
                }
            }
            LinkedHashSet hs = new LinkedHashSet(blockList);
            blockList.clear();
            blockList.addAll(hs);
            addStudentView.showBlockSpinner(blockList);
        }
    }

    @Override
    public void loadVillages(String block) {
        ArrayList<Village> villageName = new ArrayList();
/*
        if (isConnectedToRasp) {
            for (RaspVillage raspVillage : raspVillageList) {
                Village village = raspVillage.getData();
                if (block.equalsIgnoreCase(village.getBlock().trim()))
                    villageName.add(new Village(village.getVillageId(), village.getVillageName()));
            }
        } else {
*/
        Village vil = new Village(-1, "Select Village");
        villageName.add(vil);
        for (Village village : villageList) {
            if (block.equalsIgnoreCase(village.getBlock().trim()))
                villageName.add(new Village(village.getVillageId(), village.getVillageName()));
        }
//        }
        if (!villageName.isEmpty()) {
            addStudentView.showVillageSpinner(villageName);
        }
    }


    @Override
    public void receivedContent(String header, String response) {
        List<String> districtList = new ArrayList<>();
        Type listType = new TypeToken<List<Village>>() {
        }.getType();
        Gson gson = new Gson();

        villageList = gson.fromJson(response, listType);
        if (villageList != null) {
            if (villageList.isEmpty()) {
                districtList.add("NO DISTRICTS");
            } else {
                districtList.add("Select District");
                for (Village vill : villageList) {
                    districtList.add(vill.getDistrict());
                }
            }
            LinkedHashSet hs = new LinkedHashSet(districtList);
            districtList.clear();
            districtList.addAll(hs);
            addStudentView.showDistrictSpinner(districtList);
        }
        addStudentView.closeProgressDialog();
    }

    @Override
    public void receivedError(String header) {

    }
}
