package co.droidmesa.silva.sensores;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    // private: visibilidad de las variables, solo son accesibles desde la clase donde se declaran.
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;

    // almacenan el valor de las coordenadas, inicialmente son iguales a cero.
    private float last_x, last_y, last_z = 0;

    /* tvX: muestra los valores de la coordenada X
    *   tvY: ´'' y
    *   tvZ: ,,, z
    * */
    private TextView tvX, tvY, tvZ, tvCantidad;

    // Tipos de variables GraphView, definen grupos de datos para graficar
    private LineGraphSeries<DataPoint> series_x;
    private LineGraphSeries<DataPoint> series_y;
    private LineGraphSeries<DataPoint> series_z;
    private GraphView graph;

    // time: contador de datos
    private int time = 0;
    // numero de datos que se van a tomar
    private final static int numData = 500;
    //
    private long startTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicializando variables
        initAccelerometer();

        // asociación de los TextView con los declarados en los layout
        tvX = (TextView)findViewById(R.id.tvX);
        tvY = (TextView)findViewById(R.id.tvY);
        tvZ = (TextView)findViewById(R.id.tvZ);
        tvCantidad = (TextView)findViewById(R.id.tvCant);

        graph = (GraphView)findViewById(R.id.graph);
        // configuración de la vista de la gráfica
        Viewport viewport = graph.getViewport();
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-30);
        viewport.setMaxY(30);
        viewport.setScalable(true);
        viewport.setScrollable(true);

        initGraph();
    }

    private void initAccelerometer() {
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // función que se ejecuta cuando se mueve el celular
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        // si el sensor no funciona mostrará un mensaje
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Toast.makeText(this, "Acelerómetro dañada", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Si es el primer evento, se usa ese valor como el valor base
            if (startTimestamp == 0) {
                startTimestamp = event.timestamp;
            }

            // Lapso de tiempo que ocurre entre los dos eventos
            double elapsedTime = (event.timestamp - startTimestamp)/1000000000.0;

            last_x = x;
            last_y = y;
            last_z = z;


            if (time > numData){
                onStop();
            } else {
                String a = Integer.toString(time++);
                tvX.setText(String.valueOf(last_x));
                tvY.setText(String.valueOf(last_y));
                tvZ.setText(String.valueOf(last_z));
                tvCantidad.setText(a);

                series_x.appendData(new DataPoint(elapsedTime, last_x), true, 100);
                series_y.appendData(new DataPoint(elapsedTime, last_y), true, 100);
                series_z.appendData(new DataPoint(elapsedTime, last_z), true, 100);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size()> 0){
            sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    public void stopSensor(View view) {
        sensorManager.unregisterListener(this);
    }

    private void initGraph() {
        // inicialización de series
        series_x = new LineGraphSeries<>();
        series_y = new LineGraphSeries<>();
        series_z = new LineGraphSeries<>();

        // Le ponemos colores a las gráficas
        series_x.setColor(ContextCompat.getColor(this, R.color.serie_x));
        series_y.setColor(ContextCompat.getColor(this, R.color.serie_y));
        series_z.setColor(ContextCompat.getColor(this, R.color.serie_z));

        // Se añaden las tres series a la gráfica
        graph.addSeries(series_x);
        graph.addSeries(series_y);
        graph.addSeries(series_z);
    }

    public void resetGraph(View view) {
        // dejamos de escuchar los eventos del acelerómetro
        sensorManager.unregisterListener(this);
        // hacemos igual a cero las variables iniciales
        startTimestamp = 0;
        time = 0;
        // Reinicio los datos de cada serie, haciendo que inicien nuevamente en (0,0)
        series_x.resetData(new DataPoint[]{new DataPoint(0,0)});
        series_y.resetData(new DataPoint[]{new DataPoint(0,0)});
        series_z.resetData(new DataPoint[]{new DataPoint(0,0)});
        initAccelerometer();
    }
}
