package com.example.blegattclient.services.responses;

import com.example.blegattclient.services.pojo.Threshold;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RegisterVehicleResponse {

    @SerializedName("AssetId")
    public String assetId;

    @SerializedName("RegistrationStatus")
    public String registrationStatus;

    public List<Threshold> thresholds = new ArrayList<>();
}
