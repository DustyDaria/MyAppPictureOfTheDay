package com.example.myapppictureoftheday.ui.main

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import coil.api.load
import com.example.myapppictureoftheday.R
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        val observer = Observer<PictureOfTheDayData>{ renderData(it)}
        viewModel.getData().observe(viewLifecycleOwner, observer)
    }

    private fun renderData(data: PictureOfTheDayData){
        when (data){
            is PictureOfTheDayData.Success -> {
                val serverResponse : ServerResponse = data.serverResponse
                message.text = serverResponse.explanation
                image_view.load(serverResponse.url) {
                    lifecycle(this@MainFragment)
                    //Метод lifecycle() позволяет отключать загрузку, если приложение свернули/закрыли (автоматическая отмена)
                    error(R.drawable.ic_load_error)
                    placeholder(R.drawable.ic_no_photo)
                }
            }
            is PictureOfTheDayData.Error -> {
                //Error
            }
            is PictureOfTheDayData.Loading -> {
                //Loading
            }
        }

    }

    companion object {
        fun newInstance() = MainFragment()
    }

}
