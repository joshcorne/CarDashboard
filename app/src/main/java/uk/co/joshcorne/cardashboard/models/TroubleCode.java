package uk.co.joshcorne.cardashboard.models;

import com.orm.SugarRecord;
import com.orm.dsl.Table;
import com.orm.query.Select;

import java.util.List;

/**
 * Created by josh on 4/27/17.
 */

@Table
public class TroubleCode extends SugarRecord
{
    private String code;
    private String oem;
    private String description;

    public TroubleCode(){}

    public TroubleCode(String code, String oem)
    {
        this.code = code;
        this.oem = oem;
    }

    public String getCode()
    {
        return code;
    }

    public String getOem()
    {
        return oem;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
}
