package test.freelancer.com.fltest.rest;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import test.freelancer.com.fltest.rest.model.Program;
import test.freelancer.com.fltest.rest.model.ProgramList;

/**
 * Created by Android 18 on 11/4/2015.
 */
public interface TvGuideInterface {

//    @GET("/users?filters[0][operator]=equals")
//    UserDto retrieveUsersByFilters(
//            @Query("filters[0][field]") String nameFilter,
//            @Query("filters[0][value]") String value);

    @GET("/wabz/guide.php")
    Call<ProgramList> getPrograms(
            @Query("start") String start);

//    http://whatsbeef.net/wabz/guide.php?start=0
//    @GET("/wabz/guide.php?start={start}")
//    Call<List<Program>> getPrograms(
//            @Path("start") String start);


}
