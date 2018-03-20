package com.lc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        create_dirs();
        resourcecompiler();
        gameinfo();
        create_vpk();
    }

    private static String dota2_path(){
        // TODO
        return "D:\\SteamLibrary\\steamapps\\common\\dota 2 beta";
    }

    static private String game_path = Paths.get(dota2_path(), "game").toString();
    static private String content_path = Paths.get(dota2_path(), "content").toString();

    private static void create_dirs(){
        Path[] paths = {
            Paths.get(content_path, "soundmods_content"),
            Paths.get(game_path, "soundmods")
        };

        for(Path p : paths){
            System.out.println("Creating dir " + p.toString());
            File f = new File(p.toString());
            if(!f.exists()) {
                f.mkdir();
            }
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        while (true) {
            int c = in.read();
            if (c == -1) break;
            out.write((char)c);
        }
    }

    private static void resourcecompiler() throws IOException, InterruptedException {
        String input = Paths.get(content_path, "soundmods_content").toString() + File.separator + "*";
        System.out.println(input);
        String bin = Paths.get(game_path, "bin", "win64", "resourcecompiler.exe").toString();
        Process process = Runtime.getRuntime().exec(
                "\"" + bin + "\"" +
                        " -i " + "\"" + input + "\"" +
                        " -r "
        );
        copy(process.getInputStream(), System.out);
        process.waitFor();
    }

    private static void gameinfo() throws IOException {
        Path path = Paths.get(game_path, "dota", "gameinfo.gi");
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(path), charset);
        content = content.replace(
                "\t\t\tGame\t\t\t\tdota",
                "\t\t\tGame\t\t\t\tsoundmods\n\t\t\tGame\t\t\t\tdota"
        );
        Files.write(path, content.getBytes(charset));
    }

    private static void create_vpk() throws IOException, InterruptedException {
        String input = Paths.get(game_path, "soundmods_content").toString();
        String output_vpk = Paths.get(game_path, "soundmods_content.vpk").toString();
        String output = Paths.get(game_path, "soundmods", "pak01_000.vpk").toString();

        String bin = Paths.get("./", "vpk.exe").toString();
        Process process = Runtime.getRuntime().exec(
                "\"" + bin + "\"" + " " + "\"" + input + "\""
        );
        copy(process.getInputStream(), System.out);
        process.waitFor();

        new File(output_vpk).renameTo(new File(output));
    }
}
