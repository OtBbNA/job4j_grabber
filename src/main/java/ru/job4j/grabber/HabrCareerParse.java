package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;

public class HabrCareerParse {

    private static int pagesNum = 5;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String DEVELOPER_PAGE = String.format("%s?page=", PAGE_LINK);

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= pagesNum; i++) {
            Connection connection = Jsoup.connect(String.format("%s%d", DEVELOPER_PAGE, i));
            System.out.println(String.format("%s%d", DEVELOPER_PAGE, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateLinkElement = dateElement.child(0);
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", vacancyName, new HabrCareerDateTimeParser().parse(dateLinkElement.attr("datetime")), link);
            });
        }
    }
}