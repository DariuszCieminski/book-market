package pl.bookmarket.testhelpers.datafactory;

import pl.bookmarket.model.Genre;

import java.util.ArrayList;
import java.util.List;

public class GenreFactory {

    private static final Genre defaultGenre = new Genre(1L, "Novel");
    private static long id = 8L;

    public static Genre getDefaultGenre() {
        return defaultGenre;
    }

    public static List<Genre> getGenresList() {
        List<Genre> list = new ArrayList<>();
        list.add(getDefaultGenre());
        list.add(new Genre(2L, "Mystery"));
        list.add(new Genre(3L, "Fantasy"));
        list.add(new Genre(4L, "Drama"));
        list.add(new Genre(5L, "Criminal"));
        list.add(new Genre(6L, "Comedy"));
        list.add(new Genre(7L, "SF"));
        return list;
    }

    public static Genre createGenre(String name) {
        return new Genre(id++, name);
    }
}