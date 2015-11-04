package test.freelancer.com.fltest.rest.model;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

/**
 * Created by Android 18 on 11/4/2015.
 */
public class Program extends SugarRecord<Program> {

    public String name;
    @SerializedName("start_time")
    public String startTime;
    @SerializedName("end_time")
    public String endTime;
    public String channel;
    public String rating;

}
