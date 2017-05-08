package uk.co.joshcorne.cardashboard.models;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

/**
 * Created by josh on 4/27/17.
 */

@Table
public class TroubleCode extends SugarRecord
{
    private String dtcKey;
    private String dtcMake;
    private String dtcValue;

    public TroubleCode(){}

    public TroubleCode(String dtcKey, String dtcMake)
    {
        this.dtcKey = dtcKey;
        this.dtcMake = dtcMake;
    }

    public String getDtcKey()
    {
        return dtcKey;
    }

    public String getDtcMake()
    {
        return dtcMake;
    }

    public String getDtcValue()
    {
        return dtcValue;
    }

    public void setDtcValue(String dtcValue)
    {
        this.dtcValue = dtcValue;
    }
}
