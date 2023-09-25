package ru.job4j.grabber;

import java.io.IOException;
import java.util.List;

public interface Parce {
    List<Post> list(String link) throws IOException;
}
