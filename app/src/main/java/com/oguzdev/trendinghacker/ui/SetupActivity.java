package com.oguzdev.trendinghacker.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.oguzdev.trendinghacker.R;
import com.oguzdev.trendinghacker.bg.HNClient;
import com.oguzdev.trendinghacker.model.NewsItem;
import com.oguzdev.trendinghacker.util.MeasureUtils;


public class SetupActivity extends Activity implements SetupFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SetupFragment())
                    .commit();
        }

        if(getActionBar() != null) {
            getActionBar().setElevation(MeasureUtils.pxFromDp(2));
        }

        HNClient hn = new HNClient(this, new HNClient.RetrieveTrendingListener() {
            @Override
            public void onRetrieve(NewsItem[] items) {
                if(items != null) {
                    Log.d("oguz", "retrieved news items with length "+items.length);
                    for(int i=0;i<items.length;i++) {
                        if(items[i] != null) {
                            Log.i("oguz", items[i].toString());
                        }
                        else {
                            Log.d("oguz", "items["+i+"] is null");
                        }
                    }
                }
            }
        });
//        hn.beginRetrieveTrending();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
