package com.color.mattdriver.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.color.mattdriver.Constants
import com.color.mattdriver.R



class SignIn : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    lateinit var listener: SignInInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is SignInInterface){
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_sign_in, container, false)
        val emailEditText: EditText = va.findViewById(R.id.emailEditText)
        val passwordEditText: EditText = va.findViewById(R.id.passwordEditText)
        val create_layout: RelativeLayout = va.findViewById(R.id.create_layout)
        val sign_up_instead: TextView = va.findViewById(R.id.sign_up_instead)

        sign_up_instead.setOnClickListener {
            listener.whenSignInSignUpInsteadSelected()
            Constants().touch_vibrate(context)
        }

        create_layout.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if(email.equals("")){
                emailEditText.setError(getString(R.string.we_need_your_email))
            }else if(password.equals("")){
                passwordEditText.setError(getString(R.string.we_need_a_password))
            }else{
                listener.whenSignInDetailsSubmitted(email,password)
            }
            Constants().touch_vibrate(context)
        }

        return va
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignIn().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    interface SignInInterface{
        fun whenSignInDetailsSubmitted(email: String, password: String)
        fun whenSignInSignUpInsteadSelected()
    }


}