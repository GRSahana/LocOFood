package com.SahanaProjects.loc_o_food;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;

import com.SahanaProjects.loc_o_food.Fragment.CompostFragment;
import com.SahanaProjects.loc_o_food.Fragment.PotFragment;
import com.SahanaProjects.loc_o_food.FragmentAdapter.ViewPagerAdapter;

public class HomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.flower);
        tabLayout.getTabAt(1).setIcon(R.drawable.fertillizer);

    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        PotFragment myObj = new PotFragment();
        adapter.addFragment(myObj, "Pot");
        CompostFragment myObj2 = new CompostFragment();
        adapter.addFragment(myObj2, "Compost");

        viewPager.setAdapter(adapter);
    }


}
