
import java.util.*;

import javax.annotation.processing.Filer;

import java.io.*;;
/*
 * Linux File Command
 * search by size and name/name pattern
 * size: >=, <=, >, <, ==
 * name: equals, contains, startsWith, endsWith
 * root/
 * dir1/
 *  file1
 *  file2
 * dir2/
 *  file3
 *  file4
 * file5
 * traverse through directories using dfs
 */

/*

public interface FileSystem {
    public void ls();
}
 
public class Directory implements FileSystem {

    String directoryName;
    List<FileSystem> fileSystemList;

    public Directory(String name){
        this.directoryName = name;
        fileSystemList = new ArrayList<>();
    }

    public void add(FileSystem fileSystemObj) {
        fileSystemList.add(fileSystemObj);
    }

    public void ls(){
        System.out.println("Directory name " + directoryName);

        for(FileSystem fileSystemObj : fileSystemList){
            fileSystemObj.ls();
        }
    }
}

public class File implements FileSystem{
    String fileName;

    public File(String name){
        this.fileName = name;
    }

    public void ls(){
        System.out.println("file name " + fileName);
    }
}

public class Main {
    public static void main(String args[]){

    Directory movieDirectory = new Directory("Movie");

    FileSystem border = new File("Border");
    movieDirectory.add(border);

    Directory comedyMovieDirectory = new Directory("ComedyMovie");
    File hulchul = new File("Hulchul");
    comedyMovieDirectory.add(hulchul);
    movieDirectory.add(comedyMovieDirectory);

    movieDirectory.ls();

    }
}
 */


interface FileSystem {
    public void ls();
}

class File implements FileSystem {
    private final String name;

    File(String name) {
        this.name = name;
    }

    // public void setString(String name) {
    //     this.name = name;
    // }

    // public String getString() {
    //     return this.name;
    // }

    @Override
    public void ls() {
        System.out.println(this.name);
    }

    public String getName() {
        return this.name;
    }
}

class Directory implements FileSystem {
    private List<FileSystem> fileSystems;

    Directory(List<FileSystem> files) {
        this.fileSystems = files;
    }


    @Override
    public void ls() {
       for (FileSystem file: this.fileSystems) {
        System.out.println("Printing the directory");
        file.ls();
       }
    }
    
}

interface FileRule {
    public boolean match(File f);
}

class FileRuleExtension implements FileRule {
    final private String ext;
    FileRuleExtension (String ext) {
        this.ext = ext;
    }

    @Override
    public boolean match(File f) {
        return f.getName().endsWith("." + this.ext);
    }
}

class FileRuleSize implements FileRule {
    enum FileRuleSizeOp {
        LT, GT, EQ, GTE, LTE
    }

    private final int size;
    private final FileRuleSizeOp op;

    FileRuleSize(int size, FileRuleSizeOp op) {
        this.size = size;
        this.op = op;
    }

    @Override
    public boolean match(File f) {
       switch (this.op) {
        case LT:
            return f.length() < size;
        default:
            return false;
       }
    }   
}

class FileRuleAnd implements FileRule {
    private final List<FileRule> rules;

    FileRuleAnd(List<FileRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean match(File f) {
        for (FileRule rule: rules) {
            if (!rule.match(f)) {
                return false;
            }
        }
        return true;
}
}

class FileRuleOr implements FileRule {
    private final List<FileRule> rules;

    FileRuleOr(List<FileRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean match(File f) {
        for (FileRule rule: rules) {
            if (rule.match(f)) {
                return true;
            }
        }
        return false;
}
    
}

public class FindFile {

    public static void find(File baseDir, FileRule rule) {
        if (baseDir.isDirectory()) {
            for (File f: baseDir.listFiles()) {
                if (f.isDirectory()) {
                    find(f, rule);
                }else {
                    if (rule.match((f))) {
                        System.out.println(f.getName());
                    }
                }
            }
        } else {
            if (rule.match((baseDir))) {
                System.out.println(baseDir.getName());
            }
        }
    }
    
    public static void main(String[] args) {
        
    }
}