package com.example.receiver

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.cs442_hw2.FFT
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import java.lang.Byte


class MainActivity : AppCompatActivity() {

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted = false
    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == 200) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    private var bitString: String = ""
    private var freqs: IntArray = IntArray(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, permissions, 200)

        //initialize recorder & get buffer size
        val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,1024)
        val bufSize = AudioRecord.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        //begin recording
        recordAudio(recorder, bufSize)
    }

    //record the audio into array
    private fun recordAudio(recorder: AudioRecord, size : Int) {
        doAsync{
            recorder.startRecording()
            while (true){
                var arr : ShortArray = ShortArray(size)
                recorder.read(arr, 0,size)
                val fftresult = FFT(512).getFreqSpectrumFromShort(arr) //n = 512, array result => 257, 19kHz = 221, 20kHz = 232, 21kHz = 244

                //NOTE: Must handle -1 exception in case wrong freq detected?
                //update the bitstring everytime new bit is discovered
                var b = getLatestBit(maxFreq(fftresult))
                if ( b != -1 )
                    bitString+=b
                //UI update every time text received
                var ch = byteToChar()
                //TESTER runOnUiThread {var x = 0; for(i in fftresult){ x++; if(i>0 && (x==221 || x==232 || x==244))text_box.text = text_box.text.toString() + x + " " + i +"\n"};}
                //if(ch != '-') {
                runOnUiThread {
                    //text_box.text = text_box.text.toString() + "test"//text_box.toString() + "hello"
                    if(!ch.equals("-"))
                        text_box.setText(text_box.text.toString() + ch) //TEST: '-' represents pause/break
                }
                //}
            }
        }
    }

    //convert byte to ASCII
//    private fun parseBits(s:String):String {
//        for ( i in s ){
//            if (Integer.parseInt(i.toString()) == 0){
//
//            }
//        }
//    }

    //convert the bitstring to output
    private fun byteToChar(): String {
        if(bitString.count() == 8){
            //TODO parse
            val byte = bitString
            bitString = ""
            return BinaryConverter.binaryToString(byte)
        } else {
            return "-"
        }
    }

    //get the latest char
    private fun getLatestBit(latestFreq:Int): Int {
        if(freqs.isEmpty()){
            freqs[0] = latestFreq
            return -1
        }
        else if (freqs.count() == 1) {
            freqs[1] = latestFreq
            return -1
        }
        else {
            freqs[0] = freqs[1]
            freqs[1] = latestFreq
        }

        when (freqs[0]) {
            19 -> when (freqs[1]) {
                19 -> {
                    return -1
                }
                20 -> {
                    return 0
                }
                21 -> {
                    return 1
                }
            }
            20 -> when (freqs[1]) {
                19 -> {
                    return 1
                }
                20 -> {
                    return -1
                }
                21 -> {
                    return 0
                }
            }
            21 -> when (freqs[1]) {
                19 -> {
                    return 1
                }
                20 -> {
                    return 0
                }
                21 -> {
                    return -1
                }
            }
        }
        return -1 //failed if it didn't call switch statement above...
    }

    private fun maxFreq(fft:DoubleArray): Int {
        val hz19 = fft[221]
        val hz20 = fft[232]
        val hz21 = fft[244]

        if(hz19 > hz20 && hz19 > hz21)
            return 19
        else if(hz20 > hz19 && hz20 > hz21)
            return 20
        else
            return 21
    }
    object BinaryConverter {

        // ...

        fun binaryToString(binary:String) : String {
            if (!isBinary(binary))
                return "Not a binary value";

            val chars = CharArray(binary.length / 8)
            var i = 0

            while (i < binary.length) {
                val str = binary.substring(i, i + 8)
                val nb = Integer.parseInt(str, 2)
                chars[i / 8] = nb.toChar()
                i += 8
            }

            return String(chars)
        }

        fun isBinary(txt:String?):Boolean {
            if (txt != null  &&  txt.length % 8 == 0) {
                for (c in txt.toCharArray()) {
                    if (c != '0'  &&  c!= '1')
                        return false
                }

                return true
            }

            return false
        }
    }
}
