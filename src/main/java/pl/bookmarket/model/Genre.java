package pl.bookmarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotBlank;
import pl.bookmarket.util.Views;
import pl.bookmarket.validation.constraints.UniqueGenre;

@Entity
@UniqueGenre
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "genreGenerator")
    @SequenceGenerator(name = "genreGenerator", sequenceName = "genre_sequence", allocationSize = 1)
    private Long id;

    @NotBlank(message = "{field.not.blank}")
    @JsonView({Views.Books.class, Views.Market.class})
    private String name;

    @OneToMany(mappedBy = "genre")
    @JsonIgnore
    private Set<Book> books;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }
}