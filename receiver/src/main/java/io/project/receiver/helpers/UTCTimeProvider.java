package io.project.receiver.helpers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 *
 * @author armena
 */
public class UTCTimeProvider {

    public static LocalDateTime getUtcTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTime
                .atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();
    }

}
