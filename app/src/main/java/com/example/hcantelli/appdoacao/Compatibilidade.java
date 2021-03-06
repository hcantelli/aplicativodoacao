package com.example.hcantelli.appdoacao;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Compatibilidade extends AppCompatActivity {
    private DatabaseReference bancoDeDados_firebase = FirebaseDatabase.getInstance().getReference();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compatibilidade);

        //Declaração de variáveis
        final double[] compatibilidade = {0};
        final ArrayList<Double> compatibilidadePorAnimal = new ArrayList<>();
        final ArrayList<String> idAnimal = new ArrayList<>();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        final String idUsuario = bundle.getString("Usuarios");

        final ProgressDialog progressDialog = ProgressDialog.show(Compatibilidade.this, getText(R.string.aguarde), getText(R.string.calculo_compatibilidade), true);
        progressDialog.show();

        //Evento realizado para verificar o cálculo da compatibilidade do usuário com os animais na base de dados
        bancoDeDados_firebase
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final ArrayList<Double> caracteristicasAdotante = new ArrayList<>();
                        final ArrayList<Double> caracteristicasAnimal = new ArrayList<>();

                            //Evento realizado para armazenar os dados do Adotante
                            bancoDeDados_firebase.child("ResultadoIA").child(idUsuario)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                caracteristicasAdotante.add((Double) ds.getValue());
                                            }
                                            //Evento realizado para armazenar os identificadores de cada Animal
                                            bancoDeDados_firebase.child("Animais").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot ds : dataSnapshot.getChildren()){
                                                        idAnimal.add(String.valueOf(ds.getKey()));
                                                    }

                                                    //Procura por cada animal para calcular cada característica com o Adotante
                                                    for(int count = 0; count < dataSnapshot.getChildrenCount();){
                                                        //Evento realizado para armazenar cada característica do Animal
                                                        bancoDeDados_firebase.child("Animais").child(idAnimal.get(count)).child("Personalidade")
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                                        caracteristicasAnimal.add(Double.parseDouble(ds.getValue().toString()));
                                                                    }
                                                                    //Cálculo do erro médio quadrático
                                                                    for (int count2 = 0; count2 < 10; count2++){
                                                                        compatibilidade[0] += Math.pow(caracteristicasAdotante.get(count2) - caracteristicasAnimal.get(count2), 2);
                                                                    }
                                                                    compatibilidadePorAnimal.add(Math.sqrt(compatibilidade[0]));
                                                                    caracteristicasAnimal.clear();
                                                                    compatibilidade[0] = 0;
                                                                    Handler handler = new Handler();
                                                                    handler.postDelayed(new Runnable() {
                                                                        public void run() {
                                                                            progressDialog.dismiss();
                                                                            Intent intent = new Intent(Compatibilidade.this, ListaDeAnimais.class);
                                                                            intent.putExtra(("CompatibilidadeAnimal"), compatibilidadePorAnimal);
                                                                            intent.putStringArrayListExtra(("idAnimal"), idAnimal);
                                                                            startActivity(intent);
                                                                        }
                                                                    }, 5000);

                                                                }
                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {
                                                                }
                                                            });
                                                        count++;
                                                        }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });


                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                            caracteristicasAdotante.clear();


                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
                );
    }
}
