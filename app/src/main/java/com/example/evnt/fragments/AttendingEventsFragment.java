package com.example.evnt.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.evnt.adapters.EvntListAdapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AttendingEventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

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
        context = getContext();
        ident = new IdentProvider(context);
        ctx = this;
        mServerRequestModule = ServerRequestModule.getInstance();
        if (mServerRequestModule == null) {
            Toast.makeText(context, "serverProblem", Toast.LENGTH_LONG).show();
        }

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
        final View view = inflater.inflate(R.layout.fragment_attending_events,
                container, false);

        SwipeRefreshLayout swipeView = view.findViewById(R.id.fragment_event_list);
        swipeView.setOnRefreshListener(this);

        RecyclerView recyclerView = view.findViewById(R.id.evnt_list_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        EvntListAdapter evntListAdapter = new EvntListAdapter(context, evntlist,
                                                        getString(R.string.attending),
                                                        getActivity().getSupportFragmentManager(),
                                                        mServerRequestModule);
        recyclerView.setAdapter(evntListAdapter);

        return view;
    }

    /**
     * so this is function should be used to reload the event list
     * in case of searches, by passing in a list of events with info
     *
     */
    private void loadList() {

        evntlist.clear();
        mServerRequestModule.getEventsRequest(getString(R.string.event_get_in), new VolleyEventListCallback() {
            @Override
            public void onEventsListSuccessResponse(JSONArray data) {
                String you = ident.getValue(getString(R.string.user_id));
                try {
                    ((FragHostActivity) getActivity()).sortResponseToList(evntlist, data, you, false);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
