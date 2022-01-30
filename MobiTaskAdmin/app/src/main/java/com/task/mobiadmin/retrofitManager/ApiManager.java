package com.task.mobiadmin.retrofitManager;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.JsonObject;
import com.google.gson.stream.MalformedJsonException;
import com.task.mobiadmin.utils.Const;
import com.task.mobiadmin.utils.Utils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Neeraj Narwal on 31/5/17.
 */
@Singleton
public class ApiManager implements Callback {

    static final String TAG = ApiManager.class.getSimpleName();
    private ProgressDialog progressDialog;
    private HashMap<Call, ResponseListener> apiResponseHashMap = new HashMap<>();
    private Context context;

    @Inject
    ApiManager(Context context) {
        this.context = context;
    }

    public void instantiateProgressDialog(Activity activity) {
        progressDialog = new ProgressDialog(activity);
    }

    //Constructor when Progress message need to update
    public void makeApiCall(Call call, String progressMessage, ResponseListener responseListener) {
        if (progressMessage != null && !progressMessage.isEmpty()) {
            progressDialog.updateMessage(progressMessage);
        }
        makeApiCall(call, true, responseListener);
    }

    public void makeApiCall(Call call, ResponseListener responseListener) {
        makeApiCall(call, true, responseListener);
    }

    public void makeApiCall(Call call, boolean showProgress, ResponseListener responseListener) {
        if (checkIfHasNetwork()) {
            try {
                apiResponseHashMap.put(call, responseListener);
                call.enqueue(this);
                if (showProgress) {
                    progressDialog.startProgressDialog();
                }

                //Logs post URL
                log(call.request().url() + "");

                //Logs post params of Multipart request
                log("Post Params >>>> \n" + bodyToString(call.request().body())
                        .replace("\r", "")
                        .replaceAll("--+[a-zA-Z0-9-/:=; ]+\\n", "")
                        .replaceAll("Content+[a-zA-Z0-9-/:=; ]+;\\s", "")
                        .replaceAll("Content+[a-zA-Z0-9-/:=; ]+\\n", "")
                        .replaceAll("charset+[a-zA-Z0-9-/:=; ]+\\n", "")
                        .replace("name=", "")
                        .replace("\n\n", "--> ")
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            responseListener.onError(call, Const.NO_NETWORK, "No network available", responseListener);
        }
    }

    private void log(String string) {
        Utils.debugLog(TAG, string);
    }

    private String bodyToString(final RequestBody request) {
        try {
            Buffer buffer = new Buffer();
            if (request != null)
                request.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    @Override
    public void onResponse(Call call, Response response) {
        progressDialog.stopProgressDialog();
        ResponseListener responseListener = apiResponseHashMap.get(call);
//        try {
//            if (response.isSuccessful()) {
//                Object o = response.body();
//                responseListener.onSuccess(call, o);
//            } else {
//                JsonParser parser = new JsonParser();
//                JsonElement mJson = parser.parse(response.errorBody().string());
//                Gson gson = new Gson();
//                ErrorData errorResponse = gson.fromJson(mJson, ErrorData.class);
//                log(errorResponse.message + "");
//                responseListener.onError(call, response.code(), errorResponse.message, responseListener);
//            }
//        } catch (Exception ignored) {
//        }

        //This handling is to handle a "success" object in response which tell its an error or not like OK/NOK
        //Do update this method if your handling is of different type if not then send response as it is
        JsonObject object = (JsonObject) response.body();
        try {
            log(object + "");
            /*
              Check if response is of type 'success' key
              else respose is some general response for some else API, send that response as it is
            */
            if (object != null && object.has("success")) {
                if (object.get("success").getAsString().equals("1")) {
                    responseListener.onSuccess(call, response.body());
                } else {
                    responseListener.onError(call, object.get("success").getAsInt(), object.has("message") ? object.get("message").getAsString() : " ", responseListener);
                }
            } else if (response.isSuccessful()) {
                responseListener.onSuccess(call, response.body());
            } else {
                responseListener.onError(call, response.code(), response.message(), responseListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            apiResponseHashMap.remove(call);
        }
    }

    @Override
    public void onFailure(Call call, Throwable t) {
        progressDialog.stopProgressDialog();
        ResponseListener responseListener = apiResponseHashMap.get(call);
        try {
            if (t.getClass() == SocketTimeoutException.class) {
                responseListener.onError(call, Const.SOCKET_TIMEOUT, "socketTimeout", responseListener);
            } else if (t.getClass() == MalformedJsonException.class) {
                responseListener.onError(call, 500, "Internal server error", responseListener);
            } else {
                responseListener.onError(call, 500, t.getMessage(), responseListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            apiResponseHashMap.remove(call);
        }
//        responseListener.onFinish();
    }

    private boolean checkIfHasNetwork() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
