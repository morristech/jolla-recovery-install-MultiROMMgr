package com.tassadar.multirommgr;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.fima.cardsui.views.CardUI;


public class MainActivity extends Activity implements StatusAsyncTask.StatusAsyncTaskListener, StartInstallListener {

    public static final int ACT_INSTALL_MULTIROM = 1;
    public static final int ACT_INSTALL_UBUNTU   = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        mCardView = (CardUI) findViewById(R.id.cardsview);
        mCardView.setSwipeable(false);
        mCardView.setSlideIn(!StatusAsyncTask.instance().isComplete());

        start();
    }

    private void start() {
        mCardView.addCard(new StatusCard(), true);
        StatusAsyncTask.instance().setListener(this);
        StatusAsyncTask.instance().execute();
    }

    private void refresh() {
        StatusAsyncTask.destroy();
        UbuntuManifestAsyncTask.destroy();

        mCardView.clearCards();
        start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem it) {
        switch(it.getItemId()) {
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onTaskFinished(StatusAsyncTask.Result res) {
        if(res.manifest != null) {
            mCardView.addCard(new InstallCard(res.manifest, res.recovery == null, this), true);

            if(res.multirom != null && res.recovery != null && res.device.supportsUbuntuTouch())
                mCardView.addCard(new UbuntuCard(this, res.manifest, res.multirom, res.recovery));
        }
    }

    @Override
    public void startActivity(Bundle data, int id, Class<?> cls) {
        Intent i = new Intent(this, cls);
        i.putExtras(data);
        startActivityForResult(i, id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ACT_INSTALL_MULTIROM:
                if(resultCode == RESULT_OK)
                    refresh();
                break;
        }
    }

    private CardUI mCardView;
}