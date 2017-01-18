import java.io.*;
import java.util.Arrays;

public class datToCSV {

    public static void main(String[] args) throws IOException {
        String folderDir = "/Users/Slurpy/Desktop/MQA_TEST_FILES/";
        String fileFolder = "It_Aint_Me_Babe";
        String fileName = folderDir+fileFolder+"/"+fileFolder+".dat";
        File file = new File(fileName);


        System.out.println("input file path: "+fileName);

        if(file.exists()) {

            int averageRange = 441000;

            String[] fileLine = convertToStrings(fileName);
            System.out.println("Calculating L-R, L+R, and hologram ratio...");
            double[][] dataLine = parseStringFile(fileLine);
            dataLine = setSumDiff(dataLine);
            getArrayStats(dataLine);
            dataLine = setBounds(dataLine, 0.0001, 0.0001);
            dataLine = throwOutliers(dataLine, 4);
            dataLine = setAverage(dataLine, averageRange);

            System.out.println("Sample rate: " + (int) Math.round(1/(dataLine[1][0] - dataLine[0][0])));
            System.out.println("Averaging function length: " + averageRange / ((int) Math.round(1/(dataLine[1][0] - dataLine[0][0]))) + " seconds");
            System.out.println("File analyzed successfully.");
            System.out.println();

            writeDat(folderDir, fileFolder, dataLine);
            writeAverageDat(folderDir, fileFolder, dataLine);
            writeGnuplotConfig(folderDir, fileFolder);
            writeAVGGnuplotConfig(folderDir, fileFolder);
            writeStats(folderDir, fileFolder, dataLine);


        }
        else{
            System.out.println("File "+file.getName()+" not found");
            System.out.println();
        }

    }

    public static void writeDat(String directory, String fileName, double[][] data){
        try{
            System.out.println("Writing DAT file...");
            PrintWriter writer = new PrintWriter(directory+fileName+"/"+fileName+".dat", "UTF-8");

            for(int i = 0; i < data.length; i++){
                writer.print(data[i][0]);
                writer.print("  ");
                writer.print(data[i][1]);
                writer.print("  ");
                writer.print(data[i][2]);
                writer.print("  ");
                writer.print(data[i][6]);
                writer.print("  ");
                writer.println();
            }

            writer.close();
            System.out.println("File "+fileName+".dat written successfully.");
            System.out.println();

        } catch (IOException e) {
            System.out.println("File not written successfully.");
            System.out.println();
        }
    }
    public static void writeAverageDat(String directory, String fileName, double[][] data){
        try{
            System.out.println("Writing averaged file...");
            PrintWriter writer = new PrintWriter(directory+fileName+"/"+fileName+"AVG.dat", "UTF-8");

            //get max values, get derivatives of signals
            double lInMax = 0.0;
            double rInMax = 0.0;
            double lOutMax = 0.0;
            double rOutMax = 0.0;
            for(int i = 0; i < data.length; i++){
                if(i == 0 || i == data.length -1){
                    data[i][3] = 0;
                    data[i][4] = 0;
                }else{
                    data[i][3] = (data[i+1][1] - data[i-1][1]) / (data[i+1][0] - data[i-1][0]);
                    data[i][4] = (data[i+1][2] - data[i-1][2]) / (data[i+1][0] - data[i-1][0]);
                }

                if(Math.abs(data[i][1]) > lInMax){
                    lInMax = Math.abs(data[i][1]);
                    //System.out.println(lMax);
                }
                if(Math.abs(data[i][2]) > rInMax){
                    rInMax = Math.abs(data[i][2]);
                    //System.out.println(rMax);
                }
                if(Math.abs(data[i][3]) > lOutMax){
                    lOutMax = Math.abs(data[i][3]);
                    //System.out.println(lMax);
                }
                if(Math.abs(data[i][4]) > rOutMax){
                    rOutMax = Math.abs(data[i][4]);
                    //System.out.println(rMax);
                }
            }

            //normalize signals
            for(int i = 0; i < data.length; i++){
                data[i][3] = data[i][3] / lOutMax * lInMax;
                data[i][4] = data[i][4] / rOutMax * rInMax;
            }

            //System.out.println("lInMax: "+lInMax);
            //System.out.println("rInMax: "+rInMax);
            //System.out.println("lOutMax: "+lOutMax);
            //System.out.println("rOutMax: "+rOutMax);

            //add abs val of sig and derivative
            for(int i = 0; i < data.length; i++){
                data[i][3] = Math.abs(data[i][1]) + Math.abs(data[i][3]);
                data[i][4] = Math.abs(data[i][2]) + Math.abs(data[i][4]);
            }

            //average summed sig
            int avgCount = 50;
            for(int i = 0; i < data.length; i++){

                double lSum = 0;
                double rSum = 0;
                if(i-avgCount >= 0 && i+avgCount < data.length) {
                    for (int j = i - avgCount; j <= i + avgCount; j++) {
                        lSum += data[j][3];
                        rSum += data[j][4];
                    }
                }
                data[i][3] = lSum / (avgCount * 2 +1);
                data[i][4] = rSum / (avgCount * 2 +1);
            }


            for(int i = 0; i < data.length; i+=1000){
            //for(int i = 0; i < data.length; i++){

                for(int j = 0; j < data[0].length; j++) {
                    writer.print(data[i][j]);
                    if(j == data[0].length - 1){
                        writer.println();
                    }else{
                        writer.print("  ");
                    }
                }
            }

            writer.close();
            System.out.println("File "+fileName+"AVG.dat written successfully.");
            System.out.println();

        } catch (IOException e) {
            System.out.println("File not written successfully.");
            System.out.println();
        }
    }
    public static void writeGnuplotConfig(String directory, String fileName){
        try{
            System.out.println("Writing Gnuplot config file...");
            PrintWriter writer = new PrintWriter(directory+fileName+"/"+fileName+".p", "UTF-8");

            writer.println("# load '"+directory+fileName+"/"+fileName+".p'");
            writer.println("set xr [0.0:0.01]");
            writer.println("set yr [-1.0:11.0]");
            writer.println();
            writer.println("filePath = \""+directory+fileName+"/"+fileName+".dat\"");
            writer.println();
            writer.println("plot filePath using ($1):($2+3) title \"LEFT\" with lines, \\");
            writer.println("filePath using ($1):($3+2) title \"RIGHT\" with lines, \\");
            writer.println("filePath using ($1):($2-$3+1) title \"L-R\" with lines, \\");
            writer.println("filePath using ($1):($2+$3) title \"L+R\" with lines, \\");
            writer.println("filePath using 1:4 title \"TIME AVG ABS(L-R/L+R)\" with lines");



            writer.close();
            System.out.println("File "+fileName+".p written successfully.");
            System.out.println();

        } catch (IOException e) {
            System.out.println("File not written successfully.");
            System.out.println();
        }
    }
    public static void writeAVGGnuplotConfig(String directory, String fileName){
        try{
            System.out.println("Writing Gnuplot AVG config file...");
            PrintWriter writer = new PrintWriter(directory+fileName+"/"+fileName+"AVG.p", "UTF-8");

            writer.println("# load '"+directory+fileName+"/"+fileName+"AVG.p'");
            writer.println("set xr [0.0:100.0]");
            writer.println("set yr [-1.0:6.0]");
            writer.println();
            writer.println("filePath = \""+directory+fileName+"/"+fileName+"AVG.dat\"");
            writer.println();
            writer.println("plot filePath using ($1):($4+3) lt rgb \"black\" title \"LEFT\" with lines, \\");
            writer.println("filePath using ($1):(-$4+3) lt rgb \"black\" title \"\" with lines, \\");
            writer.println("filePath using ($1):($5+2) lt rgb \"red\" title \"RIGHT\" with lines, \\");
            writer.println("filePath using ($1):(-$5+2) lt rgb \"red\" title \"\" with lines, \\");
            writer.println("filePath using ($1):($4-$5+1) lt rgb \"green\" title \"L-R\" with lines, \\");
            writer.println("filePath using ($1):($4+$5) lt rgb \"orange\" title \"L+R\" with lines, \\");
            writer.println("filePath using ($1):(-($4+$5)) lt rgb \"orange\" title \"\" with lines, \\");
            writer.println("filePath using 1:7 lt rgb \"violet\" title \"TIME AVG ABS(L-R/L+R)\" with lines");



            writer.close();
            System.out.println("File "+fileName+".p written successfully.");
            System.out.println();

        } catch (IOException e) {
            System.out.println("File not written successfully.");
            System.out.println();
        }
    }
    public static void writeStats(String directory, String fileName, double[][] data){
        try{
            System.out.println("Writing STAT file...");
            PrintWriter writer = new PrintWriter(directory+fileName+"/"+fileName+"STAT.txt", "UTF-8");





            writer.close();
            System.out.println("File "+directory+fileName+"/"+fileName+"STAT.txt written successfully.");
            System.out.println();

        } catch (IOException e) {
            System.out.println("File not written successfully.");
            System.out.println();
        }

    }
    public double[][] getArrayStats(double[][] data){
        double[][] array = data;

        for (int i = 0; i < array.length; i++) {
            array[i][5] =
        }

        return data;
    }
    public static int getMaxLine(String file) throws IOException{

        FileReader fr = new FileReader(new File(file));
        LineNumberReader lnr = new LineNumberReader(fr);

        lnr.skip(Long.MAX_VALUE);
        int lines = lnr.getLineNumber();
        System.out.println("Max line count: "+lines);

        return lines;
    }
    public static String[] convertToStrings(String filePath) throws IOException{

        int maxLine = getMaxLine(filePath);

        FileReader flr = new FileReader(new File(filePath));
        BufferedReader br = new BufferedReader(flr);
        String[] fileLine = new String[maxLine];
        System.out.println("Reading in file lines...");
        for (int i = 0; i < maxLine; i++) {

            fileLine[i] = br.readLine();
            //System.out.println(fileLine[i]);

        }
        System.out.println("File read in successfully.");
        System.out.println();
        return fileLine;
    }
    public static double[][] parseStringFile(String[] file){

        String[] data;
        double[][] dataLine = new double[file.length][7];


        for (int i = 0; i < dataLine.length; i++) {
            data = file[i].split(" ");
            //System.out.println(Arrays.toString(data));

            int j = 0;
            int k = 0;
            while (j < data.length) {
                if (data[j].length() > 0) {
                    dataLine[i][k] = Double.parseDouble(data[j]);
                    k++;
                }
                j++;
            }



        }

        return dataLine;
    }
    public static double[][] setSumDiff(double[][] data){
        for (int i = 0; i < data.length; i++) {


            data[i][3] = data[i][1] - data[i][2];
            data[i][4] = data[i][1] + data[i][2];


        }
        return data;
    }
    public static double[][] setBounds(double[][] data, double lowerBound, double upperBound){
        for (int i = 0; i < data.length; i++) {

            //bounding error correction
            if (data[i][4] == 0.0) {
                data[i][5] = upperBound;
            } else if (data[i][3] == 0.0) {
                data[i][5] = lowerBound;
            } else {
                data[i][5] = Math.abs(data[i][3] / data[i][4]);
            }

        }
        return data;
    }
    public static double[][] throwOutliers(double[][] data, double upperBound){
        for (int i = 0; i < data.length; i++) {

            //ignore outliers - 95% of values are below 4 so we cast out everything above 4
            if (data[i][5] > upperBound) {
                data[i][5] = data[i - 1][5];
            }


        }
        return data;
    }
    public static double[][] setAverage(double[][] data, int averageRange){

        double sum = 0;
        for (int i = 0; i < data.length; i++) {



            //shift sum, remove previous first value and add latest
            if (i > averageRange) {
                sum -= data[i - averageRange - 1][5];
            }
            sum += data[i][5];

            data[i][6] = sum / averageRange;




        }
        return data;
    }
    double roundDown(double number, double place) {
        double result = number / place;
        result = Math.floor(result);
        result *= place;
        return result;
    }
}