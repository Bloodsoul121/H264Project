package com.blood.filter

import android.os.Bundle
import android.widget.RadioGroup
import com.blood.common.adapter.BindingCallback
import com.blood.common.base.BasePermissionActivity
import com.blood.filter.bean.FilterConfig
import com.blood.filter.databinding.ActivityMainBinding
import com.blood.filter.dialog.FilterDialog
import com.blood.filter.view.CameraView

class MainActivity : BasePermissionActivity(), BindingCallback<FilterConfig>, RadioGroup.OnCheckedChangeListener {

    private lateinit var binding: ActivityMainBinding

    private var isRecord = false
    private var isOutputH264 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        binding.rgSpeed.setOnCheckedChangeListener(this)

        binding.btnFilter.setOnClickListener {
            FilterDialog.newInstance(this).show(supportFragmentManager, "FilterDialog")
        }

        binding.btnH264.setOnClickListener {
            isOutputH264 = !isOutputH264
            binding.cameraView.toggleOutH264(isOutputH264)
            binding.btnH264.text = if (isOutputH264) "H264" else "MP4"
        }

        binding.btnRecord.setOnClickListener {
            isRecord = !isRecord
            if (isRecord) {
                binding.cameraView.startRecord()
            } else {
                binding.cameraView.stopRecord()
            }
            binding.btnRecord.text = if (isRecord) "停止录制" else "开始录制"
        }
    }

    override fun onItemClick(t: FilterConfig) {
        binding.cameraView.toggle(t.id)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.btn_extra_slow -> binding.cameraView.setSpeed(CameraView.Speed.MODE_EXTRA_SLOW)
            R.id.btn_slow -> binding.cameraView.setSpeed(CameraView.Speed.MODE_SLOW)
            R.id.btn_normal -> binding.cameraView.setSpeed(CameraView.Speed.MODE_NORMAL)
            R.id.btn_fast -> binding.cameraView.setSpeed(CameraView.Speed.MODE_FAST)
            R.id.btn_extra_fast -> binding.cameraView.setSpeed(CameraView.Speed.MODE_EXTRA_FAST)
        }
    }

}