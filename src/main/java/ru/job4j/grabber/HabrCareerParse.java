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

public class HabrCareerParse implements Parse {

    private static int pagesNum = 5;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static HabrCareerDateTimeParser habrCareerDateTimeParser;

    public HabrCareerParse(HabrCareerDateTimeParser habrCareerDateTimeParser) {
        this.habrCareerDateTimeParser = new HabrCareerDateTimeParser();
    }

    private String retrieveDescription(String link) {
        String rsl = null;
        try {
            rsl = Jsoup.connect(link).get().select(".style-ugc").text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rsl;
    }

    private Post postCreator(Element row) {
        Element dateElement = row.select(".vacancy-card__date").first();
        Element dateLinkElement = dateElement.child(0);
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyTitle = titleElement.text();
        String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String vacancyDescription = retrieveDescription((String.format("%s%s", SOURCE_LINK, linkElement.attr("href"))));
        LocalDateTime vacancyCreated = habrCareerDateTimeParser.parse(dateLinkElement.attr("datetime"));
        return new Post(vacancyTitle, vacancyLink, vacancyDescription, vacancyCreated);
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> rsl = new ArrayList<>();
        for (int i = 1; i <= pagesNum; i++) {
            Connection connection = Jsoup.connect(String.format("%s%d", link, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                rsl.add(postCreator(row));
            });
        }
        return rsl;
    }
}