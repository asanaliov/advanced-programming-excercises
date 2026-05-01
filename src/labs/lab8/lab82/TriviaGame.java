package labs.lab8.lab82;

import java.util.ArrayList;
import java.util.Scanner;


interface QuestionStrategy {
    void displayQuestion(String questionText);

    boolean isCorrect(String userAnswer, String correctAnswer);
}

class TrueFalseStrategy implements QuestionStrategy {

    @Override
    public void displayQuestion(String questionText) {
        System.out.println(questionText);
        System.out.println("Enter 'T' for true or 'F' for false.");
    }

    @Override
    public boolean isCorrect(String userAnswer, String correctAnswer) {
        if (userAnswer.isEmpty()) return false;
        return userAnswer.toLowerCase().charAt(0) == correctAnswer.toLowerCase().charAt(0);
    }
}

class FreeFormStrategy implements QuestionStrategy {

    @Override
    public void displayQuestion(String questionText) {
        System.out.println(questionText);
    }
    @Override
    public boolean isCorrect(String userAnswer, String correctAnswer) {
        if (userAnswer.isEmpty()) return false;
        return userAnswer.equalsIgnoreCase(correctAnswer);
    }
}

class TriviaQuestion {
    public String question;
    public String answer;
    public int value;
    public QuestionStrategy strategy;


    public TriviaQuestion(String question, String answer, int value, QuestionStrategy strategy) {
        this.question = question;
        this.answer = answer;
        this.value = value;
        this.strategy = strategy;
    }

    public void ask() {
        strategy.displayQuestion(question);
    }

    public boolean checkAnswer(String input) {
        return strategy.isCorrect(input, answer);
    }

    public String getAnswer() {
        return answer;
    }

    public int getValue() {
        return value;
    }
}

class TriviaData {

    private final ArrayList<TriviaQuestion> data = new ArrayList<>();


    public void addQuestion(String q, String a, int v, QuestionStrategy s) {
        data.add(new TriviaQuestion(q, a, v, s));
    }

    public int numQuestions() {
        return data.size();
    }

    public TriviaQuestion getQuestion(int index) {
        return data.get(index);
    }
}

public class TriviaGame {

    private final TriviaData triviaData;

    public TriviaGame() {
        triviaData = new TriviaData();
        loadQuestions();
    }

    public void loadQuestions() {
        triviaData.addQuestion("The possession of more than two sets of chromosomes is termed?",
                "polyploidy", 3, new FreeFormStrategy());
        triviaData.addQuestion("Erling Kagge skiied into the north pole alone on January 7, 1993.",
                "F", 1, new TrueFalseStrategy());
        triviaData.addQuestion("1997 British band that produced 'Tub Thumper'",
                "Chumbawumba", 2, new FreeFormStrategy());
        triviaData.addQuestion("I am the geometric figure most like a lost parrot",
                "polygon", 2, new FreeFormStrategy());
        triviaData.addQuestion("Generics were introducted to Java starting at version 5.0.",
                "T", 1, new TrueFalseStrategy());
    }

    public void start() {
        int score = 0;

        Scanner keyboard = new Scanner(System.in);

        for (int i = 0; i < triviaData.numQuestions(); i++) {
            TriviaQuestion q = triviaData.getQuestion(i);
            System.out.println("Question " + (i + 1) + ".  " + q.value + " points.");

            q.ask();
            String answer = keyboard.nextLine();

            if (q.checkAnswer(answer)) {
                System.out.println("That is correct!  You get " + q.getValue() + " points.");
                score += q.getValue();
            } else {
                System.out.println("Wrong, the correct answer is " + q.getAnswer());
            }
            System.out.println("Your score is " + score);
        }
        System.out.println("Game over!  Thanks for playing!");

    }

    public static void main(String[] args) {
        new TriviaGame().start();
    }
}
