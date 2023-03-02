package com.agrobot.chatbot.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.RenderScript
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.agrobot.chatbot.R
import com.agrobot.chatbot.data.Message
import com.agrobot.chatbot.utils.BotResponse
import com.agrobot.chatbot.utils.Constants.RECEIVE_ID
import com.agrobot.chatbot.utils.Constants.SEND_ID
import com.agrobot.chatbot.utils.Time
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.*


private lateinit var fusedLocationClient: FusedLocationProviderClient

private var vc_msg:String = ""

class MainActivity : AppCompatActivity() {
    private val RQ_SPEECH_REC = 102
    private val TAG = "MainActivity"

    var messagesList = mutableListOf<Message>()

    private lateinit var adapter: MessagingAdapter
    private val botList = listOf("Peter", "Francesca", "Luigi", "Igor")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       // fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val random = (0..3).random()
        val botname = botList[random]

        customBotMessage("Hello! Today you're speaking with ${botname}, how may I help?")
        setTitle("AgroBot : ${botname}")
        recyclerView()

        btn_send.setOnClickListener {
            sendMessage()
        }

        //Scroll back to correct position when user clicks on text view
        et_message.setOnClickListener {
            GlobalScope.launch {
                delay(100)

                withContext(Dispatchers.Main) {
                    rv_messages.scrollToPosition(adapter.itemCount - 1)

                }
            }
        }

        btn_send_audio.setOnClickListener {
            askSpeechInput()
           // botResponse(vc_msg)


        }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK){
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            vc_msg = result?.get(0).toString()
            sendMessageVC(vc_msg)
            //et_message.setText(result?.get(0).toString())

        }

    }

    private fun askSpeechInput() {
        if(!SpeechRecognizer.isRecognitionAvailable(this)){
            Toast.makeText(this,"speech recognition is not availible" , Toast.LENGTH_SHORT).show()
        }else{
            val i = Intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()) //language detection
            i.putExtra(RecognizerIntent.EXTRA_PROMPT , "speak") // to prompt user when open speech dialogbox
            startActivityForResult(i , RQ_SPEECH_REC)
        }


    }



    private fun recyclerView() {
        adapter = MessagingAdapter()
        rv_messages.adapter = adapter
        rv_messages.layoutManager = LinearLayoutManager(applicationContext)

    }

    override fun onStart() {
        super.onStart()
        //In case there are messages, scroll to bottom when re-opening app
        GlobalScope.launch {
            delay(100)
            withContext(Dispatchers.Main) {
                rv_messages.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun sendMessage() {
        val message = et_message.text.toString()
        val timeStamp = Time.timeStamp()

        if (message.isNotEmpty()) {
            //Adds it to our local list
            messagesList.add(Message(message, SEND_ID, timeStamp))
            et_message.setText("")

            adapter.insertMessage(Message(message, SEND_ID, timeStamp))
            rv_messages.scrollToPosition(adapter.itemCount - 1)

            botResponse(message)
        }
    }

    private fun sendMessageVC(message: String) {
        //val message = et_message.text.toString()
        val timeStamp = Time.timeStamp()

        if (message.isNotEmpty()) {
            //Adds it to our local list
            messagesList.add(Message(message, SEND_ID, timeStamp))
            et_message.setText("")

            adapter.insertMessage(Message(message, SEND_ID, timeStamp))
            rv_messages.scrollToPosition(adapter.itemCount - 1)

            botResponse(message)
        }
    }

    private fun botResponse(message: String) {
        val timeStamp = Time.timeStamp()

        GlobalScope.launch {
            //response delay

            delay(1000)

            withContext(Dispatchers.Main) {
                //Gets the response
                val response = BotResponse.basicResponses(message)

                //Adds it to our local list
                messagesList.add(Message(response, RECEIVE_ID, timeStamp))

                //Inserts our message into the adapter
                adapter.insertMessage(Message(response, RECEIVE_ID, timeStamp))

                //Scrolls us to the position of the latest message
                rv_messages.scrollToPosition(adapter.itemCount - 1)



                }
            }
        }


    private fun customBotMessage(message: String) {

        GlobalScope.launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                val timeStamp = Time.timeStamp()
                messagesList.add(Message(message, RECEIVE_ID, timeStamp))
                adapter.insertMessage(Message(message, RECEIVE_ID, timeStamp))

                rv_messages.scrollToPosition(adapter.itemCount - 1)
            }
        }
    }
}



