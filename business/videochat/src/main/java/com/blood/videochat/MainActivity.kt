package com.blood.videochat

import android.os.Bundle
import com.blood.common.base.BasePermissionActivity
import com.blood.common.helper.Camera1Helper
import com.blood.videochat.databinding.ActivityMainBinding
import com.blood.videochat.pull.PullLiveCodec
import com.blood.videochat.pull.PullSocketLive
import com.blood.common.helper.SurfaceViewHelper
import com.blood.videochat.push.PushLiveCodec
import com.blood.videochat.push.PushSocketLive
import com.blood.videochat.socket.SocketLive

class MainActivity : BasePermissionActivity() {

    companion object {
        const val SOCKET_PORT = 10000
    }

    private lateinit var binding: ActivityMainBinding
    private var socketLive: SocketLive? = null
    private var pushLiveCodec: PushLiveCodec? = null
    private var pullLiveCodec: PullLiveCodec? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun process() {
        resizeLocalView()

        binding.call.setOnClickListener {
            binding.call.isEnabled = false
            binding.receive.isEnabled = false
            socketLive = PushSocketLive(pullLiveCodec!!).apply { connect(SOCKET_PORT) }
            pushLiveCodec?.startLive(socketLive!!)
            pullLiveCodec?.startLive(socketLive!!)
        }
        binding.receive.setOnClickListener {
            binding.call.isEnabled = false
            binding.receive.isEnabled = false
            socketLive = PullSocketLive(pullLiveCodec!!).apply { connect(SOCKET_PORT) }
            pushLiveCodec?.startLive(socketLive!!)
            pullLiveCodec?.startLive(socketLive!!)
        }

        pushLiveCodec = PushLiveCodec()
        Camera1Helper(pushLiveCodec!!).init(binding.localeSurfaceView)

        pullLiveCodec = PullLiveCodec()
        SurfaceViewHelper(pullLiveCodec!!).init(binding.remoteSurfaceView)
    }

    private fun resizeLocalView() {
        val layoutParams = binding.localeSurfaceView.layoutParams
        layoutParams.width = binding.root.width * 2 / 5
        layoutParams.height = binding.root.height * 2 / 5
        binding.localeSurfaceView.layoutParams = layoutParams
    }

    override fun onDestroy() {
        super.onDestroy()
        socketLive?.close()
        pushLiveCodec?.stopLive()
        pullLiveCodec?.stopLive()
    }

}