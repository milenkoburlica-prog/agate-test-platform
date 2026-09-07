package at.co.svc.tosca.tsu.del;

import java.io.*;
import java.util.*;

public class MainToscaTableCleaner {

    public static void main(String[] args) {
        String basePath = System.getProperty("user.dir") + "\\TCSheet\\";
        String filesToProcess = "T_AUM_meldungAnlegen_V8_SYST_AUT1.txt";//,  
//                "T_VDAS_retrieveVersichertendatenPerStichtag_V16_Auth_1.txt", 
//                "T_AUM_meldungAnlegen_V8_SYST_AUT1.txt"
//                };

        start(basePath, filesToProcess);
    }


    public static void start(String basePath, String fileName) {
        String inputPath = basePath + fileName;
        String outPath = inputPath.replace(".txt", ".csv");

        try {
            // 1. Originalni koraci do generisanja _out5right
            processToscaFile(inputPath, outPath);
            String markedPath = inputPath.replace(".txt", "_out1.txt");
            processAndMarkTabs(outPath, markedPath);
            String markedPath2 = inputPath.replace(".txt", "_out2.txt");
            convertMarkedFileToCsv(markedPath, markedPath2);
            String markedPath3 = inputPath.replace(".txt", "_out3.txt");
            convertOut2ToOut3(markedPath2, markedPath3);
            String markedPath4left = inputPath.replace(".txt", "_out4left.txt");
            String markedPath4Right = inputPath.replace(".txt", "_out4right.txt");
            convertOut3ToOut4(markedPath3, markedPath4left, markedPath4Right);
            String markedPath5left = inputPath.replace(".txt", "_out5left.txt");
            convertOut4LeftToOut5Left(markedPath4left, markedPath5left);
            String markedPath5right = inputPath.replace(".txt", "_out5right.txt");
            convertOut4RightToOut5Right(markedPath4Right, markedPath5right);

            // 2. Finalno čišćenje (SADA OVO KORISTIMO)
            String flattenedLeft = inputPath.replace(".txt", "_out5left_FLAT.txt");
            String flattenedLeftClean = inputPath.replace(".txt", "_out5left_CLEAN.txt");
            String cleanedRightPath = inputPath.replace(".txt", "_out5right_CLEAN.txt");
            String markedPath6 = inputPath.replace(".txt", "_out6.txt"); // Ovo je finalni CSV

            // Očisti obe strane na po jednu kolonu
            flattenLeftPart(markedPath5left, flattenedLeft);
            //keepOnlyLastColumn(markedPath5right, cleanedRightPath);
            cleanLeftFile(flattenedLeft, flattenedLeftClean);
            // Spoji dve čiste kolone u jednu tabelu
            mergeFinalCsv(flattenedLeftClean, markedPath5right, outPath); 

            // 3. Brisanje privremenih fajlova (samo ono što ne treba)
//            deleteFile(outPath);
//            deleteFile(markedPath);
//            deleteFile(markedPath2);
//            deleteFile(markedPath3);
//            deleteFile(markedPath4left);
//            deleteFile(markedPath4Right);
//            deleteFile(markedPath5left);
//            deleteFile(markedPath5right);
//            deleteFile(flattenedLeft);
//            deleteFile(cleanedRightPath);
            
            System.out.println("✔ Obrada završena za: " + fileName + ". Rezultat je u: " + markedPath6);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    public static void cleanLeftFile(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Pronađi poziciju prvog znaka ';'
                int firstDelimiterIndex = line.indexOf(';');
                
                String firstColumn;
                if (firstDelimiterIndex != -1) {
                    // Uzmi sve što je pre prvog ';'
                    firstColumn = line.substring(0, firstDelimiterIndex).trim();
                } else {
                    // Ako nema ';' u redu, uzmi ceo red
                    firstColumn = line.trim();
                }
                
                // Upiši samo prvu kolonu (ako nije prazna)
                if (!firstColumn.isEmpty()) {
                    writer.write(firstColumn);
                    writer.newLine();
                }
            }
        }
    }
    
    public static void keepOnlyLastColumn(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(";", -1);
                
                // Uzimamo samo poslednju kolonu koja ima vrednost
                String lastVal = "";
                for (int i = columns.length - 1; i >= 0; i--) {
                    if (!columns[i].trim().isEmpty()) {
                        lastVal = columns[i];
                        break;
                    }
                }
                
                writer.write(lastVal);
                writer.newLine();
            }
        }
    }
    
    public static void processToscaFile(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String line;
            String pendingLine = "";
            int lastInstancesIndent = -1;

            while ((line = reader.readLine()) != null) {
                // Dodajemo novu liniju na prethodnu ako postoji prelom
                pendingLine += (pendingLine.isEmpty() ? "" : " ") + line;

                // Brojimo navodnike - ako je broj paran, red je kompletan
                long quoteCount = pendingLine.chars().filter(ch -> ch == '\"').count();
                
                // Ako broj nije paran, znači da je tekst prelomljen, preskačemo obradu dok ne nađemo zatvarajući navodnik
                if (quoteCount % 2 != 0) {
                    continue;
                }

                // Sada radimo sa kompletnim redom
                String fullLine = pendingLine;
                pendingLine = ""; // Resetujemo buffer

                if (fullLine.trim().isEmpty()) {
                    writer.write(fullLine);
                    writer.newLine();
                    continue;
                }

                int currentIndent = countLeadingTabs(fullLine);
                String trimmedLine = fullLine.trim();

                if ((trimmedLine.contains("\"Instances\"")) || (trimmedLine.contains("\"Instanzen\""))) {
                    lastInstancesIndent = currentIndent;
                    continue;
                }
                if (lastInstancesIndent != -1 && currentIndent > lastInstancesIndent) continue;
                if (currentIndent <= lastInstancesIndent) lastInstancesIndent = -1;

                writer.write(fullLine);
                writer.newLine();
            }
        }
    }
    
    
    public static void convertMarkedFileToCsv(String inputPath, String outputPath) throws IOException {
        List<String> outputLines = new ArrayList<>();
        Stack<NodeInfo> pathStack = new Stack<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            // Prvi red (Header) - ostaje tabovima razdvojen
            outputLines.add(reader.readLine().replace("<TAB>", "\t"));

            // Drugi red (Ime testa)
            String secondLine = reader.readLine();
            if (secondLine != null) {
                String[] parts = secondLine.split("<TAB>", 2);
                outputLines.add("\"\"\t" + parts[1].replace("<TAB>", "\t"));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                // 1. Prebroj koliko <TAB> ima na početku
                int indent = 0;
                String temp = line;
                while (temp.startsWith("<TAB>")) {
                    indent++;
                    temp = temp.substring(5); // dužina od "<TAB>" je 5
                }

                // 2. Podeli liniju na kolone koristeći <TAB>
                String[] columns = line.split("<TAB>", -1);
                
                // Ime je u koloni nakon poslednjeg indenta
                String rawName = columns[indent].trim().replace("\"", "");

                if (rawName.isEmpty()) {
                    outputLines.add(line.replace("<TAB>", "\t"));
                    continue;
                }

                // 3. Logika za hijerarhiju
                while (!pathStack.isEmpty() && pathStack.peek().indent >= indent) {
                    pathStack.pop();
                }

                String fullName = rawName;
                if (!pathStack.isEmpty()) {
                    fullName = pathStack.peek().fullName + "." + rawName;
                }

                pathStack.push(new NodeInfo(indent, fullName));

                // 4. Rekonstrukcija: [Indent Tabovi] + [Ime] + [Ostatak kolona]
                String reconstructedLine = "\t".repeat(indent) + "\"" + fullName + "\"";
                for (int i = indent + 1; i < columns.length; i++) {
                    reconstructedLine += "\t" + columns[i];
                }
                outputLines.add(reconstructedLine);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String l : outputLines) {
                writer.write(l);
                writer.newLine();
            }
        }
    }
    
    public static String parseToscaQuotedText(String input) {
        
        if (input.contains("Die Meldung kann nich")) {
            System.out.println();
        }
        if (input == null || input.isEmpty()) return input;

        String s = input.trim();

        // Dodaj uslov s.length() > 1 da izbegneš grešku
        if (s.length() > 1 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
        }

        // collapse multiple escaped quotes ("""""" -> "")
        while (s.contains("\"\"\"")) {
            s = s.replace("\"\"\"", "\"\"");
        }

        // CSV unescape
        s = s.replace("\"\"", "\"");

        // Java-safe escaping
        //s = s.replace("\"", "\\\"");

        return s;
    }
    
    
    public static void processAndMarkTabs(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line.replace("\t", "<TAB>"));
                writer.newLine();
            }
        }
    }

    public static void finalizeCsv(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // 1. Ukloni prvi red (header)
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // 2. Zameni sve <TAB> markere sa ;
                // Ostavljamo sve ostalo kako jeste jer je već formatirano u out4
                String formattedLine = line.replace("<TAB>", ";");
                
                // 3. Upiši u fajl
                writer.write(formattedLine);
                writer.newLine();
            }
        }
    }
    
    public static void convertOut2ToOut3(String inputPath, String outputPath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        // 1. Pronađi indeks "Filter" kolone iz prvog reda (headera)
        int filterIndex = -1;
        if (!lines.isEmpty()) {
            String[] headerCols = lines.get(0).split("\t", -1);
            for (int i = 0; i < headerCols.length; i++) {
                if (headerCols[i].contains("Filter")) {
                    filterIndex = i;
                    break;
                }
            }
        }

        // 2. Obradi sve redove koristeći taj fiksni indeks
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String line : lines) {
                String[] columns = line.split("\t", -1);

                // Popuni samo ako je indeks validan za ovaj red
                if (filterIndex != -1 && filterIndex < columns.length) {
                    columns[filterIndex] = "FILTER";
                }

                writer.write(String.join("\t", columns));
                writer.newLine();
            }
        }
    }
    
    
    public static void convertOut3ToOut4(String inputPath, String leftPath, String rightPath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        int filterIndex = -1;
        if (!lines.isEmpty()) {
            String[] headerCols = lines.get(0).split("\t", -1);
            for (int i = 0; i < headerCols.length; i++) {
                if (headerCols[i].contains("FILTER")) {
                    filterIndex = i;
                    break;
                }
            }
        }

        try (BufferedWriter leftWriter = new BufferedWriter(new FileWriter(leftPath));
             BufferedWriter rightWriter = new BufferedWriter(new FileWriter(rightPath))) {
            
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] columns = line.split("\t", -1);
                
                if (filterIndex != -1 && columns.length > filterIndex) {
                    // Leva strana
                    List<String> leftSide = new ArrayList<>();
                    for (int j = 0; j < filterIndex; j++) {
                        leftSide.add(columns[j]);
                    }
                    leftWriter.write(String.join("\t", leftSide));
                    leftWriter.newLine();

                    // Desna strana - OVO JE KLJUČNA IZMENA
                    // Ako je ovo prvi red (i == 0), ne upisujemo ga u rightWriter
                    if (i > 0) {
                        List<String> rightSide = new ArrayList<>();
                        for (int j = filterIndex + 1; j < columns.length; j++) {
                            rightSide.add(columns[j]);
                        }
                        rightWriter.write(String.join("\t", rightSide));
                        rightWriter.newLine();
                    }
                } else {
                    System.out.println("Preskakanje reda: " + line);
                }
            }
        }
    }
    
    

    public static void convertOut4LeftToOut5Left(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Umesto brisanja, zameni tabove sa ; da sačuvaš kolone
                String cleanedLine = line.replace("\t", ";");
                // Ako želiš da ukloniš navodnike, to je ok:
                cleanedLine = cleanedLine.replace("\"", "");
                
                writer.write(cleanedLine);
                writer.newLine();
            }
        }
    }
    public static void convertOut4RightToOut5Right(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Zameni tabove sa ;
                // Ostavljamo ostale podatke netaknute
                String formattedLine = line.replace("\t", ";");
               
                writer.write(formattedLine);
                writer.newLine();
            }
        }
    }
    

    public static void convertOut5ToOut6(String leftPath, String rightPath, String outputPath) throws IOException {
        List<String[]> leftMatrix = loadMatrix(leftPath);
        List<String[]> rightMatrix = loadMatrix(rightPath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            // 1. Spoji zaglavlja (prvi red)
            String[] combinedHeader = new String[leftMatrix.get(0).length + rightMatrix.get(0).length];
            System.arraycopy(leftMatrix.get(0), 0, combinedHeader, 0, leftMatrix.get(0).length);
            System.arraycopy(rightMatrix.get(0), 0, combinedHeader, leftMatrix.get(0).length, rightMatrix.get(0).length);
            
            writer.write(String.join(";", combinedHeader));
            writer.newLine();

            // 2. Prođi kroz redove podataka (od 1 pa nadalje)
            for (int i = 1; i < leftMatrix.size(); i++) {
                String[] leftRow = leftMatrix.get(i);
                String[] rightRow = rightMatrix.get(i);

                // Spajamo ih, ali PAŽLJIVO - bez brisanja ičega
                String combinedLine = String.join(";", leftRow) + ";" + String.join(";", rightRow);
                
                writer.write(combinedLine);
                writer.newLine();
            }
        }
    }
    
    public static void saveRightPathOnly(String rightPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(rightPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Samo prepiši liniju, bez ikakve logike
                writer.write(line);
                writer.newLine();
            }
        }
        System.out.println("Proba: rightPath je sačuvan u: " + outputPath);
    }
    public static void flattenLeftPart(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(";", -1);
                
                // 1. Pronađi prvu kolonu koja nije prazna
                String foundValue = "";
                for (String col : columns) {
                    if (!col.trim().isEmpty()) {
                        foundValue = col;
                        break;
                    }
                }
                
                // 2. Postavi tu vrednost u prvu kolonu
                columns[0] = foundValue;
                
                // 3. Sve ostale kolone postavi na prazno (osim ako ih ne želiš skroz obrisati)
                // Ovde čuvamo dužinu reda da se ne pokvari struktura matrice
                for (int i = 1; i < columns.length; i++) {
                    columns[i] = "";
                }
                
                // 4. Upiši novi red gde je sve "spljošteno" u prvu kolonu
                writer.write(String.join(";", columns));
                writer.newLine();
            }
        }
    }    
    
    public static void mergeFinalCsv(String leftPath, String rightPath, String outputPath) throws IOException {
        try (BufferedReader leftReader = new BufferedReader(new FileReader(leftPath));
             BufferedReader rightReader = new BufferedReader(new FileReader(rightPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String leftLine, rightLine;
            // Čitamo oba fajla red po red
            while ((leftLine = leftReader.readLine()) != null && (rightLine = rightReader.readLine()) != null) {
                
             // Obradi desnu stranu koja ima mnogo kolona
                String[] rightCols = rightLine.split(";", -1);
                for (int i = 0; i < rightCols.length; i++) {
                    rightCols[i] = parseToscaQuotedText(rightCols[i]);
                }
             // 2. SPOJI OBRADJENE KOLONE NAZAD U STRING
                String processedRightLine = String.join(";", rightCols);
                
                // Samo spajamo: leva strana + ";" + cela desna strana
                // Ne diramo sadržaj desne strane, ostavljamo svih stotine kolona
                String combinedLine = leftLine.trim() + ";" + processedRightLine.trim();
                
                writer.write(combinedLine);
                writer.newLine();
            }
        }
    }
    
    
    // Pomoćna metoda za učitavanje matrice
    private static List<String[]> loadMatrix(String path) throws IOException {
        List<String[]> matrix = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // OBAVEZNO -1 da sačuvaš prazne kolone (bitno za matricu!)
                matrix.add(line.split(";", -1));
            }
        }
        return matrix;
    }
    
    public static List<String[]> collapseColumns(List<String[]> matrix) {
        List<String[]> processedMatrix = new ArrayList<>();
        
        for (String[] row : matrix) {
            // 1. Pronađi prvu kolonu koja nije prazna (to je tvoj parametar)
            String parameterName = "";
            int lastEmptyIndex = -1;
            
            for (int i = 0; i < row.length; i++) {
                if (!row[i].trim().isEmpty()) {
                    parameterName = row[i];
                    lastEmptyIndex = i;
                    break;
                }
            }
            
            // 2. Kreiraj novi red
            // Prva kolona je parametar, ostatak su test podaci
            String[] newRow = new String[row.length - lastEmptyIndex];
            newRow[0] = parameterName;
            
            // 3. Prekopiraj ostatak matrice
            for (int j = 1; j < newRow.length; j++) {
                newRow[j] = row[lastEmptyIndex + j];
            }
            
            processedMatrix.add(newRow);
        }
        return processedMatrix;
    }
    
    public static void convertOut6ToOut7(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Preskoči prvi red
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Upiši sve ostale redove
                writer.write(line);
                writer.newLine();
            }
        }
    }
    
    public static void filterEmptyRows(String inputPath, String outputPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(";", -1);
                
                if (columns.length < 2) continue;

                // 1. Provera da li red ima bar jednu ne-praznu vrednost (od kolone 1 nadalje)
                boolean hasValue = false;
                for (int i = 1; i < columns.length; i++) {
                    String val = columns[i].trim().replace("\"", "");
                    if (!val.isEmpty()) {
                        hasValue = true;
                        break;
                    }
                }

                // Ako je red validan, formatiraj ga
                if (hasValue) {
                    // 2. Uniformisanje imena reda (prva kolona)
                    String name = columns[0].trim();
                    if (!name.startsWith("\"") || !name.endsWith("\"")) {
                        name = "\"" + name.replace("\"", "") + "\"";
                    }
                    columns[0] = name;

                    // 3. Rekonstrukcija reda i upis
                    writer.write(String.join(";", columns));
                    writer.newLine();
                }
            }
        }
    }
    
    
    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
    
    
    
    
    private static int countLeadingTabs(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == '\t') count++;
        return count;
    }

    static class NodeInfo {
        int indent;
        String fullName;
        NodeInfo(int indent, String fullName) { this.indent = indent; this.fullName = fullName; }
    }
}