package se.fork.pannrum.util

import android.app.Activity
import android.net.Uri
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import se.fork.pannrum.model.TempRecord
import se.fork.pannrum.model.VideoRecord
import timber.log.Timber
import java.io.File
import java.lang.Exception


object FirebaseHelper {
    val database = FirebaseDatabase.getInstance()
    val tempDbRef = database.getReference("CurrentValues")
    val deviceDbRef = database.getReference("DeviceTokens")
    val videosDbRef = database.getReference("Videos")
    val storage = FirebaseStorage.getInstance()
    val videoRef = storage.getReference("videos")
    var tempListener : ValueEventListener? = null
    var videoListener : ChildEventListener? = null
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

    fun isListeningForVideos() : Boolean {
        return videoListener != null
    }



    fun listenForVideos(onSuccess:  (VideoRecord?) -> Unit, onError: (DatabaseError) -> Unit ) {
        stopListeningForVideos()
        Timber.d("listenForVideos on $videosDbRef")
        videoListener = videosDbRef.limitToLast(1).addChildEventListener(object : ChildEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                Timber.e(databaseError.toException(),"onCancelled: ${databaseError.details}")
                onError(databaseError)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Timber.e("onChildMoved should never be called")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Timber.e("onChildChanged should never be called")
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Timber.d("listenForVideos 0: ${dataSnapshot.value}")
                var videoRecord = dataSnapshot.getValue(VideoRecord::class.java)
                Timber.d("listenForVideos 1: Got $videoRecord")
                videoRecord?.key = dataSnapshot.key
                Timber.d("listenForVideos 2: Got $videoRecord")
                onSuccess(videoRecord)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

        })
    }

    fun stopListeningForVideos() {
        videoListener?.let {
            videosDbRef.removeEventListener(videoListener!!)
            videoListener = null
        }
    }

    fun downloadVideo(key: String, onSuccess: (File) -> Unit, onError: (Exception) -> Unit) {
        val localFile: File = File.createTempFile("video", ".mp4")
        val newVideoRef = videoRef.child(key).child("video.mp4")
        Timber.d("downloadVideo: newVideoRef = $newVideoRef")
        newVideoRef.getFile(localFile)
            .addOnSuccessListener(OnSuccessListener<FileDownloadTask.TaskSnapshot?> {
                Timber.d("downloadVideo: Success downloading video: $it")
                onSuccess(localFile)
            }).addOnFailureListener(OnFailureListener {
                Timber.e(it, "downloadVideo: Failed to download video")
                onError(it)
            })
    }
}