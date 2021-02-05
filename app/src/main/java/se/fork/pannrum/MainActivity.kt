package se.fork.pannrum

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.iid.FirebaseInstanceId
import de.nitri.gauge.Gauge
import kotlinx.android.synthetic.main.activity_main.*
import se.fork.pannrum.model.TempRecord
import se.fork.pannrum.util.FirebaseHelper
import timber.log.Timber
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var smokeTempGauge: Gauge
    private lateinit var boilerTempGauge: Gauge
    private lateinit var pumpLeftTempGauge: Gauge
    private lateinit var pumpRightTempGauge: Gauge
    private lateinit var pumpTopTempGauge: Gauge
    private lateinit var accTank1TopTempGauge: Gauge
    private lateinit var accTank1BottomTempGauge: Gauge
    private lateinit var accTank2TopTempGauge: Gauge
    private lateinit var accTank2BottomTempGauge: Gauge
    private lateinit var accTank3TopTempGauge: Gauge
    private lateinit var accTank3BottomTempGauge: Gauge

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
        handleDeviceToken()
    }

    override fun onPause() {
        super.onPause()
        stopListening()
    }

    override fun onResume() {
        super.onResume()
        listen()
    }

    fun initUI() {
        smokeTempGauge = smoke_gauge
        boilerTempGauge = boiler.findViewById(R.id.top_gauge)
        pumpLeftTempGauge = ladddomat_left_gauge
        pumpRightTempGauge = ladddomat_right_gauge
        pumpTopTempGauge = ladddomat_top_gauge
        accTank1TopTempGauge = acc_1.findViewById(R.id.top_gauge)
        accTank1BottomTempGauge = acc_1.findViewById(R.id.bottom_gauge)
        accTank2TopTempGauge = acc_2.findViewById(R.id.top_gauge)
        accTank2BottomTempGauge = acc_2.findViewById(R.id.bottom_gauge)
        accTank3TopTempGauge = acc_3.findViewById(R.id.top_gauge)
        accTank3BottomTempGauge = acc_3.findViewById(R.id.bottom_gauge)
    }

    fun writeTestRecord() {
        Timber.d("writeTestRecord")
        if (FirebaseHelper.isLoggedIn()) {
            val rec = TempRecord.createTestRecord()
            FirebaseHelper.writeTempRecord(rec)
        }
    }

    fun listen() {
        Timber.d("listen")
        if (FirebaseHelper.isLoggedIn().not()) {
            Timber.d("listen: Not logged in, logging in...")
            FirebaseHelper.login(this, "pnr2021", {user -> startListening(user)}, { error -> showAuthError(error)})
        } else {
            Timber.d("listen: Logged in, starting listener")
            startListening(FirebaseHelper.currentUser)
            playVideo("gs://pannrum-7e210.appspot.com/videos/video.avi", Date())
        }
    }

    private fun handleDeviceToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.e(task.exception,"handleDeviceToken: getInstanceId failed")
                    return@OnCompleteListener
                }
                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                Timber.d("handleDeviceToken: Got token: $token")
                token?.let {
                    registerDeviceToken(token)
                }
            })
    }

    fun registerDeviceToken(token: String) {
        Timber.d("registerDeviceToken")
        if (FirebaseHelper.isLoggedIn().not()) {
            Timber.d("registerDeviceToken: Not logged in, logging in...")
            FirebaseHelper.login(this, "pnr2021", {user -> startListening(user)}, { error -> showAuthError(error)})
        } else {
            Timber.d("registerDeviceToken: Logged in, registering token $token")
            FirebaseHelper.registerDeviceToken(token)
        }
    }

    fun startListening(user: FirebaseUser?) {
        writeTestRecord()
        Timber.d("startListening: ${user?.email}")
        if (FirebaseHelper.isListeningForTemperatures().not()) {
            Timber.d("startListening: Starting to listen")
            FirebaseHelper.listenForTemperatures({rec -> updateUI(rec)}, {databaseError -> showDatabaseError(databaseError) })
        }
    }

    fun stopListening() {
        if (FirebaseHelper.isListeningForTemperatures()) {
            Timber.d("stopListening: Listening - stopping")
            FirebaseHelper.stopListeningForTemperatures()
        }
    }

    fun updateUI(tempRec: TempRecord?) {
        Timber.d("updateUI: $tempRec")
        tempRec?.let {
            updateGauges(tempRec)
        }
    }

    fun updateGauges(record: TempRecord) {
        Timber.d("updateGauges: $record")
        record.boilerTemp?.let {
            boilerTempGauge.moveToValue(record.boilerTemp)
        }
        record.smokeTemp?.let {
            smokeTempGauge.moveToValue(record.smokeTemp)
        }
        record.pumpLeftTemp?.let {
            pumpLeftTempGauge.moveToValue(record.pumpLeftTemp)
        }
        record.pumpRightTemp?.let {
            pumpRightTempGauge.moveToValue(record.pumpRightTemp)
        }
        record.pumpTopTemp?.let {
            pumpTopTempGauge.moveToValue(record.pumpTopTemp)
        }
        record.accTank1TopTemp?.let {
            accTank1TopTempGauge.moveToValue(record.accTank1TopTemp)
        }
        record.accTank1BottomTemp?.let {
            accTank1BottomTempGauge.moveToValue(record.accTank1BottomTemp)
        }
        record.accTank2TopTemp?.let {
            accTank2TopTempGauge.moveToValue(record.accTank2TopTemp)
        }
        record.accTank2BottomTemp?.let {
            accTank2BottomTempGauge.moveToValue(record.accTank2BottomTemp)
        }
        record.accTank3TopTemp?.let {
            accTank3TopTempGauge.moveToValue(record.accTank3TopTemp)
        }
        record.accTank3BottomTemp?.let {
            accTank3BottomTempGauge.moveToValue(record.accTank3BottomTemp)
        }
    }

    fun showAuthError(error: Throwable?) {
        Toast.makeText(this, "Inloggningen misslyckades: ${error?.localizedMessage}", Toast.LENGTH_LONG).show()
    }

    fun showDatabaseError(error: DatabaseError) {
        Toast.makeText(this, "Databasläsning misslyckades: ${error.toException().localizedMessage}", Toast.LENGTH_LONG).show()
    }

    fun playVideo(videoUrl: String, videoTime: Date) {
        video_view.setVideoURI(Uri.parse(videoUrl))
        video_view.setOnPreparedListener {
            it.start()
        }
    }
}