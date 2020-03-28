package com.VeizagaTorrico.proyectotorneos.fragments.crear_competencias;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.VeizagaTorrico.proyectotorneos.R;
import com.VeizagaTorrico.proyectotorneos.RetrofitAdapter;
import com.VeizagaTorrico.proyectotorneos.models.Competition;
import com.VeizagaTorrico.proyectotorneos.models.TypesOrganization;
import com.VeizagaTorrico.proyectotorneos.services.CompetitionSrv;
import com.VeizagaTorrico.proyectotorneos.services.TypesOrganizationSrv;
import com.VeizagaTorrico.proyectotorneos.utils.ManagerSharedPreferences;
import com.VeizagaTorrico.proyectotorneos.utils.MensajeSinInternet;
import com.VeizagaTorrico.proyectotorneos.utils.NetworkReceiver;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.VeizagaTorrico.proyectotorneos.Constants.FILE_SHARED_DATA_USER;
import static com.VeizagaTorrico.proyectotorneos.Constants.KEY_ID;

public class CrearCompetencia3Fragment extends Fragment implements MensajeSinInternet {

    private OnFragmentInteractionListener mListener;

    //Widgets
    private Button btnCrear;
    private Spinner spinner, spinnerFase;
    private TextView txtView, tvGrupo;
    private EditText etGrupo;
    private Competition competition;
    private TypesOrganizationSrv orgSrv;
    private CompetitionSrv competitionSrv;
    private View vista;
    private Map<String,String> competencia;
    private String cantGrupos;
    private int fase;
    private boolean hayGrupo ;
    private boolean hayFase ;
    private int usuario;

    private List<String> fases;

    public CrearCompetencia3Fragment() {
        // Required empty public constructor
    }
    public static CrearCompetencia3Fragment newInstance(String param1, String param2) {
        CrearCompetencia3Fragment fragment = new CrearCompetencia3Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        hayGrupo = false;
        vista = inflater.inflate(R.layout.fragment_crear_competencia3, container, false);
        initElements();
        if(NetworkReceiver.existConnection(vista.getContext())) {
            llenarSpinnerOrg();
            listeners();
        } else {
            sinInternet();
        }

        return vista;
    }

    private void listeners() {
        //LISTENER BOTONES
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    competencia.put("nombre",competition.getName());
                    competencia.put("fecha_ini",competition.getFechaInicio());
                    competencia.put("fecha_fin",competition.getFechaFin());
                    competencia.put("ciudad",competition.getCiudad());
                    competencia.put("genero",competition.getGenero());
                    competencia.put("max_comp","32");
                    competencia.put("categoria_id", Integer.toString(competition.getCategory().getId()));
                    competencia.put("tipoorg_id",Integer.toString(competition.getTypesOrganization().getId()));
                    competencia.put("user_id",Integer.toString(usuario));
                    competencia.put("frecuencia",Integer.toString(competition.getFrecuencia()));

                    Log.d("BODY" , competencia.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(hayGrupo) {
                    cantGrupos = etGrupo.getText().toString();
                    if(validarGrupo()){
                        competencia.put("cant_grupos", cantGrupos);
                    }else{
                        Toast.makeText(getContext(), "Por favor complete los campos vacios", Toast.LENGTH_SHORT);
                    }
                }
                else {
                    competencia.put("cant_grupos", null);
                }
                if(hayFase){
                    competencia.put("fase", Integer.toString(fase));
                }else {
                    competencia.put("fase", null);
                }


                Call<Competition> call = competitionSrv.createCompetition(competencia);
                Log.d("Url",call.request().url().toString());

                call.enqueue(new Callback<Competition>() {
                    @Override
                    public void onResponse(Call<Competition> call, Response<Competition> response) {
                        Log.d("Response code",Integer.toString(response.code()));

                        if(response.code() == 201){
                            Log.d("COMPETENCIA CREADA", competencia.toString());
                            // ACA ES DONDE PUEDO PASAR A OTRO FRAGMENT Y DE PASO MANDAR UN OBJETO QUE CREE CON EL BUNDLE
                            Toast toast = Toast.makeText(vista.getContext(), "Competencia Creada!", Toast.LENGTH_SHORT);
                            toast.show();
                            Navigation.findNavController(vista).navigate(R.id.misCompetencias);
                        }
                        if (response.code() == 400) {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response.errorBody().string());
                                String userMessage = jsonObject.getString("messaging");
                                Log.d("CREATE_COMP", "Msg de la repuesta: "+userMessage);
                                Toast.makeText(vista.getContext(), "Error en la peticion: "+userMessage+" >>", Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                        if (response.code() == 500) {
                            Log.d("CREATE_COMP_ERROR", "Problemas en el servidor.");
                            Toast.makeText(vista.getContext(), "Problemas en el servidor ", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Competition> call, Throwable t) {
                        Toast toast = Toast.makeText(vista.getContext(), "Problemas con el servidor", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void sinInternet() {
        Toast toast = Toast.makeText(vista.getContext(), "Sin Conexion a Internet, por el momento no se podran crear competencias.", Toast.LENGTH_LONG);
        toast.show();
        btnCrear.setVisibility(View.INVISIBLE);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void initElements(){
        competencia = new HashMap<>();
        usuario = Integer.valueOf(ManagerSharedPreferences.getInstance().getDataFromSharedPreferences(getContext(), FILE_SHARED_DATA_USER, KEY_ID));
        orgSrv = new RetrofitAdapter().connectionEnable().create(TypesOrganizationSrv.class);
        competitionSrv = new RetrofitAdapter().connectionEnable().create(CompetitionSrv.class);

        txtView = vista.findViewById(R.id.descripcionTipoOrg);
        etGrupo = vista.findViewById(R.id.etCantGrupo);
        btnCrear = vista.findViewById(R.id.btnCCSig_3);
        spinner = vista.findViewById(R.id.spinnerTipoOrg);
        spinnerFase = vista.findViewById(R.id.spinnerFase);
        tvGrupo = vista.findViewById(R.id.tv_grupo_crearComp3);
    }
     private void llenarSpinnerOrg(){
         Call<List<TypesOrganization>> call = orgSrv.getTypesOrganization();
         call.enqueue(new Callback<List<TypesOrganization>>() {
             @Override
             public void onResponse(Call<List<TypesOrganization>> call, Response<List<TypesOrganization>> response) {
                 List<TypesOrganization> tipos;

                 if(response.code() == 200){
                     tipos = response.body();
                     Log.d("RESPONSECODE OrgActvty",  Integer.toString(response.code()) );
                     ArrayAdapter<TypesOrganization> adapter = new ArrayAdapter<>(vista.getContext(),android.R.layout.simple_spinner_item,tipos);
                     adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                     spinner.setAdapter(adapter);
                     spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                         @Override
                         public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                             try {
                                 TypesOrganization org = (TypesOrganization) spinner.getSelectedItem();
                                 txtView.setText(org.getDescription());
                                 competition =(Competition) getArguments().getSerializable("competition");
                                 competition.setTypesOrganization(org);

                                 if(org.getName().contains("grupo")){
                                     Log.d("prueba" , "entra?" );
                                     tvGrupo.setVisibility(View.VISIBLE);
                                     etGrupo.setVisibility(View.VISIBLE);
                                     hayGrupo = true;
                                 } else{
                                     tvGrupo.setVisibility(View.INVISIBLE);
                                     etGrupo.setVisibility(View.INVISIBLE);
                                     hayGrupo = false;
                                 }

                                 if(org.getName().contains("Eliminatorias")){
                                     Log.d("prueba" , "entra?" );
                                     spinnerFase.setVisibility(View.VISIBLE);
                                     hayFase = true;
                                     llenarSpinnerFase();
                                 } else{
                                     spinnerFase.setVisibility(View.INVISIBLE);
                                     hayFase = false;
                                 }

                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                         }

                         @Override
                         public void onNothingSelected(AdapterView<?> adapterView) {
                         }
                     });
                 }
             }

             @Override
             public void onFailure(Call<List<TypesOrganization>> call, Throwable t) {
                 Toast toast = Toast.makeText(getContext(), "Por favor recargar pestaña", Toast.LENGTH_SHORT);
                 toast.show();
                 Log.d("onFailure", t.getMessage());
             }
         });

     }

    private void llenarSpinnerFase() {
        final List<String> fases = new ArrayList<>();
        fases.add("Final");
        fases.add("Semi Final");
        fases.add("Cuartos");
        fases.add("Octavos");
        fases.add("16 avos");
        fases.add("32 avos");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(vista.getContext(),android.R.layout.simple_spinner_item,fases);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFase.setAdapter(adapter);
        spinnerFase.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fase = i+1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private boolean validarGrupo() {
        if(cantGrupos.isEmpty())
            return false;
        return true;
    }

}
