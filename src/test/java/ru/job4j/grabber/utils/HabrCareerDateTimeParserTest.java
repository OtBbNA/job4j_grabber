package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {

    @Test
    void parse() {
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        String dateTime = "2023-09-23T12:27:24+03:00";
        assertThat(dateTimeParser.parse(dateTime).toString()).isEqualTo(dateTime.substring(0, dateTime.lastIndexOf("+")));
    }
}