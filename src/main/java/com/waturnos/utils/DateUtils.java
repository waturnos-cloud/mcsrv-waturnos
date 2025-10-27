package com.waturnos.utils;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * Clase de utilidad para manejar fechas y horas usando java.time (Java 8+).
 * Optimizada para un contexto moderno (JDK 17+ y Spring Boot).
 * * <p><b>Nota para Spring Boot:</b> Evite usar los métodos de parseo y formateo
 * ({@link #parse(String, String)}, {@link #format(LocalDate, String)})
 * en las capas de Controller o Repository. Deje que el framework (Jackson, JPA)
 * maneje la conversión. Use {@code @JsonFormat} en DTOs o configure
 * las propiedades de Jackson/JPA en su {@code application.properties}.</p>
 *
 * Esta clase es final y no puede ser instanciada.
 */
public final class DateUtils {

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // 1. CONSTANTES
    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    /**
     * Zona horaria por defecto del sistema.
     */
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    /**
     * Formato ISO (Estándar): "yyyy-MM-dd" (ej. "2023-10-27")
     */
    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    /**
     * Formato ISO con tiempo: "yyyy-MM-ddTHH:mm:ss" (ej. "2023-10-27T10:15:30")
     */
    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    /**
     * Constructor privado para prevenir la instanciación.
     */
    private DateUtils() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada.");
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // 2. MÉTODOS "NOW" (OBTENER FECHA/HORA ACTUAL)
    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    /**
     * Obtiene la fecha actual (solo día, mes y año), sin hora.
     * @return un {@link LocalDate} (ej. "2023-10-27")
     */
    public static LocalDate getToday() {
        return LocalDate.now();
    }
    
    /**
     * Obtiene la fecha y hora actual.
     * @return un {@link LocalDateTime} (ej. "2023-10-27T10:15:30")
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * Obtiene la fecha y hora actual en una zona horaria específica.
     * @param zoneId La zona horaria (ej. ZoneId.of("America/New_York"))
     * @return un {@link ZonedDateTime}
     */
    public static ZonedDateTime getCurrentDateTime(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }
    
    /**
     * Obtiene los milisegundos de la época (epoch) actuales (UTC).
     * @return long que representa los milisegundos desde 1970-01-01T00:00:00Z.
     */
    public static long getCurrentEpochMilli() {
        return Instant.now().toEpochMilli();
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // 3. FORMATEO Y PARSEO (Usar con precaución)
    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    /**
     * Formatea una fecha (LocalDate) usando un patrón de texto.
     * <p><b>Advertencia:</b> En Spring Boot, prefiera que Jackson maneje esto
     * en la capa de API (ej. con {@code @JsonFormat}).</p>
     * * @param date La fecha a formatear.
     * @param pattern El patrón (ej. "dd-MMM-yyyy").
     * @return Un String formateado, o null si la fecha es null.
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null || pattern == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formatea una fecha y hora (LocalDateTime) usando un patrón de texto.
     * <p><b>Advertencia:</b> En Spring Boot, prefiera que Jackson maneje esto
     * en la capa de API (ej. con {@code @JsonFormat}).</p>
     * * @param dateTime La fecha y hora a formatear.
     * @param pattern El patrón (ej. "dd-MMM-yyyy HH:mm").
     * @return Un String formateado, o null si la fecha/hora es null.
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parsea un String a un LocalDate usando un patrón específico.
     * <p><b>Advertencia:</b> En Spring Boot, prefiera que Jackson maneje esto
     * en la capa de API (ej. con {@code @JsonFormat}).</p>
     * * @param dateString El texto de la fecha (ej. "27/10/2023").
     * @param pattern El patrón que coincide (ej. "dd/MM/yyyy").
     * @return un {@link LocalDate}, o null si el parseo falla o el input es null.
     */
    public static LocalDate parse(String dateString, String pattern) {
        if (dateString == null || pattern == null) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parsea un String a un LocalDateTime usando un patrón específico.
     * <p><b>Advertencia:</b> En Spring Boot, prefiera que Jackson maneje esto
     * en la capa de API (ej. con {@code @JsonFormat}).</p>
     * * @param dateTimeString El texto de la fecha (ej. "27/10/2023 10:15").
     * @param pattern El patrón que coincide (ej. "dd/MM/yyyy HH:mm").
     * @return un {@link LocalDateTime}, o null si el parseo falla o el input es null.
     */
    public static LocalDateTime parseDateTime(String dateTimeString, String pattern) {
        if (dateTimeString == null || pattern == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // 4. MANIPULACIÓN (Lógica de negocio)
    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    public static LocalDate addDays(LocalDate date, int days) {
        return (date == null) ? null : date.plusDays(days);
    }
    
    public static LocalDate subtractDays(LocalDate date, int days) {
        return (date == null) ? null : date.minusDays(days);
    }

    public static LocalDate addMonths(LocalDate date, int months) {
        return (date == null) ? null : date.plusMonths(months);
    }

    public static LocalDate addYears(LocalDate date, int years) {
        return (date == null) ? null : date.plusYears(years);
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, int hours) {
        return (dateTime == null) ? null : dateTime.plusHours(hours);
    }
    
    public static LocalDateTime addMinutes(LocalDateTime dateTime, int minutes) {
        return (dateTime == null) ? null : dateTime.plusMinutes(minutes);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // 5. AJUSTADORES Y "GETTERS" (Lógica de negocio)
    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    /**
     * Obtiene el inicio del día (00:00:00) para una fecha dada.
     * @param date La fecha.
     * @return un {@link LocalDateTime} a las 00:00.
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return (date == null) ? null : date.atStartOfDay();
    }
    
    /**
     * Obtiene el fin del día (23:59:59.999...) para una fecha dada.
     * @param date La fecha.
     * @return un {@link LocalDateTime} al final del día.
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return (date == null) ? null : date.atTime(LocalTime.MAX);
    }
    
    /**
     * Obtiene la fecha del primer día del mes para una fecha dada.
     * @param date La fecha.
     * @return un {@link LocalDate} (ej. "2023-10-01" si la entrada es "2023-10-27").
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return (date == null) ? null : date.with(TemporalAdjusters.firstDayOfMonth());
    }
    
    /**
     * Obtiene la fecha del último día del mes para una fecha dada.
     * @param date La fecha.
     * @return un {@link LocalDate} (ej. "2023-10-31" si la entrada es "2023-10-27").
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        return (date == null) ? null : date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Obtiene el día de la semana.
     * @param date La fecha.
     * @return un {@link DayOfWeek} (ej. MONDAY, TUESDAY...).
     */
    public static DayOfWeek getDayOfWeek(LocalDate date) {
        return (date == null) ? null : date.getDayOfWeek();
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // 6. CÁLCULOS Y COMPARACIONES (Lógica de negocio)
    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    /**
     * Calcula el número de días entre dos fechas.
     * @param start La fecha de inicio (inclusive).
     * @param end La fecha de fin (exclusive).
     * @return el número de días.
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calcula el número de meses completos entre dos fechas.
     * @param start La fecha de inicio.
     * @param end La fecha de fin.
     * @return el número de meses.
     */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Calcula el período (años, meses, días) entre dos fechas.
     * @param start La fecha de inicio.
     * @param end La fecha de fin.
     * @return un {@link Period}.
     */
    public static Period periodBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return Period.ZERO;
        return Period.between(start, end);
    }

    /**
     * Calcula la duración (horas, minutos, segundos) entre dos horas.
     * @param start La hora de inicio.
     * @param end La hora de fin.
     * @return un {@link Duration}.
     */
    public static Duration durationBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return Duration.ZERO;
        return Duration.between(start, end);
    }
    
    /**
     * Comprueba si una fecha es un fin de semana (Sábado o Domingo).
     * <p>Utiliza un <b>Switch Expression</b> (Java 14+).</p>
     * @param date La fecha.
     * @return true si es fin de semana, false en caso contrario.
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> false;
        };
    }

    /**
     * Comprueba si un año es bisiesto.
     * @param year el año (ej. 2024).
     * @return true si es bisiesto.
     */
    public static boolean isLeapYear(int year) {
        return Year.isLeap(year);
    }
    
    /**
     * Comprueba si dos fechas (LocalDate) representan el mismo día.
     * @param date1 Primera fecha
     * @param date2 Segunda fecha
     * @return true si son el mismo día.
     */
    public static boolean isSameDay(LocalDate date1, LocalDate date2) {
        if (date1 == null || date2 == null) return false;
        return date1.isEqual(date2);
    }

    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---
    // 7. CONVERSIÓN CON LEGACY java.util.Date
    // --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- ---

    /**
     * Convierte un {@link LocalDate} a un {@link java.util.Date} (legacy).
     * La hora se establece al inicio del día (00:00) en la zona horaria del sistema.
     * @param localDate La fecha a convertir.
     * @return un {@link java.util.Date}, o null.
     */
    public static Date toUtilDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant());
    }
    
    /**
     * Convierte un {@link LocalDateTime} a un {@link java.util.Date} (legacy).
     * @param localDateTime La fecha y hora a convertir.
     * @return un {@link java.util.Date}, o null.
     */
    public static Date toUtilDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(DEFAULT_ZONE_ID).toInstant());
    }

    /**
     * Convierte un {@link java.util.Date} (legacy) a un {@link LocalDate}.
     * @param date El {@link java.util.Date} a convertir.
     * @return un {@link LocalDate}, o null.
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDate();
    }
    
    /**
     * Convierte un {@link java.util.Date} (legacy) a un {@link LocalDateTime}.
     * @param date El {@link java.util.Date} a convertir.
     * @return un {@link LocalDateTime}, o null.
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime();
    }
}