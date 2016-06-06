package com.facebook.presto.udfs.scalar;

import com.facebook.presto.operator.Description;
import com.facebook.presto.operator.scalar.ScalarFunction;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.type.StandardTypes;
import com.facebook.presto.type.SqlType;
import io.airlift.slice.Slice;
import org.joda.time.DateTimeField;
import org.joda.time.chrono.ISOChronology;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.facebook.presto.operator.scalar.QuarterOfYearDateTimeField.QUARTER_OF_YEAR;
import static com.facebook.presto.spi.StandardErrorCode.INVALID_FUNCTION_ARGUMENT;
import static com.facebook.presto.spi.type.DateTimeEncoding.unpackMillisUtc;
import static com.facebook.presto.util.DateTimeZoneIndex.getChronology;
import static com.facebook.presto.util.DateTimeZoneIndex.unpackChronology;
import static java.util.Locale.ENGLISH;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.joda.time.DateTimeZone.UTC;


/**
 * Created by seven.wang on 2016/6/1.
 */
public final class DateTimeFunction {

    @ScalarFunction("unix_timestamp")
    @SqlType(StandardTypes.BIGINT)
    public static long unix_timestamp()
    {
        return System.currentTimeMillis()/1000;
    }

    @ScalarFunction("unix_timestamp")
    @SqlType(StandardTypes.BIGINT)
    public static long unix_timestamp(@Nullable @SqlType(StandardTypes.VARCHAR) Slice string)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(string.toStringUtf8()).getTime()/1000;
        } catch (ParseException e) {
        }
        return 0;
    }

    @ScalarFunction("unix_timestamp")
    @SqlType(StandardTypes.BIGINT)
    public static long unix_timestamp(@Nullable @SqlType(StandardTypes.VARCHAR) Slice string, @Nullable @SqlType(StandardTypes.VARCHAR) Slice string2)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(string2.toStringUtf8());
        try {
            return sdf.parse(string.toStringUtf8()).getTime()/1000;
        } catch (ParseException e) {
        }
        return 0;
    }

    private static final ISOChronology UTC_CHRONOLOGY = ISOChronology.getInstance(UTC);
    @Description("difference of the given dates in the given unit")
    @ScalarFunction("datediff")
    @SqlType(StandardTypes.BIGINT)
    public static long diffDate(ConnectorSession session, @SqlType(StandardTypes.VARCHAR) Slice unit, @SqlType(StandardTypes.DATE) long date1, @SqlType(StandardTypes.DATE) long date2)
    {
        return getDateField(UTC_CHRONOLOGY, unit).getDifferenceAsLong(DAYS.toMillis(date2), DAYS.toMillis(date1));
    }



    @Description("difference of the given times in the given unit")
    @ScalarFunction("datediff")
    @SqlType(StandardTypes.BIGINT)
    public static long diffTime(ConnectorSession session, @SqlType(StandardTypes.VARCHAR) Slice unit, @SqlType(StandardTypes.TIME) long time1, @SqlType(StandardTypes.TIME) long time2)
    {
        ISOChronology chronology = getChronology(session.getTimeZoneKey());
        return getTimeField(chronology, unit).getDifferenceAsLong(time2, time1);
    }

    @Description("difference of the given times in the given unit")
    @ScalarFunction("datediff")
    @SqlType(StandardTypes.BIGINT)
    public static long diffTimeWithTimeZone(
            @SqlType(StandardTypes.VARCHAR) Slice unit,
            @SqlType(StandardTypes.TIME_WITH_TIME_ZONE) long timeWithTimeZone1,
            @SqlType(StandardTypes.TIME_WITH_TIME_ZONE) long timeWithTimeZone2)
    {
        return getTimeField(unpackChronology(timeWithTimeZone1), unit).getDifferenceAsLong(unpackMillisUtc(timeWithTimeZone2), unpackMillisUtc(timeWithTimeZone1));
    }

    @Description("difference of the given times in the given unit")
    @ScalarFunction("datediff")
    @SqlType(StandardTypes.BIGINT)
    public static long diffTimestamp(
            ConnectorSession session,
            @SqlType(StandardTypes.VARCHAR) Slice unit,
            @SqlType(StandardTypes.TIMESTAMP) long timestamp1,
            @SqlType(StandardTypes.TIMESTAMP) long timestamp2)
    {
        return getTimestampField(getChronology(session.getTimeZoneKey()), unit).getDifferenceAsLong(timestamp2, timestamp1);
    }

    @Description("difference of the given times in the given unit")
    @ScalarFunction("datediff")
    @SqlType(StandardTypes.BIGINT)
    public static long diffTimestampWithTimeZone(
            @SqlType(StandardTypes.VARCHAR) Slice unit,
            @SqlType(StandardTypes.TIMESTAMP_WITH_TIME_ZONE) long timestampWithTimeZone1,
            @SqlType(StandardTypes.TIMESTAMP_WITH_TIME_ZONE) long timestampWithTimeZone2)
    {
        return getTimestampField(unpackChronology(timestampWithTimeZone1), unit).getDifferenceAsLong(unpackMillisUtc(timestampWithTimeZone2), unpackMillisUtc(timestampWithTimeZone1));
    }

    private static DateTimeField getDateField(ISOChronology chronology, Slice unit)
    {
        String unitString = unit.toStringUtf8().toLowerCase(ENGLISH);
        switch (unitString) {
            case "day":
                return chronology.dayOfMonth();
            case "week":
                return chronology.weekOfWeekyear();
            case "month":
                return chronology.monthOfYear();
            case "quarter":
                return QUARTER_OF_YEAR.getField(chronology);
            case "year":
                return chronology.year();
        }
        throw new PrestoException(INVALID_FUNCTION_ARGUMENT, "'" + unitString + "' is not a valid DATE field");
    }

    private static DateTimeField getTimeField(ISOChronology chronology, Slice unit)
    {
        String unitString = unit.toStringUtf8().toLowerCase(ENGLISH);
        switch (unitString) {
            case "millisecond":
                return chronology.millisOfSecond();
            case "second":
                return chronology.secondOfMinute();
            case "minute":
                return chronology.minuteOfHour();
            case "hour":
                return chronology.hourOfDay();
        }
        throw new PrestoException(INVALID_FUNCTION_ARGUMENT, "'" + unitString + "' is not a valid Time field");
    }

    private static DateTimeField getTimestampField(ISOChronology chronology, Slice unit)
    {
        String unitString = unit.toStringUtf8().toLowerCase(ENGLISH);
        switch (unitString) {
            case "millisecond":
                return chronology.millisOfSecond();
            case "second":
                return chronology.secondOfMinute();
            case "minute":
                return chronology.minuteOfHour();
            case "hour":
                return chronology.hourOfDay();
            case "day":
                return chronology.dayOfMonth();
            case "week":
                return chronology.weekOfWeekyear();
            case "month":
                return chronology.monthOfYear();
            case "quarter":
                return QUARTER_OF_YEAR.getField(chronology);
            case "year":
                return chronology.year();
        }
        throw new PrestoException(INVALID_FUNCTION_ARGUMENT, "'" + unitString + "' is not a valid Timestamp field");
    }

    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            System.out.println(sdf.parse("2016-05-29").getTime()/1000);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
