package com.VeizagaTorrico.proyectotorneos.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.VeizagaTorrico.proyectotorneos.R;
import com.VeizagaTorrico.proyectotorneos.RetrofitAdapter;
import com.VeizagaTorrico.proyectotorneos.models.RespSrvUser;
import com.VeizagaTorrico.proyectotorneos.services.UserSrv;
import com.VeizagaTorrico.proyectotorneos.utils.ManagerSharedPreferences;
import com.VeizagaTorrico.proyectotorneos.utils.Validations;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.VeizagaTorrico.proyectotorneos.Constants.FILE_SHARED_DATA_USER;
import static com.VeizagaTorrico.proyectotorneos.Constants.KEY_EMAIL;
import static com.VeizagaTorrico.proyectotorneos.Constants.KEY_ID;
import static com.VeizagaTorrico.proyectotorneos.Constants.KEY_LASTNAME;
import static com.VeizagaTorrico.proyectotorneos.Constants.KEY_NAME;
import static com.VeizagaTorrico.proyectotorneos.Constants.KEY_USERNAME;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MiPerfilFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MiPerfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MiPerfilFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private View vista;

    private EditText edtNombre, edtApellido, edtCorreo, edtusuario;
    private Button btnSaveChanges;

    private String nombreNuevo, apellidoNuevo, correoNuevo, usuarioNuevo;
    private UserSrv apiUserService;
    private Map<String,String> newUserMap = new HashMap<>();
    RespSrvUser respSrvRegister;

    private OnFragmentInteractionListener mListener;

    public MiPerfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InicioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MiPerfilFragment newInstance(String param1, String param2) {
        MiPerfilFragment fragment = new MiPerfilFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista = inflater.inflate(R.layout.fragment_mi_perfil, container, false);

        updateUi();
        loadDataUser();
        listenBotonSaveChanges();

        return vista;
    }

    // realizamos los binding con los componentes de la vista
    private void updateUi(){
        edtNombre = vista.findViewById(R.id.edt_nombre_miperfil);
        edtApellido = vista.findViewById(R.id.edt_apellido_miperfil);
        edtCorreo = vista.findViewById(R.id.edt_correo_miperfil);
        edtusuario = vista.findViewById(R.id.edt_usuario_miperfil);

        btnSaveChanges = vista.findViewById(R.id.btn_update_miperfil);
    }

    // completamos los campos con los datos del usaurio guardados localmente
    private void loadDataUser(){
        edtNombre.setText(ManagerSharedPreferences.getInstance().getDataFromSharedPreferences(this.getContext(), FILE_SHARED_DATA_USER, KEY_NAME));
        edtApellido.setText(ManagerSharedPreferences.getInstance().getDataFromSharedPreferences(this.getContext(), FILE_SHARED_DATA_USER, KEY_LASTNAME));
        edtCorreo.setText(ManagerSharedPreferences.getInstance().getDataFromSharedPreferences(this.getContext(), FILE_SHARED_DATA_USER, KEY_EMAIL));
        edtusuario.setText(ManagerSharedPreferences.getInstance().getDataFromSharedPreferences(this.getContext(), FILE_SHARED_DATA_USER, KEY_USERNAME));
    }

    // ponemos a la escucha el boton de guardar los cambios
    private void listenBotonSaveChanges(){
        btnSaveChanges.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(formCorrect()){
                    getValuesFields();
                    updateDataUser();
                }
                else{
                    Toast.makeText(getContext(), "Datos INCORRECTOS!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // manda los nuevos del usaurio al server
    private void updateDataUser(){
        Log.d("RESP_UPDATE_ERROR", "entro a updateUser()");
        // hacemos la conexion con el api de rest del servidor
        apiUserService = new RetrofitAdapter().connectionEnable().create(UserSrv.class);

        newUserMap.put("id", ManagerSharedPreferences.getInstance().getDataFromSharedPreferences(getContext(), FILE_SHARED_DATA_USER, KEY_ID));
        newUserMap.put("nombre", nombreNuevo);
        newUserMap.put("apellido", apellidoNuevo);
        newUserMap.put("usuario", usuarioNuevo);
        newUserMap.put("correo", correoNuevo);

        Call<RespSrvUser> call = apiUserService.updateData(newUserMap);
        call.enqueue(new Callback<RespSrvUser>() {
            @Override
            public void onResponse(Call<RespSrvUser> call, Response<RespSrvUser> response) {
                if (response.code() == 200) {
                    Toast.makeText(getContext(), "Datos actualizados exitosamente ", Toast.LENGTH_SHORT).show();
                    respSrvRegister = response.body();
                    updateDataUserLocal();
                    // TODO: actualizar los datos que aparecen en el menu
                    updateInfoNavMain();
                    return;
                }
                if (response.code() == 400) {
                    Log.d("RESP_UPDATE_ERROR", "PETICION MAL FORMADA: "+response.errorBody());
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.errorBody().string());
                        String userMessage = jsonObject.getString("messaging");
                        Log.d("RESP_UPDATE_ERROR", "Msg de la repuesta: "+userMessage);
                        Toast.makeText(getContext(), "No se pudieron actualizar los datos << "+userMessage+" >>", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            @Override
            public void onFailure(Call<RespSrvUser> call, Throwable t) {
                Log.d("RESP_UPDATE_ERROR", "error: "+t.getMessage());
                Toast.makeText(getContext(), "Existen problemas con la conexion al servidor ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // actualizamos los datos del usuario almacenados localmente
    private void updateDataUserLocal() {
        ManagerSharedPreferences.getInstance().setDataFromSharedPreferences(getContext(), FILE_SHARED_DATA_USER, KEY_NAME, respSrvRegister.getNombre());
        ManagerSharedPreferences.getInstance().setDataFromSharedPreferences(getContext(), FILE_SHARED_DATA_USER, KEY_LASTNAME, respSrvRegister.getApellido());
        ManagerSharedPreferences.getInstance().setDataFromSharedPreferences(getContext(), FILE_SHARED_DATA_USER, KEY_EMAIL, respSrvRegister.getCorreo());
        ManagerSharedPreferences.getInstance().setDataFromSharedPreferences(getContext(), FILE_SHARED_DATA_USER, KEY_USERNAME, respSrvRegister.getUsuario());
    }

    // actualizamos los datos q se muestran en el menu principal
    private void updateInfoNavMain() {
        // recuperamos la navbar main
//        View navbarMain = getActivity().findViewById(R.id.navbar_head_main);
//        TextView tvTitle = navbarMain.findViewById(R.id.tv_user_navbar_main);
//        TextView tvSubtitle = navbarMain.findViewById(R.id.tv_correo_navbar_main);
//
//        // actualizamos los datos
//        tvTitle.setText(ManagerSharedPreferences.getInstance().getDataFromSharedPreferences(getContext(), FILE_SHARED_DATA_USER, KEY_USERNAME));
//        tvSubtitle.setText(ManagerSharedPreferences.getInstance().getDataFromSharedPreferences(getContext(), FILE_SHARED_DATA_USER, KEY_EMAIL));
    }

    private boolean formCorrect(){
        if(!Validations.isNombre(edtNombre)){
//            Log.d("VALIDACIONES",  "Nombre incorrecto");
            return false;
        }
        if(!Validations.isNombre(edtApellido)){
//            Log.d("VALIDACIONES",  "Apellido incorrecto");
            return false;
        }
        if(Validations.isEmpty(edtCorreo)){
//            Log.d("VALIDACIONES",  "Nombre de usuario vacio");
            return false;
        }
        if(Validations.isEmpty(edtusuario)){
//            Log.d("VALIDACIONES",  "Correo vacio");
            return false;
        }

        if (!Validations.isEmail(edtCorreo)){
//            Log.d("VALIDACIONES",  "Email incorrecto");
            return false;
        }

        return true;
    }

    private void getValuesFields() {
        nombreNuevo = edtNombre.getText().toString();
        apellidoNuevo = edtApellido.getText().toString();
        usuarioNuevo = edtusuario.getText().toString();
        correoNuevo = edtCorreo.getText().toString();

        //Toast.makeText(getApplicationContext(), nombre+" - "+apellido+" - "+usuario+" - "+correo+" - "+pass+" - "+confPass, Toast.LENGTH_SHORT).show();
    }

    // TODO: Rename method, update argument and hook method into UI event
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
