package com.VeizagaTorrico.proyectotorneos.services;

import com.VeizagaTorrico.proyectotorneos.ConstantURL;
import com.VeizagaTorrico.proyectotorneos.models.Competition;
import com.VeizagaTorrico.proyectotorneos.models.CompetitionMin;
import com.VeizagaTorrico.proyectotorneos.models.Success;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface CompetitionSrv {

    @FormUrlEncoded
    @POST( ConstantURL.BASE_URL + "existcompetition" )
    Call<Success>comprobar(@Field("competencia") String competencia);

    @GET(ConstantURL.BASE_URL + "competitions")
    Call<List<Competition>> getCompetitions();

    @GET(ConstantURL.BASE_URL + "competition-participates")
    Call<List<CompetitionMin>> getCompetitionsParticipates(@Query("idUsuario") int idUsuario);

    @GET(ConstantURL.BASE_URL + "competition-follow")
    Call<List<CompetitionMin>> getCompetitionsFollow(@Query("idUsuario") int idUsuario);

    @GET(ConstantURL.BASE_URL + "findCompetitionsByName/{nameCompetition}")
    Call<List<Competition>> findCompetitionsByName(@Path("nameCompetition") String nameCompetition);

    @GET(ConstantURL.BASE_URL + "findCompetitionsByFilters")
    Call<List<CompetitionMin>> findCompetitionsByFilters(@QueryMap Map<String,String> filters);
}
