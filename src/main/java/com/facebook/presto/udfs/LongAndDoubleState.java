package com.facebook.presto.udfs;

public interface LongAndDoubleState
{
    long getLong();

    void setLong(long value);

    double getDouble();

    void setDouble(double value);
}