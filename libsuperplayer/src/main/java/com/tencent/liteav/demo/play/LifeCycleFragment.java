package com.tencent.liteav.demo.play;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Description:     LifeCycleFragment
 * Author:         刘帅
 * CreateDate:     2022/1/14
 */
public class LifeCycleFragment extends Fragment {

    private final Set<CallBack> callBacks = new HashSet<>();

    public LifeCycleFragment() {
        super();
    }

    public static LifeCycleFragment attach(final @NonNull Activity activity) {
        final FragmentManager fragmentManager = activity.getFragmentManager();
        final String tag = activity.getComponentName().toShortString();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new LifeCycleFragment();
            fragmentManager.beginTransaction().add(fragment, tag).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        LifeCycleFragment lifeCycleFragment;
        if (fragment instanceof LifeCycleFragment) {
            lifeCycleFragment = (LifeCycleFragment) fragment;
        } else {
            throw new IllegalStateException("A class " + fragment.getClass().getName() + " with tag '" + tag + "' already exists.");
        }
        return lifeCycleFragment;
    }

    public void addCallBack(CallBack callBack) {
        callBacks.add(callBack);
    }

    public void removeCallBack(CallBack callBack) {
        callBacks.remove(callBack);
        if (callBacks.isEmpty()){
            removeFragment();
        }
    }

    public void clearCallBack() {
        callBacks.clear();
        removeFragment();
    }

    private void removeFragment() {
        final FragmentManager fragmentManager = getActivity().getFragmentManager();
        fragmentManager.beginTransaction().remove(this).commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    public interface CallBack {

        default void onCreate(Bundle savedInstanceState) {
        }

        default void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        }

        default void onStart() {
        }

        default void onResume() {
        }

        default void onPause() {
        }

        default void onStop() {
        }

        default void onDestroyView() {
        }

        default void onDestroy() {
        }

        default void onSaveInstanceState(final Bundle outState) {
        }

        default void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        }

        default void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onCreate(savedInstanceState);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onActivityCreated(savedInstanceState);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onStart();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onPause();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onDestroyView();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onDestroy();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onStop();
            }
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onSaveInstanceState(outState);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!callBacks.isEmpty()) {
            for (CallBack callBack : callBacks) {
                callBack.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}