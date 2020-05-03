package zin.game.card.jackace.network;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import zin.game.card.jackace.JackAceGameConnection;
import zin.game.card.jackace.JackAceGameMessage;

public interface JackAceConnectionService {

    @GET("/")
    Single<Object> checkIfSomeoneJoined(@Query("a") String a,@Query("t") String t,@Query("m") String m,@Query("col1") String col1, @Query("val1") String val1);

    @GET("/")
    Single<ResponseWrapper<List<JackAceGameConnection>>> findGamesToJoin(@Query("a") String a,@Query("t") String t,@Query("m") String m,@Query("col1") String col1);

    @POST("/")
    Single<ResponseWrapper<Object>> joinGame(@Query("a") String a,@Query("t") String t,@Query("m") String m, @Query("col1")String c1, @Query("val1") String val1, @Body JackAceGameConnection con);

    @POST("/")
    Single<ResponseWrapper<Object>> startGame(@Query("a") String a,@Query("t") String t,@Query("m") String m, @Body JackAceGameConnection con);


    @POST("/")
    Single<ResponseWrapper<Object>> writeMessage(@Query("a") String a,@Query("t") String t,@Query("m") String m, @Body JackAceGameMessage message);

    @POST("/")
    Single<ResponseWrapper<Object>> markMessageAsRead(@Query("a") String a,@Query("t") String t,@Query("m") String m, @Query("col1")String c1, @Query("val1") String val1, @Body JackAceGameMessage message);

    @GET("/")
    Single<ResponseWrapper<List<JackAceGameMessage>>> getUnreadMessages(@Query("a") String a,@Query("t") String t,@Query("m") String m, @Query("col1")String c1, @Query("val1") String val1, @Query("c2")String c2, @Query("v2") String val2);
}