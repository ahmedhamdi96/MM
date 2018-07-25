package com.moviemood.moviemood.services;

/**
 * Created by ahmed on 11/30/2017.
 */

public interface RequestTrigger {
    void onSuccessCall(String successMessage);
    void onFailureCall(String failureMessage);
    void saveUUID(String uuid);
}
