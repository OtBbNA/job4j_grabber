package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parce {

    private static int ids = 1;

    private static int pagesNum = 5;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final String DEVELOPER_PAGE = String.format("%s?page=", PAGE_LINK);

    public static String retrieveDescription(String link) throws IOException {
        return Jsoup.connect(link).get().select(".style-ugc").text();
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> rsl = new ArrayList<>();
        for (int i = 1; i <= pagesNum; i++) {
            Connection connection = Jsoup.connect(String.format("%s%d", link, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateLinkElement = dateElement.child(0);
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyTitle = titleElement.text();
                String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String vacancyDescription = null;
                try {
                    vacancyDescription = retrieveDescription((String.format("%s%s", SOURCE_LINK, linkElement.attr("href"))));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LocalDateTime vacancyCreated = new HabrCareerDateTimeParser().parse(dateLinkElement.attr("datetime"));
                rsl.add(new Post(ids, vacancyTitle, vacancyLink, vacancyDescription, vacancyCreated));
                ids++;
            });
        }
        return rsl;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse();
        for (Post post : habrCareerParse.list(DEVELOPER_PAGE)) {
            System.out.println(post);
        }
    }
}