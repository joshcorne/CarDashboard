package uk.co.joshcorne.cardashboard;

import com.orm.SugarApp;
import com.orm.SugarContext;

/**
 * Created by josh on 5/7/17.
 */

public class App extends SugarApp
{
    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);
    }

    @Override
    public void onTerminate() {
        SugarContext.terminate();
        super.onTerminate();
    }
}