package com.example.shoppingtopping

import android.annotation.SuppressLint
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.example.shoppingtopping.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView

@SuppressLint("CheckResult")
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

//  Auth
        auth = FirebaseAuth.getInstance()


//  Fullname Validation
        val nameStream = RxTextView.textChanges(binding.etFullname)
            .skipInitialValue()
            .map { name->
                name.isEmpty()
            }
        nameStream.subscribe {
            showNameExistAlert(it)
        }

//  Email Validation
        val emailStream = RxTextView.textChanges(binding.etEmail)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
            showEmailValidAlert(it)
        }
//  Username Validation
        val usernameStream = RxTextView.textChanges(binding.etUsername)
            .skipInitialValue()
            .map { username ->
                 username.length < 6
            }
        usernameStream.subscribe {
            showTextMinimalAlert(it, "Username")
        }

//  Password Validation
        val passwordStream = RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { password ->
                password.length < 6
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it, "Password")
        }
//  Confirm Password Validation
        val passwordConfirmStream = io.reactivex.Observable.merge(
            RxTextView.textChanges(binding.etPassword)
                .skipInitialValue()
                .map { password ->
                    password.toString() != binding.etConfirmPassword.text.toString()
                },
            RxTextView.textChanges(binding.etConfirmPassword)
                .skipInitialValue()
                .map { confirmPassword ->
                    confirmPassword.toString() != binding.etConfirmPassword.text.toString()
                }
        )
        passwordConfirmStream.subscribe {
            showPasswordConfirmAlert(it)
        }

//  Button Enable True or False
        val invalidFieldStream = io.reactivex.Observable.combineLatest(
            nameStream,
            emailStream,
            usernameStream,
            passwordStream,
            passwordConfirmStream,
            { nameInvalid: Boolean, emailInvalid: Boolean, usernameInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmInvalid: Boolean ->
                !nameInvalid && !emailInvalid && !usernameInvalid && !passwordInvalid && !passwordConfirmInvalid
            })
        invalidFieldStream.subscribe { isValid ->
            if (isValid) {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
            } else {
                binding.btnRegister.isEnabled = false
                binding.btnRegister.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)

            }
        }


//  Click
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            registerUser(email, password)

        }
        binding.tvHaveAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showNameExistAlert(isNotValid: Boolean){
        binding.etFullname.error = if (isNotValid) "Name cannot be empty!" else null
    }
    private fun showTextMinimalAlert(isNotValid: Boolean, text: String){
        if(text == "Username")
            binding.etUsername.error = if (isNotValid) "$text must be more than 6 letters!" else null
        else if (text == "Password")
            binding.etPassword.error = if (isNotValid) "$text must be more than 8 letters!" else null

    }
    private fun showEmailValidAlert(isNotValid: Boolean){
        binding.etEmail.error = if (isNotValid) "Invalid Email" else null
    }
    private fun showPasswordConfirmAlert(isNotValid: Boolean){
        binding.etConfirmPassword.error = if (isNotValid) "Passwords are not the same" else null
    }

    private fun registerUser(email: String,password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    startActivity(Intent(this, LoginActivity::class.java))
                    Toast.makeText(this, "Register Succeed" , Toast.LENGTH_SHORT ).show()
                } else {
                    Toast.makeText(this, it.exception?.message , Toast.LENGTH_SHORT ).show()
                }
            }
    }
}