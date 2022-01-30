package com.task.mobiadmin.retrofitManager;

import retrofit2.Call;

/**
 * Created by Neeraj Narwal on 31/5/17.
 */
public interface ResponseListener {
    void onSuccess(Call call, Object object);

    void onError(Call call, int statusCode, String errorMessage, ResponseListener responseListener);

/** Uncomment below method and its occurenece in {@link ApiManager}
 *  if you want a method which is sure to fire even request completes or fails
 */

//    void onFinish();

}
