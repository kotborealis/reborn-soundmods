package com.lc;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class Main {

    static private Charset charset = StandardCharsets.UTF_8;

    static private String game_path;
    static private String soundmods_content_path;


    public static void main(String[] args) throws IOException, InterruptedException {
        init_dota2_path();
        check_workshop_tools();
        create_dirs();
        update_gameinfo();
        resourcecompiler();
        copy_compiled_files();
        create_vpk();
    }

    private static void init_dota2_path() throws IOException, InterruptedException {
        Process process = process = Runtime.getRuntime().exec(
                "reg QUERY \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Steam App 570\" /v InstallLocation"
        );

        ByteArrayOutputStream sstdout = new ByteArrayOutputStream();
        copy(process.getInputStream(), sstdout);

        String stdout = new String(sstdout.toByteArray(), charset);
        process.waitFor();

        if(stdout.split("\n").length < 4){
            System.out.println("Unable to find Dota2 path.");
            System.exit(1);
        }


        String dota2_path = stdout.split("\n")[2].split("REG_SZ")[1].trim();
        game_path = Paths.get(dota2_path, "game").toString();

        String content_path = Paths.get(dota2_path, "content").toString();
        soundmods_content_path = Paths.get(content_path, "soundmods_content").toString() + File.separator;

        System.out.println("Found Dota2: " + dota2_path + "\n");
    }

    private static void check_workshop_tools(){
        String bin = Paths.get(game_path, "bin", "win64", "resourcecompiler.exe").toString();
        if(!new File(bin).exists()){
            System.out.println("Unable to find resourcecompiler.exe; install Dota 2 workshop tools --- https://i.imgur.com/pAXSp3H.png");
            System.exit(1);
        }
    }


    private static void create_dirs(){
        Path[] paths = {
            Paths.get(soundmods_content_path),
            Paths.get(game_path, "soundmods")
        };

        for(Path p : paths){
            System.out.println("Creating dir " + p.toString());
            File f = new File(p.toString());
            if(!f.exists()) {
                f.mkdir();
            }
        }
        System.out.println();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        while (true) {
            int c = in.read();
            if (c == -1) break;
            out.write((char)c);
        }
    }

    private static void resourcecompiler() throws IOException, InterruptedException {
        String input = soundmods_content_path + "*";
        String bin = Paths.get(game_path, "bin", "win64", "resourcecompiler.exe").toString();
        Process process = Runtime.getRuntime().exec(
                "\"" + bin + "\"" +
                        " -i " + "\"" + input + "\"" +
                        " -r "
        );
        copy(process.getInputStream(), System.out);
        process.waitFor();
        System.out.println();
    }

    private static void update_gameinfo() throws IOException {
        System.out.println("Updating gameinfo.gi\n");

        String addon_str = "\t\t\tGame\t\t\t\tsoundmods";

        Path path = Paths.get(game_path, "dota", "gameinfo.gi");
        String content = new String(Files.readAllBytes(path), charset);
        if(!content.contains(addon_str)) {
            content = content.replace(
                    "\t\t\tGame\t\t\t\tdota",
                    addon_str + "\n\t\t\tGame\t\t\t\tdota"
            );
        }
        Files.write(path, content.getBytes(charset));
    }

    private static void copy_compiled_files() throws IOException {
        System.out.println("Copying already compiled files");
        CopyFiles.copy(
                new File(soundmods_content_path),
                new File(Paths.get(game_path, "soundmods_content").toString())
        );
        System.out.println();
    }

    private static void create_vpk() throws IOException, InterruptedException {
        System.out.println("Creating vpk\n");

        String input = Paths.get(game_path, "soundmods_content").toString();
        String output_vpk = Paths.get(game_path, "soundmods_content.vpk").toString();
        String output = Paths.get(game_path, "soundmods", "pak01_dir.vpk").toString();

        String bin = Paths.get("./", "vpk.exe").toString();
        Process process = Runtime.getRuntime().exec(
                "\"" + bin + "\"" + " " + "\"" + input + "\""
        );
        copy(process.getInputStream(), System.out);
        process.waitFor();
        System.out.println();

        new File(output).delete();
        new File(output_vpk).renameTo(new File(output));
    }
}
