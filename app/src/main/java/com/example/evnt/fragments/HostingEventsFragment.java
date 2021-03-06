package com.example.evnt.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.evnt.EvntCardInfo;
import com.example.evnt.FragHostActivity;
import com.example.evnt.IdentProvider;
import com.example.evnt.R;
import com.example.evnt.networking.ServerRequestModule;
import com.example.evnt.networking.VolleyEventListCallback;
import com.example.evnt.adapters.EvntHostListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HostingEventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Context context;
    private ServerRequestModule mServerRequestModule;
    private List<EvntCardInfo> evntlist;
    private IdentProvider ident;
    private Fragment ctx;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * This is where we will be opening the saved state of the fragmend (if available)
     * and also passing in the serverrequestmodule to be able to fetch events from the server
     *
     * TODO need to add funcionality to save instances
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ident = new IdentProvider(getContext());
        ctx = this;

        mServerRequestModule = ServerRequestModule.getInstance();
        if (mServerRequestModule == null) {
            Toast.makeText(context, "serverProblem", Toast.LENGTH_LONG).show();
        }
        // TODO server call for user's hosted/ing events here
        evntlist = new ArrayList<>();
        loadList();
    }

    /**
     * THis is where we inflate the view and create the cards we need to store using the
     * recyclerview functionality.
     *
     * TODO need to see if changes needed here for search implementation.
     * TODO make GET api request to get list of events
     * @param savedInstanceState
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        // Fragment needs its root view before we can actually do stuff
        final View view = inflater.inflate(R.layout.fragment_hosting_events,
                container, false);

        SwipeRefreshLayout swipeView = view.findViewById(R.id.fragment_event_list);
        swipeView.setOnRefreshListener(this);

        FloatingActionButton create_event_button = view.findViewById(R.id.create_event);
        create_event_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                WebView wv = new WebView(getContext()) {
                    @Override
                    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
                        super.onFocusChanged(true, direction, previouslyFocusedRect);
                    }

                    @Override
                    public boolean onCheckIsTextEditor() {
                        return true;
                    }
                };

                HashMap<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                params.put("accessToken", ident.getValue(getString(R.string.access_token)));
                params.put("userId", ident.getValue(context.getString(R.string.user_id)));
                wv.getSettings().setJavaScriptEnabled(true);
                wv.loadUrl(getString(R.string.event_create), params);
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return false;
                    }
                });
                builder.setView(wv);
                builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        loadList();
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                dialog.show();
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.evnt_list_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        EvntHostListAdapter evntHostListAdapter = new EvntHostListAdapter(context, evntlist, mServerRequestModule);
        recyclerView.setAdapter(evntHostListAdapter);

        return view;
    }

    /**
     * so this is function should be used to reload the event list
     * in case of searches, by passing in a list of events with info
     *
     * TODO need to modify the params and use them to build arraylist
     */
    private void loadList() {

        // TODO need to filter non-hosting events
        evntlist.clear();
        mServerRequestModule.getEventsRequest(getString(R.string.event_get_in), new VolleyEventListCallback() {
            @Override
            public void onEventsListSuccessResponse(JSONArray data) {
                String you = ident.getValue(getString(R.string.user_id));
                try {
                    ((FragHostActivity) getActivity()).sortResponseToList(evntlist, data, you, true);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // TODO This is a hack to refresh the view, so we redraw the list
                if (getFragmentManager() != null) {
                    getFragmentManager().beginTransaction().detach(ctx).attach(ctx).commit();
                }
            }

            @Override
            public void onErrorResponse(String result) {
                Toast.makeText(context, "unable to load events currently", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onRefresh() {
        loadList();
    }
}
