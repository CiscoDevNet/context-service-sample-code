package com.cisco.thunderhead.sample.importexport;

import com.beust.jcommander.IStringConverter;
import com.cisco.thunderhead.util.RFC3339Date;

import java.text.ParseException;

/**
 * Created by ankitamuley on 4/20/16.
 * Required to parse JCommander arguments in Export.java
 */
public class RFC3339Converter implements IStringConverter<RFC3339Date> {
    @Override
    public RFC3339Date convert(String value) {
        try {
            return new RFC3339Date(value);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing date", e);
        }
    }
}
