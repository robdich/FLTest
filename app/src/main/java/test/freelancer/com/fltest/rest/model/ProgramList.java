package test.freelancer.com.fltest.rest.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ProgramList {

    @SerializedName("results")
    @Expose
    public List<Program> programList = new ArrayList<Program>();
    @SerializedName("count")
    @Expose
    public Integer count;

}