package com.airmap.airmapsdktest;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.airmap.airmapsdk.models.airspace.AirMapAirspaceAdvisoryStatus;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.ui.activities.MyLocationMapActivity;
import com.airmap.airmapsdk.ui.views.CustomViewPager;
import com.airmap.airmapsdk.util.Utils;

import java.util.List;

/**
 * Created by collin@airmap.com on 9/8/17.
 */

public class MapDemoActivity extends MyLocationMapActivity {

    private static final String TAG = "MapDemoActivity";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private MapPagerAdapter pagerAdapter;
    private CustomViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
    }

    private void setupViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pagerAdapter = new MapPagerAdapter(getSupportFragmentManager());

        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPagingEnabled(false);
        viewPager.setAdapter(pagerAdapter);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    }

    public void setRulesets(List<AirMapRuleset> availableRulesets, List<AirMapRuleset> selectedRulesets) {
        getRulesetsFragment().setRulesets(availableRulesets, selectedRulesets);
    }

    public void selectRuleset(AirMapRuleset ruleset) {
        getMapFragment().onRulesetSelected(ruleset);
    }

    public void deselectRuleset(AirMapRuleset ruleset) {
        getMapFragment().onRulesetDeselected(ruleset);
    }

    public void switchSelectedRulesets(AirMapRuleset fromRuleset, AirMapRuleset toRuleset) {
        getMapFragment().onRulesetSwitched(fromRuleset, toRuleset);
    }

    public void setAdvisoryStatus(AirMapAirspaceAdvisoryStatus status) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof AdvisoriesFragment) {
                ((AdvisoriesFragment) fragment).setAdvisoryStatus(status);
            }
        }
    }

    private MapFragment getMapFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof MapFragment) {
                return (MapFragment) fragment;
            }
        }

        return null;
    }

    private RulesetsFragment getRulesetsFragment() {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof RulesetsFragment) {
                return (RulesetsFragment) fragment;
            }
        }

        return null;
    }

    private class MapPagerAdapter extends FragmentPagerAdapter {

        public MapPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MapFragment.newInstance();
                case 1:
                   return RulesetsFragment.newInstance();
                case 2:
                    return AdvisoriesFragment.newInstance();
            }

            return null;
        }

        @Override
        public String getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Map";
                case 1:
                    return "Rules";
                case 2:
                    return "Advisories";
            }

            return "";
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}