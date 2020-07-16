package martinek.segasesu

import martinek.segasesu.brain.EvaluationRequest
import martinek.segasesu.brain.EvaluationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Created by Kexik on 14.04.2017.
 */

/**
 * Retrofit api for comunicating with server
 */
interface RetrofitApi
{
    @GET("gameData/changed/{userName}")
    fun getLastChangeTimeStamp(@Path("userName") userName : String) : Call<TimestampHolder>

    @GET("gameData/get/{userName}")
    fun getGameData(@Path("userName") userName : String) : Call<GameData>

    @POST("gameData")
    fun postGameData(@Body gameData : GameData) : Call<GameData>

    @GET("evaluation/{id}/{category}")
    fun getEvaluationList(@Path("id") id : Int, @Path("category") category : Int) : Call<EvaluationRequest>

    @POST("evaluation/{id}/{category}")
    fun postEvaluation(@Body response : EvaluationResponse, @Path("id") id : Int, @Path("category") category : Int) : Call<Void>


    class TimestampHolder
    {
        var changedTimestamp : Long = 0
    }
}
