import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GotoRemover {

    public static List<String> readFileInArray(String filename)
            throws IOException {
        List<String> listOfStrings = new ArrayList<>();

        BufferedReader bf = new BufferedReader(new FileReader(filename));

        String line = bf.readLine();

        while (line != null) {
            listOfStrings.add(line);
            line = bf.readLine();
        }
        bf.close();

        return listOfStrings;
    }

    public static void writeFile(List<String> arr, String path) throws IOException {
        FileWriter writer = new FileWriter("res_"+path);
        for (String str: arr) {
            writer.write(str+"\n");
        }
        writer.close();
    }

    public static int gotoSearch(List<String> code) {
        for (int i = 0; i < code.size(); i++) {
            if (code.get(i).contains("goto")) {
                if (code.get(i).trim().indexOf("goto") == 0) {
                    int index = code.get(i).indexOf("goto");
                    code.set(i, code.get(i).substring(0, index) + "if(true) " + code.get(i).substring(index));
                }
                return i;
            }
        }
        return -1;
    }

    public static String labelName(String line) {
        String temp = line.substring(line.indexOf("goto") + 5);
        return temp.substring(0, temp.length() - 1);
    }

    public static int labelSearch(String label, List<String> code) {
        for (int i = 0; i < code.size(); i++) {
            if (code.get(i).contains(label + ":"))
                return i;
        }
        return -1;
    }

    public static String condition(String line) {
        return line.substring(line.indexOf("("), line.indexOf(")")+1);
    }

    public static int level(List<String> code, int stringNumber){
        int lvl = 0;
        for (int i = 0; i < stringNumber; i++) {
            if(code.get(i).contains("{")) lvl++;
            if(code.get(i).contains("}")) lvl--;
        }
        return lvl;
    }

    public static void gotoEliminating(List<String> code, String label, int gotoStringNumber, int labelStringNumber, String cond) {
        if (gotoStringNumber < labelStringNumber) {

            code.set(gotoStringNumber, code.get(gotoStringNumber).substring(0, code.get(gotoStringNumber).indexOf("(") + 1) +
                    "!" + cond + "){");
            String tabulation = code.get(gotoStringNumber).substring(0, code.get(gotoStringNumber).indexOf("i"));

            for (int i = gotoStringNumber+1; i < labelStringNumber; i++) {
                code.set(i, "    " +code.get(i));
            }

            code.set(labelStringNumber, tabulation+"}");
        } else if (gotoStringNumber > labelStringNumber) {
            String tabulation = code.get(labelStringNumber).substring(0, code.get(labelStringNumber).indexOf(label));
            code.set(labelStringNumber, tabulation+"do{");

            for (int i = labelStringNumber+1; i < gotoStringNumber; i++) {
                code.set(i, "    " +code.get(i));
            }

            code.set(gotoStringNumber, tabulation+"}while("+cond+")");
        }
    }

    public static void gotoMoving(List<String> code, String label, int gotoStringNumber, int labelStringNumber, String cond, int gotoLevel, int labelLevel){
        if(gotoLevel>labelLevel){
            String tabulation = "";
            for (int i = 0; i < gotoLevel; i++) {
                tabulation+="    ";
            }

            code.set(gotoStringNumber, tabulation+"if(goto_"+label+") break;");


            int bracketStringNumber = 0;
            for (int i = gotoStringNumber; i < code.size(); i++) {
                if(code.get(i).contains("}")){
                    bracketStringNumber = i;
                    break;
                }
            }

            code.set(labelStringNumber, code.get(labelStringNumber) + " goto_"+label+"=0;");

            code.add(bracketStringNumber+1, tabulation.substring(4)+"if(goto_"+label+") goto "+label+";");

            code.add(gotoStringNumber, tabulation+"goto_"+label+"="+cond+";");
            int mainString = 0;
            for (int i = 0; i < code.size(); i++) {
                if(code.get(i).contains("main(")){
                    mainString = i;
                    break;
                }
            }
            code.add(mainString+2, "    boolean goto_"+label+";");
        }
    }

    public static void removeGoto(String path) throws IOException {
        List<String> code = readFileInArray(path);

        int gotoStringNumber = gotoSearch(code);
        String label = labelName(code.get(gotoStringNumber));
        int labelStringNumber = labelSearch(label, code);
        String cond = condition(code.get(gotoStringNumber));

        int gotoLevel = level(code, gotoStringNumber);
        int labelLevel = level(code, labelStringNumber);

        if(labelLevel!=gotoLevel){
            gotoMoving(code, label, gotoStringNumber, labelStringNumber, cond, gotoLevel, labelLevel);
        }else{
            gotoEliminating(code, label, gotoStringNumber, labelStringNumber, cond);
        }

        writeFile(code, path);

        for (String line : code) {
            System.out.println(line);
        }
    }

    public static void main(String[] args) throws IOException {
        removeGoto("test1.txt");
        removeGoto("test2.txt");
        removeGoto("test3.txt");
        removeGoto("test4.txt");
    }
}