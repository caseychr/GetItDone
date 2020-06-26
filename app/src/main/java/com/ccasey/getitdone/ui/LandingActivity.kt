package com.ccasey.getitdone.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ccasey.getitdone.MainActivity
import com.ccasey.getitdone.R
import com.ccasey.getitdone.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.android.synthetic.main.layout_login.view.*

class LandingActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FirebaseUtil.init(this)
        init()
    }

    override fun onStart() {
        super.onStart()
        FirebaseUtil.addListener()
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.detachListener()
    }

    private fun init() {
        loginButtonOnClick()
        registerTextOnClick()
    }

    private fun performLogin() {
        if(!layoutLoginCredentials.emailEditText.checkEmpty() || !layoutLoginCredentials.passwordEditText.checkEmpty() ||
            !layoutLoginCredentials.emailEditText.checkEmailValid()) {
            Toast.makeText(this, getString(R.string.err_msg_valid_email), Toast.LENGTH_SHORT).show()
            return
        } else {
            FirebaseUtil.login(emailEditText.text.toString(), passwordEditText.text.toString(), {loadMainActivity()})
        }
    }

    private fun performRegister() {
        if(!layoutLoginCredentials.emailEditText.checkEmpty() || !layoutLoginCredentials.passwordEditText.checkEmpty() ||
            !layoutLoginCredentials.emailEditText.checkEmailValid()) {
            Toast.makeText(this, getString(R.string.err_msg_valid_email), Toast.LENGTH_SHORT).show()
            return
        } /*else if(!layoutLoginCredentials.passwordEditText.checkConfirmPassword(layoutLoginCredentials.passwordConfirmEditText.toString())) {
            println("PSWD: ${layoutLoginCredentials.passwordEditText.text}, ${layoutLoginCredentials.passwordConfirmEditText.text}," +
                    " ${layoutLoginCredentials.passwordEditText.checkConfirmPassword(layoutLoginCredentials.passwordConfirmEditText.toString())}")
            Toast.makeText(this, getString(R.string.err_msg_pswd_mismatch), Toast.LENGTH_SHORT).show()
        }*/ else {
            FirebaseUtil.registerEmail(layoutLoginCredentials.emailEditText.text.toString(), layoutLoginCredentials.passwordEditText.text.toString()
            , { setLoginUI() })
        }
    }

    private fun registerTextOnClick() {
        registerLink.apply {
            this.setOnClickListener {
                if(text.toString().equals(getString(R.string.register))) {
                    text = getString(R.string.login)
                    loginBtn.text = getString(R.string.register)
                    layoutLoginCredentials.passwordConfirmEditText.visibility = View.VISIBLE
                } else {
                    setLoginUI()
                }
            }
        }
    }

    private fun setLoginUI() {
        registerLink.text = getString(R.string.register)
        loginBtn.text = getString(R.string.login)
        layoutLoginCredentials.passwordConfirmEditText.visibility = View.GONE
    }

    private fun loginButtonOnClick() {
        loginBtn.setOnClickListener {
            if(loginBtn.text.equals(getString(R.string.register))) {
                performRegister()
            } else {
                performLogin()
            }
        }
    }

    private fun loadMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

fun EditText.checkEmpty(): Boolean {
    return text.isNotEmpty()
}

fun EditText.checkEmailValid(): Boolean {
    return (text.contains("@") && text.contains(context.getString(R.string.suffix_com)))
}

fun EditText.checkConfirmPassword(password: String): Boolean {
    return text.toString().toCharArray().contentEquals(password.toCharArray())
}