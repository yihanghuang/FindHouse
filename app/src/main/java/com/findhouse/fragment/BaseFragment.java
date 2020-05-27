package com.findhouse.fragment;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;

import com.findhouse.utils.MyApplication;

public class BaseFragment extends Fragment {
    private Activity activity;

    public Context getContext() {
        if (activity == null) {
            return MyApplication.getInstance();
        }
        return activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

}
