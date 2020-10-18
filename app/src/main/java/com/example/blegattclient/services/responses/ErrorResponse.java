package com.example.blegattclient.services.responses;

import com.google.gson.annotations.SerializedName;

public class ErrorResponse {
    @SerializedName("Message")
    public String message;

    @SerializedName("ExceptionType")
    public String exceptionType;
    /*
    {
  "Message": "An exception occured when invoking the operation - Cannot create a record in Assets (EntAssetObjectTable). Asset: AP20TS3042, MarutiABC.\nThe record already exists.",
  "ExceptionType": "DuplicateKeyException",
  "ActivityId": "671c83eb-9ccf-0002-f091-1d67cf9cd601"
}
    */

    public static ErrorResponse UnknownError()
    {
        ErrorResponse response = new ErrorResponse();
        response.exceptionType = "UnknownException";
        response.message = "Unknown error";
        return response;
    }

    public static ErrorResponse ParseError()
    {
        ErrorResponse response = new ErrorResponse();
        response.exceptionType = "ParseException";
        response.message = "Parse error";
        return response;
    }
}
