package com.cffex.sc;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mojo(name="count")
public class SourceCounterMojo extends AbstractMojo {

    private static final String[] INLUDES_DEFAULTS={"java","xml","properties"};

    @Parameter(property = "baseDir",defaultValue="${project.basedir}")
    private File baseDir;

    @Parameter(property = "sourceDir",defaultValue="${project.build.sourceDirectory}")
    private File sourceDir;

    @Parameter(property = "testSourceDir",defaultValue="${project.build.testSourceDirectory}")
    private File testSourceDir;

    @Parameter(property = "resources",defaultValue="${project.build.resources}")
    private List<Resource> resources;

    @Parameter(property = "testResources",defaultValue="${project.build.testResources}")
    private List<Resource> testResources;

    @Parameter(property = "includes")
    private String[] includes;

    public void execute() throws MojoExecutionException {

        if(includes==null || includes.length==0){
            includes=INLUDES_DEFAULTS;
        }
        try{
            countDir(sourceDir);
            countDir(testSourceDir);
            for(Resource resource:resources){
                countDir(new File(resource.getDirectory()));
            }
            for(Resource resource:testResources){
                countDir(new File(resource.getDirectory()));
            }

        }catch (IOException e){
            throw new MojoExecutionException("Unable to count the lines of code.",e);
        }
    }

    private void countDir(File dir) throws IOException {
        if(!dir.exists()) return;

        List<File> collected=new ArrayList<File>();
        collectFiles(collected,dir);
        int lines=0;

        for(File sourceFile:collected){
            lines+=countLines(sourceFile);
        }

        String path=dir.getAbsolutePath().substring(baseDir.getAbsolutePath().length());
        getLog().info(path+":"+lines+" lines of code in "+collected.size()+"files");

    }

    private void collectFiles(List<File> collected, File file){
        if(file.isFile()){
            for(String include:includes){
                if(file.getName().endsWith("."+include)){
                    collected.add(file);
                    break;
                }
            }
        }else {
            for(File sub:file.listFiles()){
                collectFiles(collected,sub);
            }
        }
    }
    private int countLines(File file) throws IOException{
        BufferedReader reader= new BufferedReader(new FileReader(file));
        int line=0;
        try{
            while(reader.ready()){
                reader.readLine();
                line++;
            }
        }finally {
            reader.close();
        }
        return  line;
    }
}
