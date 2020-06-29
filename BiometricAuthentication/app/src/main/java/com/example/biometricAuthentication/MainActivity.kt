package com.example.biometricAuthentication

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class MainActivity : AppCompatActivity() {
    private val KEY_NAME = "hello"
    private val pwd = "12345";
    private lateinit var secretKey: SecretKey
    private lateinit var biometricManager: BiometricManager;
    private lateinit var biometricPrompt: BiometricPrompt;
    private lateinit var promptInfo: BiometricPrompt.PromptInfo;
    private lateinit var executor: Executor

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        biometricManager = BiometricManager.from(this)
        executor = ContextCompat.getMainExecutor(this);
        checkbiometric(biometricManager);
        checkAuthentication();
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    fun checkbiometric(biometricmanager: BiometricManager) {
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                Log.d("biometricstatus", "succesfull");
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.d("biometricstatus", "currently unavailble");
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.d("biometricstatus", "no hardware");
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Log.d("biometricstatus", "user has not enrolled");
        }
    }

    fun checkAuthentication() {
        biometricPrompt =
            BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showToast("AuthenticationError" + errString);
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    gotoHomeActivity();
                    showToast("AuthenticationSuccess ");
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showToast("AuthenticationFsiled");
                }
            })
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    fun gotoHomeActivity() {
        val i = Intent(this, HomeActivity::class.java)
        startActivity(i)
    }


}
