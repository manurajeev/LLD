
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

// class File {
//     private String name;
//     private String size;
// }

// class Directory {
//     List<Directory> directories;
// }

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