package com.oguzdev.trendinghacker.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.oguzdev.trendinghacker.R;
import com.oguzdev.trendinghacker.bg.UpdateService;
import com.oguzdev.trendinghacker.client.HNClient;
import com.oguzdev.trendinghacker.client.ReadLaterClient;
import com.oguzdev.trendinghacker.common.model.NewsItem;
import com.oguzdev.trendinghacker.common.model.UpdatePrefs;
import com.oguzdev.trendinghacker.util.AlarmUtils;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SetupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetupFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private View rootView;
    private UpdatePrefs prefs;

    public static SetupFragment newInstance(String param1, String param2) {
        SetupFragment fragment = new SetupFragment();
        return fragment;
    }

    public SetupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_setup, container, false);

        Button serviceToggleButton = (Button) rootView.findViewById(R.id.button_service_toggle);
        serviceToggleButton.setOnClickListener(this);
        updateServiceToggleButton(serviceToggleButton, prefs.enabled, prefs.enabled);

        rootView.findViewById(R.id.read_later_login_button).setOnClickListener(this);

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        prefs = UpdatePrefs.getUpdatePrefs(activity);
        if(prefs == null) {
            prefs = new UpdatePrefs();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_service_toggle) {
            updateServiceToggleButton((Button) v, prefs.enabled, !prefs.enabled);
            toggleService();
        }
        else if(v.getId() == R.id.read_later_login_button) {
            saveReadLaterCredentials();
        }
    }

    public void toggleService() {
        prefs.enabled = !prefs.enabled;
        if(getActivity() != null) {
            Context context = getActivity();
            if (prefs.enabled) {
                AlarmUtils.setupHourlyAlarm(context);
                context.startService(new Intent(context, UpdateService.class));
            } else {
                AlarmUtils.cancelHourlyAlarm(context);
                prefs.recentlyDisplayedItems = new NewsItem[0];
            }
            prefs.storeUpdatePrefs(context);
        }
    }

    public void updateServiceToggleButton(Button button, boolean oldStateIsEnabled,
                                          boolean newStateIsEnabled) {
        int disableColor = getResources().getColor(R.color.button_disable);
        int enableColor = getResources().getColor(R.color.button_enable);
        int oldColor, newColor;
        if(oldStateIsEnabled) {
            button.setTextColor(disableColor);
            oldColor = disableColor;
        }
        else {
            button.setTextColor(enableColor);
            oldColor = enableColor;
        }
        if(newStateIsEnabled) {
            button.setText(getString(R.string.button_service_disable));
            newColor = disableColor;
        }
        else {
            button.setText(getString(R.string.button_service_enable));
            newColor = enableColor;
        }

        ValueAnimator colorAnim = ObjectAnimator.ofInt(button, "textColor", oldColor, newColor);
        colorAnim.setDuration(200);
        colorAnim.setEvaluator(new ArgbEvaluator());
        colorAnim.start();
    }

    public void saveReadLaterCredentials() {
        EditText usernameInput = (EditText) rootView.findViewById(R.id.read_later_username_input);
        EditText passwordInput = (EditText) rootView.findViewById(R.id.read_later_password_input);

        ReadLaterClient.Credentials credentials = new ReadLaterClient.Credentials(
                                                        usernameInput.getText().toString(),
                                                        passwordInput.getText().toString());
        ReadLaterClient.storeCredentials(credentials, getActivity());

        usernameInput.setText("");
        passwordInput.setText("");

        Toast.makeText(getActivity(), R.string.read_later_credentials_saved,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
