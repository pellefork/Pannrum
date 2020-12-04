package se.fork.pannrum.util

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import se.fork.pannrum.model.TempRecord
import timber.log.Timber

object FirebaseHelper {
    val database = FirebaseDatabase.getInstance()
    val tempDbRef = database.getReference("CurrentValues")
    val deviceDbRef = database.getReference("DeviceTokens")
    var tempListener : ValueEventListener? = null
    var auth = FirebaseAuth.getInstance()
    val loggedInUser = "lillhagsbacken@gmail.com"

    fun login(activity: Activity, password: String, onSuccess:  (FirebaseUser?) -> Unit, onError: (Throwable?) -> Unit ) {
        val email = loggedInUser
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Timber.d( "signInWithEmail:success $user")
                    onSuccess(user)
                } else {
                    Timber.e( task.exception, "signInWithEmail:failure")
                    onError(task.exception)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn() : Boolean {
        return auth.currentUser?.email.equals(loggedInUser)
    }

    var currentUser = auth.currentUser

    fun registerDeviceToken(token: String) {
        deviceDbRef.child(token).setValue(true)
    }

    fun isListeningForTemperatures() : Boolean {
        return tempListener != null
    }

    fun writeTempRecord(rec : TempRecord) {
        tempDbRef.child("Temperatures").setValue(rec)
    }

    fun listenForTemperatures(onSuccess:  (TempRecord?) -> Unit, onError: (DatabaseError) -> Unit ) {
        val path = tempDbRef.child("Temperatures")
        stopListeningForTemperatures()
        Timber.d("listenForTemperatures on $path")
        tempListener = path.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Timber.d("onDataChange: dataSnapshot.getValue() ${dataSnapshot.getValue()}")
                val temps = dataSnapshot.getValue(TempRecord::class.java)
                Timber.d("listenForDevice: Got $temps")
                onSuccess(temps)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Timber.e(databaseError.toException(),"onCancelled: ${databaseError.details}")
                onError(databaseError)
            }
        })
    }

    fun stopListeningForTemperatures() {
        tempListener?.let {
            tempDbRef.removeEventListener(tempListener!!)
            tempListener = null
        }
    }
}