package com.example.mechanicfinder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.mechanicfinder.utils.Name
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser


    private lateinit var etNameSignup: TextInputLayout
    private lateinit var etEmailIdSignup: TextInputLayout
    private lateinit var etPasswordSignup: TextInputLayout
    private lateinit var etRePasswordSignup: TextInputLayout
    private lateinit var btnSignup: MaterialButton
    private lateinit var textviewReferLogin: MaterialTextView
    private lateinit var database:FirebaseDatabase
    private lateinit var ref:DatabaseReference
    private val TAG = "SignupActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        auth.currentUser.also {
            if (it != null) {
                user = it
                startActivity(Intent(this,MechanicMapActivity::class.java))
            }
        }

        etNameSignup = findViewById(R.id.et_name_signup)
        etEmailIdSignup = findViewById(R.id.et_email_id_signup)
        etPasswordSignup = findViewById(R.id.et_password_signup)
        etRePasswordSignup = findViewById(R.id.et_re_password_signup)
        btnSignup = findViewById(R.id.btn_signup)
        textviewReferLogin = findViewById(R.id.textview_refer_login)
        btnSignup.setOnClickListener {
            signUpUser()
        }

        textviewReferLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signUpUser() {

        if (etNameSignup.editText?.text.toString().isEmpty()) {
            etNameSignup.error = "Please enter your Name"
            etNameSignup.requestFocus()
            return
        }

        if (etEmailIdSignup.editText?.text.toString().isEmpty()) {
            etEmailIdSignup.error = "Please enter your Email id"
            etEmailIdSignup.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(etEmailIdSignup.editText?.text.toString()).matches()) {
            etEmailIdSignup.error = "Please enter Valid Email"
            etEmailIdSignup.requestFocus()
            return
        }

        if (etPasswordSignup.editText?.text.toString().isEmpty()) {
            etPasswordSignup.error = "Please enter your Password"
            etPasswordSignup.requestFocus()
            return
        }

        if (etPasswordSignup.editText?.text.toString().length < 6) {
            etPasswordSignup.error = "Password is too short\nMust be length between 6-12."
            etPasswordSignup.requestFocus()
            return
        }

        if (etPasswordSignup.editText?.text.toString().length > 12) {
            etPasswordSignup.error = "Password is much long\nMust be length between 6-12."
            etPasswordSignup.requestFocus()
            return
        }

        if (etRePasswordSignup.editText?.text.toString().isEmpty()) {
            etRePasswordSignup.error = "Please re-enter your Password"
            etRePasswordSignup.requestFocus()
            return
        }

        if (etPasswordSignup.editText?.text.toString() != etRePasswordSignup.editText?.text.toString()) {
            etRePasswordSignup.error = "Password does not Match."
            etRePasswordSignup.requestFocus()
            return
        }

        if (identicalPassword() && notEmpty()) {

            auth.createUserWithEmailAndPassword(
                etEmailIdSignup.editText?.text.toString(),
                etPasswordSignup.editText?.text.toString()
            )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        user = auth.currentUser!!
                        database= FirebaseDatabase.getInstance()
                        ref=database.reference
                        ref.child("mechanic").child(user.uid).setValue(User(etNameSignup.editText?.text.toString(),etEmailIdSignup.editText?.text.toString(),etPasswordSignup.editText?.text.toString(),0.011.toString(),0.22.toString())).addOnCompleteListener {
                            if(it.isSuccessful)
                            {
                                Toast.makeText(this, "SignUp Successfully!", Toast.LENGTH_SHORT).show()
                                sendEmailVerification()

                                val intent = Intent(this, LoginActivity::class.java)
                                intent.putExtra(Name, etNameSignup.editText?.text.toString())
                                startActivity(intent)
                            }
                           else{
                               Toast.makeText(this, "SignUp Failed Try again!"+it.exception, Toast.LENGTH_SHORT)
                            }
                        }


                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            this, "Authentication failed.", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {

        }
    }




    private fun identicalPassword(): Boolean {
        return etPasswordSignup.editText?.text.toString() == etRePasswordSignup.editText?.text.toString()
    }


    private fun notEmpty(): Boolean {
        return etNameSignup.editText?.text.toString().isNotEmpty() &&
                etEmailIdSignup.editText?.text.toString().isNotEmpty() &&
                etPasswordSignup.editText?.text.toString().isNotEmpty() &&
                etRePasswordSignup.editText?.text.toString().isNotEmpty()
    }

    private fun sendEmailVerification() {
        user.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Email sent to ${etEmailIdSignup.editText?.text.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}