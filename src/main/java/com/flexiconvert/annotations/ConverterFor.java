package com.flexiconvert.annotations;

import com.flexiconvert.ConversionType;
import java.lang.annotation.*;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConverterFor {
    ConversionType value();
}