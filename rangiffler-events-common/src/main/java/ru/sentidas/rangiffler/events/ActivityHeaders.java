package ru.sentidas.rangiffler.events;

public final class ActivityHeaders {
    private ActivityHeaders() {}

    public static final String EVENT_TYPE     = "event_type";      // дублируем enum в header (удобно для роутинга)
    public static final String EVENT_VERSION  = "event_version";   // "v1"
    public static final String PRODUCED_BY    = "produced_by";     // название сервиса/модуля
    public static final String TRACE_ID       = "trace_id";        // кореляция (опционально)
}
