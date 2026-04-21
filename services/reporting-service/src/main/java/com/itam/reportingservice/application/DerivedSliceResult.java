package com.itam.reportingservice.application;

public record DerivedSliceResult<T>(boolean available, T value) {

    public static <T> DerivedSliceResult<T> available(T value) {
        return new DerivedSliceResult<>(true, value);
    }

    public static <T> DerivedSliceResult<T> unavailable() {
        return new DerivedSliceResult<>(false, null);
    }
}
