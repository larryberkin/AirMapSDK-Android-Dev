package com.airmap.airmapsdk.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.NoSuchPaddingException;

import devliving.online.securedpreferencestore.RecoveryHandler;
import devliving.online.securedpreferencestore.SecuredPreferenceStore;

public class PreferenceUtils {

    public static SharedPreferences getPreferences(Context context) throws SecuredPreferenceException {
        try {
            return SecuredPreferenceStore.getSharedInstance(context);
        } catch (IOException | CertificateException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | UnrecoverableEntryException | NoSuchProviderException | NoSuchPaddingException | KeyStoreException | RuntimeException e) {
            e.printStackTrace();

            throw new SecuredPreferenceException(e.getMessage());
        }
    }

    public static void setRecoveryHandler(RecoveryHandler recoveryHandler) {
        SecuredPreferenceStore.setRecoveryHandler(recoveryHandler);
    }
}
