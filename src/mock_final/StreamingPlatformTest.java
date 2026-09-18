package mock_final;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// ==================== EXCEPTIONS ====================

class UserPrivilegeException extends Exception {
    public UserPrivilegeException(String message) {
        super(message);
    }
}

class UserNotFoundException extends Exception {
    public UserNotFoundException(String userId) {
        super("User with ID " + userId + " not found");
    }
}

class MovieNotFoundException extends Exception {
    public MovieNotFoundException(String movieId) {
        super("Movie with ID " + movieId + " not found");
    }
}

// ==================== ENUMS ====================

enum Quality {
    P480(480), P720(720), P1080(1080), K4(2160);

    private final int resolution;

    Quality(int resolution) {
        this.resolution = resolution;
    }

    public int getResolution() {
        return resolution;
    }

    public static Quality fromString(String quality) {
        switch (quality.toUpperCase()) {
            case "480P":
                return P480;
            case "720P":
                return P720;
            case "1080P":
                return P1080;
            case "4K":
                return K4;
            default:
                throw new IllegalArgumentException("Invalid quality: " + quality);
        }
    }
}

enum Genre {
    ACTION, COMEDY, DRAMA, HORROR, SCIFI, ROMANCE, THRILLER, DOCUMENTARY
}

// ==================== MOVIE CLASS ====================

class Movie implements Comparable<Movie> {
    private final String id;
    private final String title;
    private final Genre genre;
    private final int duration;
    private final int releaseYear;
    private final double rating;

    public Movie(String id, String title, Genre genre, int duration, int releaseYear, double rating) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.releaseYear = releaseYear;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Genre getGenre() {
        return genre;
    }

    public int getDuration() {
        return duration;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public double getRating() {
        return rating;
    }


    @Override
    public int compareTo(Movie other) {
        if (this.rating > other.rating) return -1;
        else if (this.rating < other.rating) return 1;
        return this.title.compareTo(other.title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return Objects.equals(id, movie.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s (%d) - %s [%.1f⭐]", title, releaseYear, genre, rating);
    }
}

// ==================== WATCH EVENT ====================

class WatchEvent {
    private final String userId;
    private final String movieId;
    private final LocalDateTime timestamp;
    private final Quality quality;

    public WatchEvent(String userId, String movieId, Quality quality) {
        this.userId = userId;
        this.movieId = movieId;
        this.quality = quality;
        this.timestamp = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public String getMovieId() {
        return movieId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Quality getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        return String.format("[%s] User %s watched Movie %s in %s",
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                userId, movieId, quality);
    }
}

// ==================== USER CLASSES ====================

abstract class User {
    protected String id;
    protected String email;
    protected String name;
    protected List<Movie> watchHistory;

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.watchHistory = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public List<Movie> getWatchHistory() {
        return watchHistory;
    }

    public abstract boolean canWatch(Quality quality, int monthlyWatchCount) throws UserPrivilegeException;

    public void addToWatchHistory(Movie movie) {
        watchHistory.add(movie);
    }

    public int getUniqueMoviesWatched() {
        return new HashSet<>(watchHistory)
                .size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

class FreeUser extends User {
    private static final int MAX_MONTHLY_WATCHES = 5;
    private static final Quality MAX_QUALITY = Quality.P720;

    public FreeUser(String id, String email, String name) {
        super(id, email, name);
    }

    @Override
    public boolean canWatch(Quality quality, int monthlyWatchCount) throws UserPrivilegeException {
        if (quality.getResolution() > MAX_QUALITY.getResolution() || monthlyWatchCount >= MAX_MONTHLY_WATCHES)
            throw new UserPrivilegeException("Premium only future");
        return true;
    }


    @Override
    public String toString() {
        return String.format("FREE: %s (%s) - %d movies watched", name, email, watchHistory.size());
    }
}

class PremiumUser extends User {
    private final LocalDateTime subscriptionDate;

    public PremiumUser(String id, String email, String name, LocalDateTime subscriptionDate) {
        super(id, email, name);
        this.subscriptionDate = subscriptionDate;
    }

    @Override
    public boolean canWatch(Quality quality, int monthlyWatchCount) throws UserPrivilegeException {
        return true;
    }

    @Override
    public String toString() {
        return String.format("PREMIUM: %s (%s) - %d movies watched [Since %s]",
                name, email, watchHistory.size(),
                subscriptionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
}

class UserFactory {
    public static User read(String[] parts) {
        // Format: F;id;email;name OR P;id;email;name;subscriptionDate
        // Example: F;u001;john@email.com;John Doe
        // Example: P;u002;jane@email.com;Jane Smith;2024-01-15T10:30:00
        String type = parts[0];
        String id = parts[1];
        String email = parts[2];
        String name = parts[3];
        if (type.equals("P")) {
            LocalDateTime subDate = LocalDateTime.parse(parts[4]);
            return new PremiumUser(id, email, name, subDate);
        }
        return new FreeUser(id, email, name);

    }
}
// ==================== STREAMING PLATFORM ====================

class StreamingPlatform {
    private final Map<String, Movie> movies;
    private final Map<String, User> users;
    private final List<WatchEvent> watchEvents;

    public StreamingPlatform() {
        this.movies = new HashMap<>();
        this.users = new HashMap<>();
        this.watchEvents = new ArrayList<>();
    }

    public void readMovies(InputStream is) {
        // Format: id;title;genre;duration;year;rating
        // Example: m001;Inception;SCIFI;148;2010;8.8

        Scanner scanner = new Scanner(is);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] words = line.split(";");
            String id = words[0];
            String title = words[1];
            Genre genre = Genre.valueOf(words[2]);
            int duration = Integer.parseInt(words[3]);
            int year = Integer.parseInt(words[4]);
            double rating = Double.parseDouble(words[5]);
            movies.put(id, new Movie(id, title, genre, duration, year, rating));
        }
    }

    public void readUsers(InputStream is) {
        Scanner scanner = new Scanner(is);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parts = line.split(";");
            User user = UserFactory.read(parts);
            users.put(user.getId(), user);
        }

    }

    public void watchMovie(String userId, String movieId, String quality)
            throws UserNotFoundException, MovieNotFoundException, UserPrivilegeException {
        if (!movies.containsKey(movieId)) throw new MovieNotFoundException(movieId);
        if (!users.containsKey(userId)) throw new UserNotFoundException(userId);

        User user = users.get(userId);
        Movie movie = movies.get(movieId);
        Quality qualityValue = Quality.fromString(quality);
        int monthlyWatchCount = getMonthlyWatchCount(userId);

        if (user.canWatch(qualityValue, monthlyWatchCount)) {
            WatchEvent watchEvent = new WatchEvent(userId, movieId, qualityValue);
            watchEvents.add(watchEvent);
            user.watchHistory.add(movie);
        }
    }

    private int getMonthlyWatchCount(String userId) {
        return (int) watchEvents.stream()
                .filter(watchEvent -> watchEvent.getUserId().equals(userId))
                .filter(e -> e.getTimestamp().getYear() == LocalDateTime.now().getYear()
                        && e.getTimestamp().getMonth() == LocalDateTime.now().getMonth())
                .count();

    }

    public Map<String, List<Movie>> getMoviesByGenre(int minRating) {
        Map<String, List<Movie>> grouped = movies.values().stream()
                .filter(movie -> movie.getRating() > minRating)
                .collect(Collectors.groupingBy(
                        movie -> movie.getGenre().name(),
                        TreeMap::new,
                        Collectors.toList()
                ));
        grouped.values().forEach(list -> list.sort(
                Comparator.comparing(Movie::getRating).reversed()
                        .thenComparing(Movie::getTitle)
        ));
        return grouped;
    }

    public Map<Integer, Long> getWatchStatsByYear() {
        return watchEvents.stream()
                .map(watchEvent -> movies.get(watchEvent.getMovieId()))
                .collect(Collectors.groupingBy(
                        Movie::getReleaseYear,
                        () -> new TreeMap<>(Comparator.reverseOrder()),
                        Collectors.counting()
                ));
    }

    public List<User> getTopNUsers(int n) {
        return users.values().stream()
                .sorted(Comparator.comparingInt(User::getUniqueMoviesWatched).reversed().thenComparing(User::getId))
                .limit(n)
                .collect(Collectors.toList());
    }

    public Map<String, Double> getAverageRatingByGenre() {
        Map<String, Double> groupedByGenre = watchEvents.stream()
                .map(event -> movies.get(event.getMovieId()))
                .collect(Collectors.groupingBy(
                        movie -> movie.getGenre().name(),
                        () -> new TreeMap<>(),
                        Collectors.averagingDouble(Movie::getRating)
                ));

        return groupedByGenre.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public Map<String, Long> moviesByGenreDistribution() {
        return movies.values()
                .stream()
                .collect(Collectors.groupingBy(
                        movie -> movie.getGenre().name(),
                        Collectors.counting()
                ));
    }

    public long premiumUsersCount() {
        return users.values()
                .stream()
                .filter(user -> user instanceof PremiumUser)
                .count();
    }

    public long freeUsersCount() {
        return users.values()
                .stream()
                .filter(user -> user instanceof FreeUser)
                .count();
    }

    public long totalUsers() {
        return premiumUsersCount() + freeUsersCount();
    }

    public double premiumUserPercentage() {
        if (totalUsers() == 0) return 0.0;
        return (premiumUsersCount() * 100.0) / totalUsers();
    }
    public double freeUserPercentage() {
        if (totalUsers() == 0) return 0.0;
        return (freeUsersCount() * 100.0) / totalUsers();
    }

    public List<String> movieIds() {
        return watchEvents.stream()
                .map(WatchEvent::getMovieId)
                .collect(Collectors.toList());
    }

    public Movie mostWatchedMovie() {

        Map<String, Integer> moviesCount = new TreeMap<>();

        movieIds().forEach(id -> moviesCount.put(id, moviesCount.getOrDefault(id, 0) + 1));

        String mostWatchedId = moviesCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No movies watched");

        return movies.get(mostWatchedId);
    }

    public User mostActiveUser() {
        return watchEvents.stream()
                .collect(Collectors.groupingBy(
                        WatchEvent::getUserId,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(stringLongEntry -> users.get(stringLongEntry.getKey()))
                .orElse(null);
    }

    public void generateReport(OutputStream os) {
        PrintWriter pw = new PrintWriter(os);

        pw.println("========== STREAMING PLATFORM REPORT ==========");
        pw.println("Total Movies: " + movies.size());
        pw.println("Total Users: " + users.size());
        pw.println("Total Watches: " + watchEvents.size());
        pw.println();

        pw.println("Movies Distribution: " + moviesByGenreDistribution());

        pw.println("Free Plan users " + freeUserPercentage());

        pw.println("Premium Plan users " + premiumUserPercentage());

        pw.println("Most watched movie: " + mostWatchedMovie());

        pw.println("Most active user: " + mostActiveUser());
        pw.flush();
    }

    // BONUS METHODS

    public List<User> getBingeWatchers() {
        Map<String, Map<LocalDate, Long>> watchesByUserAndDate = watchEvents.stream()
                .collect(Collectors.groupingBy(
                        WatchEvent::getUserId,
                        Collectors.groupingBy(
                                event -> event.getTimestamp().toLocalDate(),
                                Collectors.counting()
                        )
                ));

        Set<String> bingeWatchersId = watchesByUserAndDate.entrySet().stream()
                .filter(entry -> entry.getValue().values().stream()
                        .anyMatch(count -> count >= 5))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return bingeWatchersId.stream()
                .map(userId -> users.get(userId))
                .collect(Collectors.toList());
    }

    public List<FreeUser> getPremiumConversionCandidates() {
        // TODO: Find FreeUsers who hit monthly limit 2+ times
        return users.values().stream()
                .filter(user -> user instanceof FreeUser)
                .map(user -> (FreeUser) user)
                .filter(freeUser -> {
                    Map<YearMonth, Long> watchesPerMonth = watchEvents.stream()
                            .filter(event -> event.getUserId().equals(freeUser.getId()))
                            .collect(Collectors.groupingBy(
                                    event -> YearMonth.from(event.getTimestamp()),
                                    Collectors.counting()
                            ));
                    long monthsAtLimit = watchesPerMonth.values().stream()
                            .filter(count -> count >= 5)
                            .count();
                    return monthsAtLimit >= 2;
                })
                .collect(Collectors.toList());
    }
}

// ==================== TEST CLASS ====================

public class StreamingPlatformTest {

    public static void main(String[] args) {
        StreamingPlatform platform = new StreamingPlatform();

        // Test with sample data
        String moviesData = """
                m001;Inception;SCIFI;148;2010;8.8
                m002;The Shawshank Redemption;DRAMA;142;1994;9.3
                m003;The Dark Knight;ACTION;152;2008;9.0
                m004;Pulp Fiction;THRILLER;154;1994;8.9
                m005;Forrest Gump;DRAMA;142;1994;8.8
                m006;The Matrix;SCIFI;136;1999;8.7
                m007;Interstellar;SCIFI;169;2014;8.6
                m008;The Godfather;DRAMA;175;1972;9.2
                """;

        String usersData = """
                F;u001;john@email.com;John Doe
                F;u002;alice@email.com;Alice Smith
                P;u003;bob@email.com;Bob Johnson;2024-01-15T10:30:00
                P;u004;carol@email.com;Carol White;2023-06-20T14:00:00
                F;u005;dave@email.com;Dave Brown
                """;

        platform.readMovies(new ByteArrayInputStream(moviesData.getBytes()));
        platform.readUsers(new ByteArrayInputStream(usersData.getBytes()));

        // Simulate some watch events
        try {
            platform.watchMovie("u001", "m001", "720p");
            platform.watchMovie("u001", "m002", "720p");
            platform.watchMovie("u003", "m001", "4K");
            platform.watchMovie("u003", "m003", "4K");
            platform.watchMovie("u004", "m008", "1080p");

            // This should throw exception - FreeUser trying to watch in 4K
            // platform.watchMovie("u001", "m003", "4K");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Generate report
        System.out.println("\n=== TESTING REPORT GENERATION ===\n");
        platform.generateReport(System.out);

        // === MOVIES BY GENRE (Rating >= 8.5) ===
        System.out.println("\n=== MOVIES BY GENRE (Rating >= 8.5) ===");
        Map<String, List<Movie>> moviesByGenre = platform.getMoviesByGenre(8);
        moviesByGenre.forEach((genre, list) -> {
            System.out.println(genre + ":");
            list.forEach(m -> System.out.println("  - " + m));
        });

        // === TOP 3 USERS ===
        System.out.println("\n=== TOP 3 USERS ===");
        List<User> topUsers = platform.getTopNUsers(3);
        topUsers.forEach(u -> System.out.println(u + " (Unique: " + u.getUniqueMoviesWatched() + ")"));

        // === WATCH STATS BY YEAR ===
        System.out.println("\n=== WATCH STATS BY YEAR ===");
        Map<Integer, Long> statsByYear = platform.getWatchStatsByYear();
        statsByYear.forEach((year, count) -> System.out.println(year + " -> " + count + " watches"));
    }

}