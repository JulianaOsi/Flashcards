package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String importPath = null;
        String exportPath = null;
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("-import"))  importPath = args[++i];
            if (args[i].equals("-export"))  exportPath = args[++i];
        }
        FlashcardsPack newPack = new FlashcardsPack(importPath, exportPath);
        newPack.start();
    }
}

class FlashcardsPack {

    private LinkedHashMap<String, String> cards = new LinkedHashMap<>();
    private HashMap<String, Integer> errors = new HashMap<>();
    private ArrayList<String> logs = new ArrayList<>();
    private String importPath;
    private String exportPath;
    Scanner scanner = new Scanner(System.in);

    public FlashcardsPack() {
        this.importPath = null;
        this.exportPath = null;
    }

    public FlashcardsPack(String importPath, String exportPath) {
        this.importPath = importPath;
        this.exportPath = exportPath;
    }

    public void start() {
        if(!(importPath == null)) importFromFile(importPath);
        String action = "";
        while (true) {
            outputMsg("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            action = inputMsg();
            switch (action) {
                case "add":
                    add();
                    break;
                case "remove":
                    remove();
                    break;
                case "import":
                    importFromFile();
                    break;
                case "export":
                    exportToFile();
                    break;
                case "ask":
                    ask();
                    break;
                case "log":
                    log();
                    break;
                case "hardest card":
                    getHardestCard();
                    break;
                case "reset stats":
                    reset();
                    break;
                case "exit":
                    outputMsg("Bye bye!");
                    if (!(exportPath == null)) exportToFile(exportPath);
                    return;
                default:
                    outputMsg("Unsuitable action, please, try again");
            }
        }
    }

    private void add() {
        outputMsg("The card:");
        String card = inputMsg();

        if (cards.containsKey(card)) {
            outputMsg(String.format("The card \"%s\" already exists.", card));
            return;
        }

        outputMsg("The definition of the card:");
        String definition = inputMsg();
        if (cards.containsValue(definition)) {
            outputMsg(String.format("The definition \"%s\" already exists", definition));
            return;
        }

        cards.put(card, definition);
        outputMsg(String.format("The pair (\"%s\":\"%s\") has been added.", card, definition));
    }

    private void remove() {
        outputMsg("The card:");
        String card = inputMsg();

        String res =
                cards.remove(card) == null ?
                        String.format("Can't remove \"%s\": there is no such card.", card) :
                        "The card has been removed.";
        errors.remove(card);

        outputMsg(res);
    }

    private  void importFromFile() {
        outputMsg("File name:");
        String importPath = inputMsg();
        importFromFile(importPath);
    }

    private void importFromFile(String path) {
        File file = new File(path);

        int count = 0;

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String card = fileScanner.nextLine();
                String definition = fileScanner.nextLine();
                String error = fileScanner.nextLine();
                cards.put(card, definition);
                if (!error.equals("0")) errors.put(card, Integer.parseInt(error));
                count++;
            }
        } catch (FileNotFoundException e) {
            outputMsg("not found");
            return;
        }

        outputMsg(count + " cards have been loaded.");
    }

    private void exportToFile() {
        outputMsg("File name:");
        String exportPath = inputMsg();
        exportToFile(exportPath);
    }

    private void exportToFile(String path) {
        File file = new File(path);

        try (FileWriter writer = new FileWriter(file)) {
            for (String card: cards.keySet()) {
                writer.write(card + "\n");
                writer.write(cards.get(card) + "\n");
                writer.write(errors.getOrDefault(card, 0) + "\n");
            }
        } catch (IOException e) {
            outputMsg(String.format("An exception occurs %s", e.getMessage()));
            return;
        }

        outputMsg(cards.size() + " cards have been saved.");
    }

    private void ask() {
        outputMsg("How many times to ask?");
        int count = Integer.parseInt(inputMsg());
        Random random = new Random();
        String[] tmpKeys = cards.keySet().toArray(new String[cards.size()]);

        for (int i = 0; i < count; i++) {
            String rndCard = tmpKeys[random.nextInt(cards.size())];
            outputMsg(String.format("Print the definition of \"%s\":", rndCard));
            String answer = inputMsg();
            if (answer.equals(cards.get(rndCard)))
                outputMsg("Correct answer.");
            else if (cards.containsValue(answer)) {
                for (String key : cards.keySet()) {
                    if (cards.get(key).equals(answer)) {
                        errors.put(rndCard, errors.getOrDefault(rndCard, 0) + 1);
                        outputMsg(String.format("Wrong answer. The correct one is \"%s\", you've just written the definition of \"%s\".", cards.get(rndCard), key));
                        break;
                    }
                }
            } else {
                errors.put(rndCard, errors.getOrDefault(rndCard, 0) + 1);
                outputMsg(String.format("Wrong answer. The correct one is \"%s\".", cards.get(rndCard)));
            }
        }
    }

    private void log() {
        outputMsg("File name:");
        String path = inputMsg();

        File file = new File(path);
        try (FileWriter writer = new FileWriter(file)) {
            for (String log : logs) {
                writer.write(log + "\n");
            }
        } catch (IOException e) {
            outputMsg(String.format("An exception occurs %s", e.getMessage()));
            return;
        }
        outputMsg("The log has been saved.");
    }

    private void outputMsg(String msg) {
        System.out.println(msg);
        logs.add(msg);
    }

    private String inputMsg() {
        String msg = scanner.nextLine();
        logs.add(msg);
        return msg;
    }

    private void getHardestCard() {
        ArrayList<String> hardestCards = new ArrayList<>();
        int maxErrors = 0;
        for (String card : errors.keySet()) {
            if (errors.get(card) > maxErrors) {
                hardestCards.clear();
                hardestCards.add(card);
                maxErrors = errors.get(card);
            }
            else if (errors.get(card) == maxErrors) {
                hardestCards.add(card);
            }
        }
        if (hardestCards.isEmpty()) outputMsg("There are no cards with errors.");
        else if (hardestCards.size() == 1){
            outputMsg(String.format(
                    "The hardest card is \"%s\". You have %d errors answering it.",
                    hardestCards.get(0), maxErrors));
        }
        else {
            String res = "The hardest cards are ";
            for (int i = 0; i < hardestCards.size()-1; i++) {
                res += String.format("\"%s\", ", hardestCards.get(i));
            }
            res += String.format("\"%s\". ", hardestCards.get(hardestCards.size() - 1));
            res += String.format("You have %d errors answering it.", maxErrors);
            outputMsg(res);
        }
    }

    private void reset() {
        errors.clear();
        outputMsg("Card statistics has been reset.");
    }
}