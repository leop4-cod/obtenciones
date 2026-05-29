package senadi.gob.ec.adminob.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("timestampConverter")
public class TimestampConverter implements Converter {

    private static final String[] PATTERNS = {
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy HH:mm",
        "dd/MM/yy HH:mm",
        "dd/MM/yyyy",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    };

    @Override
    public Object getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        for (String p : PATTERNS) {
            try {
                Date d = new SimpleDateFormat(p).parse(value.trim());
                return new Timestamp(d.getTime());
            } catch (ParseException ignore) {
            }
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, Object value) {
        if (value == null) return "";
        Date d;
        if (value instanceof Date) d = (Date) value;
        else return value.toString();
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(d);
    }
}
