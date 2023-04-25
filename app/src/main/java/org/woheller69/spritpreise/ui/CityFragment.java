package org.woheller69.spritpreise.ui;

import static org.woheller69.spritpreise.ui.RecycleList.CityAdapter.DETAILS;
import static org.woheller69.spritpreise.ui.RecycleList.CityAdapter.OVERVIEW;
import static org.woheller69.spritpreise.ui.RecycleList.CityAdapter.STATIONS;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import org.woheller69.spritpreise.R;
import org.woheller69.spritpreise.activities.CityGasPricesActivity;
import org.woheller69.spritpreise.database.Station;
import org.woheller69.spritpreise.ui.RecycleList.CityAdapter;
import org.woheller69.spritpreise.ui.RecycleList.OnSwipeDownListener;
import org.woheller69.spritpreise.ui.updater.IUpdateableCityUI;
import org.woheller69.spritpreise.ui.updater.ViewUpdater;
import org.woheller69.spritpreise.ui.viewPager.CityPagerAdapter;

import java.util.List;

//Fragment with the viewholders for a location
public class CityFragment extends Fragment implements IUpdateableCityUI {

    private int mCityId = -1;
    private static final int[] mDataSetTypes = {STATIONS}; //Before: {OVERVIEW, DETAILS, STATIONS} OVERVIEW and DETAILS unused at the moment.

    private CityAdapter mAdapter;

    private RecyclerView recyclerView;

    public static CityFragment newInstance(Bundle args)
    {
        CityFragment cityFragment = new CityFragment();
        cityFragment.setArguments(args);
        return cityFragment;
    }

    public void setAdapter(CityAdapter adapter) {
        mAdapter = adapter;

        if (recyclerView != null) {
            recyclerView.setAdapter(mAdapter);
            recyclerView.setFocusable(false);
        }
    }

    public void loadData() {

                mAdapter = new CityAdapter(mCityId, mDataSetTypes, getContext());
                setAdapter(mAdapter);
            }


    @Override
    public void onResume() {
        loadData();
        super.onResume();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        ViewUpdater.addSubscriber(this);
    }

    @Override
    public void onDetach() {
        ViewUpdater.removeSubscriber(this);

        super.onDetach();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.city_fragment, container, false);

        recyclerView = v.findViewById(R.id.CityRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()){
            public boolean canScrollVertically() {    //Make parent recyclerview not scrollable (not needed in this app) and scroll stations instead
                return false;
            }
        });


        Bundle args = getArguments();
        mCityId = args.getInt("city_id");

        return v;
    }

    @Override
    public void processUpdateStations(List<Station> stations, int cityID) {

            if (mAdapter != null && mCityId==cityID) {
                mAdapter.updateStationsData(stations);
            }
    }

}
