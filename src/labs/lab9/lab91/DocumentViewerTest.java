package labs.lab9.lab91;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

interface IDocument {
    String getText();
}

class SimpleDocument implements IDocument {
    private final String id;
    private final String text;

    public SimpleDocument(String id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public String getText() {
        return text;
    }
}

abstract class DocumentDecorator implements IDocument {
    private final IDocument document;

    public DocumentDecorator(IDocument document) {
        this.document = document;
    }

    @Override
    public String getText() {
        return document.getText();
    }
}

class LineNumbersDecorator extends DocumentDecorator {

    public LineNumbersDecorator(IDocument document) {
        super(document);
    }

    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        String text = super.getText();
        AtomicInteger count = new AtomicInteger(1);
        String[] lines = text.split("\n");
        Arrays.stream(lines).forEach(line -> sb.append(String.format("%s: %s\n", count.getAndIncrement(), line)));
        return sb.toString();
    }
}
class WordCountDecorator extends DocumentDecorator{

    public WordCountDecorator(IDocument document) {
        super(document);
    }

    @Override
    public String getText() {
        String text = super.getText();
        int wordCount = text.split("\\s++").length;
        return text + "Words: " + wordCount + "\n";
    }
}

class RedactionDecorator extends DocumentDecorator{
    private final List<String> forbiddenWords;

    public RedactionDecorator(IDocument document, List<String> forbiddenWords) {
        super(document);
        this.forbiddenWords = forbiddenWords;
    }

    @Override
    public String getText() {
        String text = super.getText();
        for (String forbiddenWord : forbiddenWords) {
            text = text.replaceAll("(?i)\\b" + Pattern.quote(forbiddenWord) + "\\b", "*");
        }
        return text;
    }
}
class DocumentViewer {
    private final Map<String, IDocument> documents = new LinkedHashMap<>();
    public DocumentViewer() {}

    public void addDocument(String id, String text) {
        documents.put(id, new SimpleDocument(id, text));
    }

    public void enableLineNumbers(String id) {
        documents.computeIfPresent(id, (k, doc) -> new LineNumbersDecorator(doc));
    }

    public void enableWordCount(String id) {
        documents.computeIfPresent(id, (k, doc) -> new WordCountDecorator(doc));
    }

    public void enableRedaction(String id, List<String> forbiddenWords) {
        documents.computeIfPresent(id, (k, doc) -> new RedactionDecorator(doc, forbiddenWords));
    }

    public void display(String id) {
        System.out.println("=== Document "+ id +" ===");
        System.out.print(documents.get(id).getText());
    }
}

public class DocumentViewerTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        DocumentViewer viewer = new DocumentViewer();

        int n = Integer.parseInt(scanner.nextLine());

        for (int i = 0; i < n; i++) {
            String id = scanner.nextLine();
            int numLines = Integer.parseInt(scanner.nextLine());

            StringBuilder textBuilder = new StringBuilder();
            for (int j = 0; j < numLines; j++) {
                textBuilder.append(scanner.nextLine()).append("\n");
            }
            viewer.addDocument(id, textBuilder.toString());
        }
        Set<String> ids = new HashSet<>();
        while (scanner.hasNext()) {
            String[] commandLineWords = scanner.nextLine().split("\\s+");
            String command = commandLineWords[0];
            if (command.equals("exit")) break;

            String id = commandLineWords[1];
            ids.add(id);
            List<String> forbiddenWords = new ArrayList<>();


            if (command.equals("enableLineNumbers")) {
                viewer.enableLineNumbers(id);
            }
            if (command.equals("enableWordCount")) {
                viewer.enableWordCount(id);
            }
            if (command.equals("enableRedaction")) {
                for (int i = 2; i < commandLineWords.length; i++) {
                    forbiddenWords.add(commandLineWords[i]);
                }
                viewer.enableRedaction(id, forbiddenWords);
            }
        }

        for (String id : ids) {
            viewer.display(id);
        }
    }
}