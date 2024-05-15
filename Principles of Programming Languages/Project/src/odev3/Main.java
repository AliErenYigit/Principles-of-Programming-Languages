/**
*
* @author Ali eren yiğit eren.yigit@ogr.sakarya.edu.tr
* @since 01.04.2024
* <p>B201210062
* Repository urlsinden git deposunu klonlayıp üzerinde analizler yapıp çıktı veren kod
* </p>
*/

package odev3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {


	public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("GitHub Repository URL girin:");
            String repoUrl = reader.readLine();
            
            // Depoyu klonla
            String repoPath = cloneRepository(repoUrl);

            if (repoPath == null) {
                System.out.println("Repository zaten kopyalanmış. Görüntülemek ister misiniz?(lütfen evet giriniz)");
    	        String answer = reader.readLine().trim().toLowerCase();
    	        if (!answer.equals("evet")) 
    	        	return ;
    	        
    	        repoPath = repoUrl.split("/")[repoUrl.split("/").length-1];
            }

            // *.java dosyalarını al
        	List<File> emptyfiles = new ArrayList<>();
            List<File> javaFiles = getJavaFiles(repoPath,emptyfiles);
            // Her java dosyası için analiz yap
            for (File javaFile : javaFiles) {
                analyzeJavaFile(javaFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String cloneRepository(String repoUrl) throws IOException {
        File directory = new File(repoUrl.substring(repoUrl.lastIndexOf('/') + 1));
        System.out.println("Dosya Yolu: " + directory.getAbsolutePath());
        if (directory.exists() && directory.isDirectory()) {
            return null; // Kopyalanmış repository bulundu
        }

        ProcessBuilder builder = new ProcessBuilder("git", "clone", repoUrl);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        return repoUrl.substring(repoUrl.lastIndexOf('/') + 1);
    }
    private static boolean isFileinArray(List<File> arr,String name) {
    	for(File file : arr) {
    		if (file.getName().equals(name)) {
    			return true;
    		}
    	}
    	return false;
    }
    private static List<File> getJavaFiles(String repoPath,List<File> empty_List) {
       // javaFiles = 
    	
        File directory = new File(repoPath);
        File[] files1 = directory.listFiles();
        List<File> java_files = new  ArrayList<>();
        if(empty_List.size() != 0) {
        	for(File file : empty_List) {
        		if(isFileinArray(java_files,file.getName()) == false)
        			java_files.add(file);
        	}
        }

        if (files1 != null) {
            for (File file : files1) {
            	if(file.isDirectory()) {
            		for(File file_inner : getJavaFiles(repoPath + "/" + file.getName(),java_files)) {
            			if(isFileinArray(java_files,file_inner.getName()) == false)
                			java_files.add(file_inner);
            		}
            	}else {
            		if(file.getName().endsWith(".java")) {
            			if(isFileinArray(java_files,file.getName()) == false)
                			java_files.add(file);
            		}
            			
            	}
            }
        } else {
            System.out.println("Dizin boş veya erişim izni yok.");
        }
      /*  File[] files = directory.listFiles((dir, name) -> name.endsWith(".java"));
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    javaFiles.add(file);
                }
            }
        }*/
        return java_files;
    }

    private static void analyzeJavaFile(File javaFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Sadece sınıf tanımları üzerinde işlem yap
                if (line.trim().startsWith("public class") || line.trim().startsWith("class")) {
                	System.out.println("Java Dosyası: " + javaFile.getName());
                	BufferedReader reader_new = new BufferedReader(new FileReader(javaFile));
                    analyzeClass(reader_new);
                	reader.close();
                    break; // Sadece bir sınıfı işle
                }
            }
        } catch (IOException e) {
        	
            e.printStackTrace();
        }
    }

    private static void analyzeClass(BufferedReader reader) throws IOException {
        String line;
        int javadocLineCount = 0;
        int otherCommentLineCount = 0;
        int codeLineCount = 0;
        int totalLineCount = 0;
        int functionCount = 0;
        boolean inClass = false;
        int inBlockComment = 0;
        
        while ((line = reader.readLine()) != null) {
        	   line = line.trim();
        	 //  System.out.println("line: " + line);
        	   totalLineCount++;
        	   

        	   if(inBlockComment > 0) {
        		   if(line.startsWith("*/"))
        		   {
        			   inBlockComment = 0;
        		   }
        		   else {
        			   if (inBlockComment == 2)
        				   javadocLineCount++;
        			   else
        				   otherCommentLineCount++;
        		   }
        	   }else {
        		   if (line.startsWith("/**"))
        			   inBlockComment = 2;
        		   else if (line.startsWith("/*"))
        			   inBlockComment = 1;
        		   else if(line.startsWith("public class") || line.startsWith("private class"))
        			   inClass=true;
        		   else if (line.startsWith("//"))
        			   otherCommentLineCount++;
        		   else if(inClass == true) {
        			   codeLineCount++;
        		   }
        	   }
        	   if (line.startsWith("public") || line.startsWith("private") || line.startsWith("protected") || line.startsWith("static")) {
        		   if (line.endsWith("{") && !line.contains("class")) {
                    functionCount++;
        		   }
        	   }
        }

        // Yorum satırları dışında her şeyin satır sayısı
        int loc = totalLineCount;

        // Yorum sapma yüzdesi hesapla
        double YG=((javadocLineCount+otherCommentLineCount)*0.8)/functionCount;
        double YH=(codeLineCount/functionCount)*0.3;
        double commentDeviationPercentage=((100*YG)/YH)-100;
      //  double expectedCommentCount = 0.1 * loc; // Genelde %10'u kadar yorum satırı önerilir
       // double commentDeviationPercentage = ((javadocLineCount + otherCommentLineCount) - expectedCommentCount) / expectedCommentCount * 100;

        // Sonuçları yazdır
        
        System.out.println("Javadoc Yorum Satır Sayısı: " + javadocLineCount);
        System.out.println("Diğer Yorum Satır Sayısı: " + otherCommentLineCount);
        System.out.println("Kod Satır Sayısı: " + codeLineCount);
        System.out.println("LOC (Line of Code): " + loc);
        System.out.println("Fonksiyon Sayısı: " + functionCount);
        System.out.println("Yorum Sapma Yüzdesi: " + "%" + commentDeviationPercentage );
        System.out.println("---------------------------------------------");
    }
}