package labs.lab9.lab92;

import java.util.*;

interface XMLComponent {
    void addAttribute(String attribute, String value);

    void print(int indent);
}

class XMLLeaf implements XMLComponent {
    private final String tag;
    private final String value;
    private final Map<String, String> attributes = new LinkedHashMap<>();

    public XMLLeaf(String tag, String value) {
        this.tag = tag;
        this.value = value;
    }

    @Override
    public void addAttribute(String attribute, String value) {
        attributes.put(attribute, value);
    }

    @Override
    public void print(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("    ".repeat(indent));
        sb.append("<").append(tag);
        attributes.forEach((a, v) -> sb.append(String.format(" %s=\"%s\"", a, v)));
        sb.append(">").append(value);
        sb.append(String.format("</%s>\n", tag));
        System.out.print(sb.toString());
    }
}

class XMLComposite implements XMLComponent {
    private final String tag;
    private final Map<String, String> attributes = new LinkedHashMap<>();
    private final List<XMLComponent> children = new ArrayList<>();

    public XMLComposite(String tag) {
        this.tag = tag;
    }

    @Override
    public void addAttribute(String attribute, String value) {
        attributes.put(attribute, value);
    }

    @Override
    public void print(int indent) {
        String indentation = "    ".repeat(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(indentation).append("<").append(tag);
        attributes.forEach((a, v) -> sb.append(String.format(" %s=\"%s\"", a, v)));
        sb.append(">");
        System.out.println(sb);
        for (XMLComponent child : children) {
            child.print(indent+1);
        }
        System.out.println(indentation + String.format("</%s>", tag));
    }

    public void addComponent(XMLComponent component) {
        children.add(component);
    }

}

public class XMLTest {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int testCase = sc.nextInt();
        XMLComponent component = new XMLLeaf("student", "Trajce Trajkovski");
        component.addAttribute("type", "redoven");
        component.addAttribute("program", "KNI");

        XMLComposite composite = new XMLComposite("name");
        composite.addComponent(new XMLLeaf("first-name", "trajce"));
        composite.addComponent(new XMLLeaf("last-name", "trajkovski"));
        composite.addAttribute("type", "redoven");
        component.addAttribute("program", "KNI");

        if (testCase == 1) {
            component.print(0);
        } else if (testCase == 2) {
            composite.print(0);
        } else if (testCase == 3) {
            XMLComposite main = new XMLComposite("level1");
            main.addAttribute("level", "1");
            XMLComposite lvl2 = new XMLComposite("level2");
            lvl2.addAttribute("level", "2");
            XMLComposite lvl3 = new XMLComposite("level3");
            lvl3.addAttribute("level", "3");
            lvl3.addComponent(component);
            lvl2.addComponent(lvl3);
            lvl2.addComponent(composite);
            lvl2.addComponent(new XMLLeaf("something", "blabla"));
            main.addComponent(lvl2);
            main.addComponent(new XMLLeaf("course", "napredno programiranje"));

            main.print(0);
        }
    }
}
