package vtor_kolokvium_2026.bucket;

import java.util.*;

interface Component {
    String getName();

    void print(int indent, StringBuilder sb);

    boolean isEmpty();

    default FolderComponent asFolder(){
        return null;
    }
}

class FileComponent implements Component {
    private final String name;

    public FileComponent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void print(int indent, StringBuilder sb) {
        sb.append("    ".repeat(indent)).append(name).append("\n");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}

class FolderComponent implements Component {
    private final String name;
    private final Map<String, Component> children = new LinkedHashMap<>();

    public FolderComponent(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void print(int indent, StringBuilder sb) {
        sb.append("    ".repeat(indent)).append(name).append("/\n");
        for (Component child : children.values()) {
            child.print(indent + 1, sb);
        }
    }
    @Override
    public FolderComponent asFolder(){
        return this;
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    public Map<String, Component> getChildren() {
        return children;
    }
}

class Bucket {
    private final FolderComponent root;

    public Bucket(String name) {
        this.root = new FolderComponent(name);
    }

    public void addObject(String key) {
        String[] parts = key.split("/");
        FolderComponent current = root;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            boolean isLastPart = (i == parts.length - 1);

            current.getChildren().putIfAbsent(part, isLastPart ? new FileComponent(part) : new FolderComponent(part));

            if (!isLastPart) {
            Component next = current.getChildren().get(part);
            current = next.asFolder();
            }
        }
    }

    public void removeObject(String key) {
        removeRecursive(root, key.split("/"), 0);
    }

    private void removeRecursive(FolderComponent current, String[] parts, int index) {
        if (index >= parts.length) {
            current.isEmpty();
            return;
        }
        String part = parts[index];
        Component child = current.getChildren().get(part);

        if (child == null) return;

        FolderComponent childFolder = child.asFolder();
        if(childFolder != null){
            removeRecursive(childFolder, parts, index+1);
            if (childFolder.isEmpty()){
                current.getChildren().remove(part);
            }
        }else{
            current.getChildren().remove(part);
        }
        current.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        root.print(0, sb);
        return sb.toString();
    }
}

public class BucketTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // bucket name is fixed
        Bucket bucket = new Bucket("bucket");

        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+", 2);
            String command = parts[0];

            if (command.equalsIgnoreCase("ADD")) {
                bucket.addObject(parts[1]);
            } else if (command.equalsIgnoreCase("REMOVE")) {
                bucket.removeObject(parts[1]);
            } else if (command.equalsIgnoreCase("PRINT")) {
                System.out.print(bucket);
            }
        }
    }
}

