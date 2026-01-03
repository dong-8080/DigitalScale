package com.bupt.myapplication.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentManagerHelper {
    
    public static void replaceFragment(FragmentManager fragmentManager, int containerId, Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(containerId, fragment);
        transaction.commitAllowingStateLoss();
    }

    public static void destroyFragment(FragmentManager fragmentManager, Fragment fragment) {
        if (fragment != null && !fragmentManager.isStateSaved()) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragment);
            transaction.commitAllowingStateLoss();
        }
    }
}

