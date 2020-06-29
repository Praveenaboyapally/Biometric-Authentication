package com.example.cryptography

import android.annotation.TargetApi
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
    private var encryptMode: Boolean = true;
    private lateinit var cipher: Cipher;
    private lateinit var decrypt: Cipher;
    private lateinit var biometricManager: BiometricManager;
    private lateinit var biometricPrompt: BiometricPrompt;
    private lateinit var promptInfo1: BiometricPrompt.PromptInfo;
    private lateinit var promptInfo2: BiometricPrompt.PromptInfo;
    private lateinit var editText: EditText
    private lateinit var encryptBtn: Button
    private lateinit var decryptBtn: Button
    private lateinit var encryptVal: ByteArray;
    private lateinit var executor: Executor

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        biometricManager = BiometricManager.from(this)
        executor = ContextCompat.getMainExecutor(this);

        checkbiometric(biometricManager);

        checkAuthentication()

        generateSecretKey(
            KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .setInvalidatedByBiometricEnrollment(true)
                .build()
        );

        promptInfo1 = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Encrypt the text")
            .setSubtitle("Encrypt using your biometric credential")
            .setNegativeButtonText("Use account password")
            .setConfirmationRequired(true)
            .build()
        promptInfo2 = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Decrypt the text")
            .setSubtitle("Decrypt using your biometric credential")
            .setNegativeButtonText("Use account password")
            .setConfirmationRequired(true)
            .build()
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
                    if (encryptMode) {
                        var enteredText: String = editText.text.toString()
                        encryptVal = result.cryptoObject!!.cipher!!.doFinal(
                            enteredText.toByteArray(Charset.defaultCharset())
                        );
                        editText.setText(encryptVal.toString())
                    } else {
                        editText.setText(String(decrypt.doFinal(encryptVal)));

                    }
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        return keyStore.getKey(KEY_NAME, pwd.toCharArray()) as SecretKey
    }

    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
        )
    }

    override fun onResume() {
        super.onResume()
        cipher = getCipher()
        decrypt = getCipher()
        secretKey = getSecretKey()
        init()
    }

    private fun init() {
        editText = findViewById(R.id.text);
        encryptBtn = findViewById(R.id.encrypt);
        decryptBtn = findViewById(R.id.decrypt);
        encryptBtn.setOnClickListener {
            encryptMode = true
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            biometricPrompt.authenticate(promptInfo1, BiometricPrompt.CryptoObject(cipher))
        }
        decryptBtn.setOnClickListener {
            encryptMode = false
            decrypt.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(cipher.iv))
            biometricPrompt.authenticate(promptInfo2, BiometricPrompt.CryptoObject(cipher))
        }
    }

}
