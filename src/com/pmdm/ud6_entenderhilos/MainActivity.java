package com.pmdm.ud6_entenderhilos;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;

public class MainActivity extends Activity {

	/**Declaracion de atributos de clase **/
    private static TextView txtView; // Declaramos un TextView estatico
    private ProgressBar pbarProgreso; //Declaración de la barra de progreso
    private MiTareaAsincrona tarea1; //Declaracion la tarea asincrona


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //Aqui enlazo los objetos locales con la interface. Solo necesito al Textview y la barra de progreso.
        txtView = (TextView) findViewById(R.id.textView1);
        pbarProgreso = (ProgressBar)findViewById(R.id.pbarProgreso);
    }

    
        
    /**
     * Método que pretende mostrar en un textView los números del 1 al 1000
     * y provoca un ANR** 
     * @param view
     */
    private void startCounter(View view) {
        for (int i = 1; i <= 1000; i++) {
            txtView.setText(String.valueOf(i));
            try {
		        Thread.sleep(1000);
		    } catch(InterruptedException e) {}
        }
    }

    
    /**
     * Método que se inicia al hacer click en el boton 'Contador_sin_hilos'
     * @param v
     */
    public void onClickContador_sin_hilos(View v) {
        startCounter(v);
        
    }
        
    /**
     * Método que se inicia al hacer click en el boton 'Contador_con_hilos_mal' y falla
     * intencionadamente ya que intenta modificar la interfaz de usuario desde un
     * hilo.
     * 
     * Provoca una FATAL Exception no capturable.
     * Para mas información :
     * http://stackoverflow.com/questions/11685708/android-app-throwing-a-fatal-exception-background-thread-when-setting-button-st 
     * @param v
     */
    public void onClickContador_con_hilos_mal(View v) {       
            new Thread(new Runnable() {
                public void run() {
                    for (int i = 1; i <= 1000; i++) {
                        txtView.setText(String.valueOf(i));
                        // pausa para dar tiempo a que se muestre el valor antes de
                        // pasar al siguiente
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Log.d("Threading", e.getLocalizedMessage());
                        }
                    }
                }
            }).start();
    }

    /**
     * Método que se inicia al hacer click en el boton 'Contador_con_post' que implementa una posible
     * soluciona al problema del método anterior
     * @param v
     */
    public void onClickContador_con_hilos_post(View v) {
        // para resolver el problema en Android se requiere un bloque Runnable
        // adicional pasado al TextView mediante su método post()
        new Thread(new Runnable() {
            public void run() {
                for (int i = 1; i <= 1000; i++) {
                    final int contador = i; // almacenar el nuevo valor
                    // actualizar la interfaz de usuario (bloque Runnable
                    // adicional para el método .post())
                    txtView.post(new Runnable() {
                        public void run() {
                            // hilo de ejecución de la interfaz de usuario
                            txtView.setText(String.valueOf(contador));
                        }
                    });
                    // pausa para dar tiempo a que se muestre el valor antes de
                    // pasar al siguiente
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.d("Threading", e.getLocalizedMessage());
                    }
                }
            }
        }).start();
    }

    
    /**
     * Clase Manejador utilizada para actualizar la Interfaz de Usuario de la actividad principal     * 
     */
    private static Handler UIactualiza = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] buffer = (byte[]) msg.obj;            
            String strRecibido = new String(buffer);//---convierte el array de byte a string---
            txtView.setText(strRecibido); //---Muestra el texto recibido en el TextView--- 
            Log.d("Threading", "corriendo");
        }
    };

    /**
     * Metodo que se inicia al hacer Click en el boton Contador_con_hilos_handler
     * @param view
     */
    public void onClickContador_con_hilos_handler(View view) {
        new Thread(new Runnable() {
            public void run() {
                for (int i = 1; i <= 1000; i++) {
                    // Aqui es donde usamos el manejador para actualizar la act. principal
                    MainActivity.UIactualiza.obtainMessage(
                            0, String.valueOf(i).getBytes()).sendToTarget();
                    // --- introducir un retraso
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.d("Threading", e.getLocalizedMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Metodo que se inicia al hacer Click en el boton Contador_Asyntask
     * y utiliza la clase 'MiTareaAsincrona' para actualizar la barra de progreso y el textView
     * @param v
     */
    public void onClickContador_Asyntask(View v)
    {
        tarea1 = new MiTareaAsincrona(); // Aqui instaciamos la tarea
        tarea1.execute(); // Ejecutamos la tarea Asincrona
    }

    /**
     * Método que para la tarea asincrona 'tarea1'
     * @param v
     */
    public void onClickPararContadorAsyntask(View v)
    {
    	
        try {
			tarea1.cancel(true);
		} catch (Exception e) {
			Toast.makeText(MainActivity.this, "Excepcion "+e, Toast.LENGTH_SHORT).show();
		}
    }

    /**
     * Clase MiTareaAsincrona que implementa un contador para actualizar un textview y una barra de progreso
     * Se trate de una clase incluida en otra clase, por esto es private. 
     * @author José Javier García Romero
     *
     */
    private class MiTareaAsincrona extends AsyncTask<Void, Integer, Boolean> {
    	
    	/**
    	 * Método que hace una tarea larga o pesada en segundo plano
    	 */
    	@Override
    	protected Boolean doInBackground(Void... params) 
    	{

    		for(int i=1; i<=10; i++) {
    			tareaLarga(); // La tarea larga consiste en esperar 10 intervalos de tiempo

    			publishProgress(i*100); // El progreso aumentara en 100 unidades por intervalo

    			if(isCancelled()) // Si cancelamos la tarea sale del bucle
    				break;
    		}
    		return true; // Devuelve siempre true 
    	}
    	
    	/**
    	 * Método que se dispara al hacer cualquier actualización en la tarea Asincrona.
    	 */
        @Override
        protected void onProgressUpdate(Integer... values) {
            int progreso = values[0].intValue(); // actualiza a la variable progreso
            txtView.setText(String.valueOf(progreso)); // actualiza el textView
            pbarProgreso.setProgress(progreso); // actualiza la barra de progreso
        }

        /**
         * Método que se ejecuta justo antes de lanzar la tarea Asincrona.
         */
        @Override
        protected void onPreExecute() {
            pbarProgreso.setMax(1000); // Aqui preparamos la barra para albergar hasta 1000 unidades maximo
            pbarProgreso.setProgress(0); // Aqui inicializamos el progreso de la barra a 0
        }

        /**
         * Método que se ejecuta despues de que termine la tarea Asincrona
         */
        @Override
        protected void onPostExecute(Boolean result) {
            if(result)
            	//Mostramos un mensaje de tarea finalizada
                Toast.makeText(MainActivity.this, "Mi Tarea Asincrona finalizada!", Toast.LENGTH_SHORT).show();
        }

        /**
         * Método que se ejecuta si cancelamos una tarea Asincrona
         */
        @Override
        protected void onCancelled() {
        	//Mostramos un mensaje de tarea terminada
            Toast.makeText(MainActivity.this, "Mi Tarea Asincrona cancelada!", Toast.LENGTH_SHORT).show();
        }
        
        /**
         * Método que simula una tarea de larga duracion
         */
		private void tareaLarga()
		{
		    try {
		        Thread.sleep(1000);
		    } catch(InterruptedException e) {}
		}
    }


}




