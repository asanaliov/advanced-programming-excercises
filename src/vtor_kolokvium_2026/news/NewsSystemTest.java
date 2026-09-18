package vtor_kolokvium_2026.news;

import java.time.LocalDateTime;
import java.util.*;

interface Observer {
    void update(Article article);
    void printFeed();
}

class User implements Observer {
    private final String username;
    private final Set<Article> newsFeed = new TreeSet<>(Comparator.comparing(Article::getTimestamp));

    public User(String username) {
        this.username = username;
    }

    @Override
    public void update(Article article) {
        newsFeed.add(article);
    }

    public void printFeed() {
        newsFeed.forEach(System.out::println);
    }
}

class NewsSystem {
    private final Map<String, Observer> users = new HashMap<>();

    private final Map<String, Set<Observer>> categorySubscribers = new HashMap<>();
    private final Map<String, Set<Observer>> authorSubscribers = new HashMap<>();

    public NewsSystem(List<String> categoryNames, List<String> authorNames) {
        categoryNames.forEach(category -> categorySubscribers.put(category, new HashSet<>()));
        authorNames.forEach(author -> authorSubscribers.put(author, new HashSet<>()));
    }

    public void addUser(String username) {
        users.put(username, new User(username));
    }

    public void subscribeUserToCategory(String username, String categoryName) {
        categorySubscribers.get(categoryName).add(users.get(username));
    }

    public void unsubscribeUserFromCategory(String username, String categoryName) {
        categorySubscribers.get(categoryName).remove(users.get(username));
    }

    public void subscribeUserToAuthor(String username, String authorName) {
        authorSubscribers.get(authorName).add(users.get(username));
    }

    public void unsubscribeUserFromAuthor(String username, String authorName) {
        authorSubscribers.get(authorName).remove(users.get(username));
    }

    public void publishArticle(Article article) {
        String author = article.getAuthor();
        String category = article.getCategory();

        authorSubscribers.get(author).forEach(user -> user.update(article));
        categorySubscribers.get(category).forEach(user -> user.update(article));
    }

    public void printNewsForUser(String username) {
        System.out.println("News for user: " + username);
        users.get(username).printFeed();
    }
}

class Article {

    private final String category;
    private final String author;
    private final String content;
    private final LocalDateTime timestamp;

    public Article(String category, String author, String content, LocalDateTime timestamp) {
        this.category = category;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" +timestamp + "] " + author + " - " + category + "\n" +
                content;
    }
}


public class NewsSystemTest {

    public static void main(String[] args) {

        // Hardcoded categories and authors
        List<String> categories = List.of(
                "Technology", "Sports", "Politics", "Health", "Science",
                "Business", "Education", "Culture", "Travel", "Entertainment"
        );

        List<String> authors = List.of(
                "MartinFowler", "JohnDoe", "AliceSmith", "BobBrown", "JaneMiller"
        );

        NewsSystem system = new NewsSystem(categories, authors);

        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 2);
            String command = parts[0];

            switch (command) {

                case "ADD_USER":
                    system.addUser(parts[1]);
                    break;

                case "SUBSCRIBE_CATEGORY": {
                    String[] p = parts[1].split("\\s+");
                    system.subscribeUserToCategory(p[0], p[1]);
                    break;
                }

                case "UNSUBSCRIBE_CATEGORY": {
                    String[] p = parts[1].split("\\s+");
                    system.unsubscribeUserFromCategory(p[0], p[1]);
                    break;
                }

                case "SUBSCRIBE_AUTHOR": {
                    String[] p = parts[1].split("\\s+");
                    system.subscribeUserToAuthor(p[0], p[1]);
                    break;
                }

                case "UNSUBSCRIBE_AUTHOR": {
                    String[] p = parts[1].split("\\s+");
                    system.unsubscribeUserFromAuthor(p[0], p[1]);
                    break;
                }

                case "PUBLISH": {
                    // format:
                    // PUBLISH <category> <author> <timestamp> <content>
                    String[] p = parts[1].split("\\s+", 4);
                    Article article = new Article(
                            p[0],
                            p[1],
                            p[3],
                            LocalDateTime.parse(p[2])
                    );
                    system.publishArticle(article);
                    break;
                }

                case "PRINT":
                    system.printNewsForUser(parts[1]);
                    break;
            }
        }
    }
}




