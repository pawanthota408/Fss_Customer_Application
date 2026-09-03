package com.fsscustomerapplication.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("company") val company: String? = null
)

data class LoginSlider(
    @SerializedName("id") val id: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?
)

data class LoginSliderResponse(
    @SerializedName("status") val status: String,
    @SerializedName("sliders") val sliders: List<LoginSlider>,
    @SerializedName("logo_url") val logoUrl: String? = null
)
