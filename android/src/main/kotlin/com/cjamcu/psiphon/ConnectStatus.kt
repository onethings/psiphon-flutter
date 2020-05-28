package com.cjamcu.psiphon


import com.google.gson.annotations.SerializedName


data class ConnectStatus(

	@field:SerializedName("connected")
	val connected: Boolean? = null,

	@field:SerializedName("port")
	val port: Int? = null
)
