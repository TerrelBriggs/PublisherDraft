package com.example.publisherdraft

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import java.util.UUID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.publisherdraft.PublishContent
import com.google.gson.Gson


class MainActivity : AppCompatActivity() {

    private var client: Mqtt5BlockingClient? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 10.6416
    private var longitude: Double = 61.3995
    private var studentID: String = "816035550"
    private var content: PublishContent = PublishContent(studentID, latitude, longitude)
    private var contentPackaged = Gson().toJson(content)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var editText = findViewById<EditText>(R.id.idEntry)

        client = Mqtt5Client.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker-816035550.sundaebytestt.com")
            .serverPort(1883)
            .build()
            .toBlocking()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                Log.e("LOC","Latitude: $latitude, Longitude: $longitude")
            } else {
                Toast.makeText(this,"Location is null. Try enabling location or check permissions.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.e("LOC","Failed to get location")
        }
    }

    fun startPublish(view: View?) {

        var userID = findViewById<EditText>(R.id.idEntry)
        studentID = userID.text.toString()
        Log.i("ID", "Student ID is: $studentID")


        try {
            client?.connect()
            Log.i("MQTT", "Connected to broker")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "An error occurred when connecting to broker", Toast.LENGTH_SHORT)
                .show()
        }

        getLastLocation()
        content = PublishContent(studentID, latitude, longitude)
        contentPackaged = Gson().toJson(content)


        sendData()
    }

    fun stopPublish(view: View?){
        try {
            client?.disconnect()
            Toast.makeText(this, "Disconnected successfully", Toast.LENGTH_SHORT).show()
        } catch (e:Exception){
            Toast.makeText(this,"An error occurred when disconnecting from broker", Toast.LENGTH_SHORT).show()
        }
    }


    fun sendData(){
        try{
            client?.publishWith()?.topic("assignment/location")?.payload(contentPackaged.toByteArray())?.send() //send ID and location in content model?
            Log.i("MQTT", "Message publish success")
            Toast.makeText(this,"Location published successfully", Toast.LENGTH_SHORT).show()
        } catch (e:Exception){
            Toast.makeText(this,"An error occurred when sending a message to the broker", Toast.LENGTH_SHORT).show()
        }
    }
}