package org.jenkinsci.plugins.vssj;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Calendar utils.
 * @author patlau
 *
 */
public final class CalendarUtils {
	
	private CalendarUtils() {	
	}

	/**
	 * Get midnight calendar instance.
	 */
    public static Calendar midnight() {
        final Calendar midnight = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        return midnight;
    }
    
    public static Calendar getDatetime(String dateString, String dateFormatString, String timeString, String timeFormatString) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
		SimpleDateFormat timeFormat = new SimpleDateFormat(timeFormatString);
		Date date = null;
    	Date time = null;
		try {
	    	date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			date = null;
		}
		try {
			time = timeFormat.parse(timeString);
		} catch (ParseException e) {
			time = null;
		}
		
		if (date != null) {
			cal.setTime(date);
			cal.set(Calendar.HOUR, time == null ? 0 : time.getHours());
			cal.set(Calendar.MINUTE, time == null ? 0 : time.getMinutes());
		} else {
			cal = null;
		}
		
		return cal;
    }
    
}
