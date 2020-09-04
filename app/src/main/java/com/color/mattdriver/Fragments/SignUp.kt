package com.color.mattdriver.Fragments

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.color.mattdriver.Constants
import com.color.mattdriver.Models.number
import com.color.mattdriver.R
import com.hbb20.CountryCodePicker
import java.util.*
import kotlin.collections.ArrayList


class SignUp : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    lateinit var listener: SignUpInterface
    private val passwordLength = 8
    private val easyPasswords: List<String> = ArrayList(
        Arrays.asList(
            "12345678",
            "98765432",
            "qwertyui",
            "asdfghjk",
            "zxcvbnm1",
            "123456ab",
            "123456qw",
            "987654qw",
            "987654as",
            ""
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is SignUpInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_sign_up, container, false)
        val ccp: CountryCodePicker = va.findViewById(R.id.ccp)
        val phoneEditText: EditText = va.findViewById(R.id.phoneEditText)
        val nameEditText: EditText = va.findViewById(R.id.nameEditText)
        val emailEditText: EditText = va.findViewById(R.id.emailEditText)
        val passwordEditText: EditText = va.findViewById(R.id.passwordEditText)
        val confirmPasswordEditText: EditText = va.findViewById(R.id.confirmPasswordEditText)
        val strength_progress_bar: ProgressBar = va.findViewById(R.id.strength_progress_bar)
        val confirm_progress_bar: ProgressBar = va.findViewById(R.id.confirm_progress_bar)
        val strength_expalainer: TextView = va.findViewById(R.id.strength_expalainer)
        val create_layout: RelativeLayout = va.findViewById(R.id.create_layout)
        val sign_in_instead: TextView = va.findViewById(R.id.sign_in_instead)

        val progressDrawable: Drawable = confirm_progress_bar.getProgressDrawable().mutate()

        if(Constants().SharedPreferenceManager(context!!).isDarkModeOn()){
            progressDrawable.setColorFilter(Color.DKGRAY, PorterDuff.Mode.SRC_IN)
            confirm_progress_bar.setProgressDrawable(progressDrawable)
        }else{
            progressDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            confirm_progress_bar.setProgressDrawable(progressDrawable)
        }


        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val confirm: String = p0.toString()
                val currentLength: Int = confirm.length

                val typedPassword: String = passwordEditText.text.toString().trim()
                val maxLength: Int = passwordEditText.text.toString().trim().length

                if(maxLength != 0 && currentLength != 0){
                    confirm_progress_bar.visibility = View.VISIBLE

                    var correctionLength = 0

                    if(maxLength>=currentLength) {
                        val loopLn = currentLength-1
                        for (i in 0..loopLn) {
                            if (confirm.get(i).equals(typedPassword.get(i))) {
                                correctionLength++
                            }else{
                                break
                            }
                        }
                    }
                    Log.e("signup","correction: "+correctionLength)
                    val percent: Double = (correctionLength.toDouble() / maxLength.toDouble())*100

                    val anim = ProgressBarAnimation(confirm_progress_bar, confirm_progress_bar.progress.toFloat(), percent.toFloat())
                    anim.duration = 400
                    anim.interpolator = LinearOutSlowInInterpolator()
                    confirm_progress_bar.startAnimation(anim)

                    if(confirm.equals(typedPassword)){
                        Constants().touch_vibrate(context)
                        strength_expalainer.setText(getString(R.string.perfect))
                    }else{
                        strength_expalainer.setText(getString(R.string.almost))
                    }
                }else{
                    confirm_progress_bar.visibility = View.INVISIBLE
                }
            }

        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val length: Int = p0!!.length

                if(length == 0){
                    strength_progress_bar.visibility = View.INVISIBLE
                    strength_expalainer.visibility = View.INVISIBLE
                }else{
                    strength_progress_bar.visibility = View.VISIBLE
                    strength_expalainer.visibility = View.VISIBLE
                }

                val percent: Double = (length.toDouble() / passwordLength.toDouble())*100

                val anim = ProgressBarAnimation(strength_progress_bar, strength_progress_bar.progress.toFloat(), percent.toFloat())
                anim.duration = 400
                anim.interpolator = LinearOutSlowInInterpolator()
                strength_progress_bar.startAnimation(anim)

                if(length == 0){
                    strength_expalainer.text = getString(R.string.type_something)
                }else if(length == 1){
                    strength_expalainer.text = getString(R.string.very_weak)
                }else if(length == 2){
                    strength_expalainer.text = getString(R.string.a_bit_more)
                }else if(length == 3){
                    strength_expalainer.text = getString(R.string.a_bit_more)
                }else if(length == 4){
                    strength_expalainer.text = getString(R.string.a_bit_more)
                }else if(length == 5){
                    strength_expalainer.text = getString(R.string.a_bit_more)
                }else if(length == 6){
                    strength_expalainer.text = getString(R.string.a_bit_more)
                }else if(length == 7){
                    strength_expalainer.text = getString(R.string.a_bit_more)
                }else if(length == 8){
                    strength_expalainer.text = getString(R.string.thats_good)
                    Constants().touch_vibrate(context)
                }else{
                    strength_expalainer.text = getString(R.string.thats_good)
                }

            }

        })

        create_layout.setOnClickListener {
            Constants().touch_vibrate(context)
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirm_password = confirmPasswordEditText.text.toString().trim()
            val typed_name = nameEditText.text.toString().trim()
            val phoneNo = phoneEditText.text.toString().trim()


            if(isValidEmail(email, emailEditText) && isValidPassword(password, confirm_password, passwordEditText, confirmPasswordEditText)){
                if(typed_name.equals("")){
                    nameEditText.setError("Type Something!")
                }else{
                    if(phoneNo.equals("")){
                        phoneEditText.setError(getString(R.string.please_fill_this))
                    }else {
                        val the_number = number(
                            phoneNo.replace(" ".toRegex(), "").toLong(),
                            ccp.selectedCountryCodeWithPlus,
                            ccp.selectedCountryName,
                            ccp.selectedCountryNameCode
                        )
                        listener.whenSignUpDetailsSubmitted(email,password,typed_name,the_number)

                    }
                }
            }

        }

        sign_in_instead.setOnClickListener {
            Constants().touch_vibrate(context)
            listener.whenSignInInstead()
        }

        return va
    }

    private fun isValidPassword(
        password: String,
        confirmPassword: String,
        mPasswordEditText: EditText,
        mConfirmPasswordEditText: EditText
    ): Boolean {
        if (password == "") {
            mPasswordEditText.setError(getString(R.string.we_need_a_password))
            return false
        }else if(confirmPassword == ""){
            mConfirmPasswordEditText.setError(getString(R.string.confirm_your_password))
        } else if (password.length < passwordLength) {
            mPasswordEditText.setError(getString(R.string.at_least_6_characters))
            return false
        } else if (password != confirmPassword) {
            mPasswordEditText.setError(getString(R.string.passwords_dont_match))
            return false
        } else if (easyPasswords.contains(password)) {
            mPasswordEditText.setError(getString(R.string.use_strong_password))
            return false
        }
        return true
    }

    private fun isValidEmail(
        email: String,
        mEmailEditText: EditText
    ): Boolean {
        if (email == "") {
            mEmailEditText.setError(getString(R.string.we_need_your_email))
            return false
        }
        val isGoodEmail =
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (!email.contains("@")) {
            mEmailEditText.setError(getString(R.string.thats_not_your_email))
            return false
        }
        var counter = 0
        for (i in 0 until email.length) {
            if (email[i] == '.') {
                counter++
            }
        }
        if (counter != 1 && counter != 2 && counter != 3) {
            mEmailEditText.setError(getString(R.string.we_need_your_actual_email_address))
            return false
        }
        var counter2 = 0
        var continueIncrement = true
        for (i in 0 until email.length) {
            if (email[i] == '@') {
                continueIncrement = false
            }
            if (continueIncrement) counter2++
        }
        if (counter2 <= 3) {
            mEmailEditText.setError(getString(R.string.thats_not_a_real_email_adress))
            return false
        }
        if (!isGoodEmail) {
            mEmailEditText.setError(getString(R.string.we_need_your_actual_email_address_please))
            return false
        }
        return isGoodEmail
    }



    class ProgressBarAnimation(
        private val progressBar: ProgressBar,
        private val from: Float,
        private val to: Float
    ) : Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation?
        ) {
            super.applyTransformation(interpolatedTime, t)
            val value = from + (to - from) * interpolatedTime
            progressBar.progress = value.toInt()
        }

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUp().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    interface SignUpInterface{
        fun whenSignUpDetailsSubmitted(email: String, password: String, name: String, numbr: number)
        fun whenSignInInstead()
    }
}