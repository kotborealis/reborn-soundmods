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
import java.util.Objects;

public class Main {

    static private Charset charset = StandardCharsets.UTF_8;
    static private String base_vsndevts;


    public static void main(String[] args) throws IOException, InterruptedException {
        base_vsndevts = new String(Files.readAllBytes(Paths.get("./base.vsndevts")), charset);

        create_dirs();
        generate_vsndevts();
        resourcecompiler();
        gameinfo();
        create_vpk();
    }

    private static String dota2_path(){
        // TODO
        return Paths.get("D:\\SteamLibrary\\steamapps\\common\\dota 2 beta").toString();
    }
    static private String game_path = Paths.get(dota2_path(), "game").toString();
    static private String content_path = Paths.get(dota2_path(), "content").toString();
    static private String soundmods_content_path = Paths.get(content_path, "soundmods_content").toString() + File.separator;


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
        System.out.println(soundmods_content_path + "*");
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

    private static void gameinfo() throws IOException {
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

    private static void create_vpk() throws IOException, InterruptedException {
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

    private static void generate_vsndevts() throws IOException {
        Files.walk(Paths.get(soundmods_content_path, "sounds"))
                .filter(Files::isRegularFile)
                .forEach(Main::generate_vsndevts);
        System.out.println();
    }

    private static void generate_vsndevts(Path path) {
        String r_path = Paths.get(soundmods_content_path).relativize(path).toString();
        String name = r_path.substring(0, r_path.lastIndexOf('.'));
        String ext  = r_path.substring(r_path.lastIndexOf('.'));

        if(!Objects.equals(ext, ".wav")){
            System.out.println("Skipping non-wav file: " + r_path);
            return;
        }

        Path vsndevts_path = Paths.get(soundmods_content_path, name + ".vsndevts");
        if(new File(vsndevts_path.toString()).exists()){
            System.out.println("Skipping existing vsndevts file: " + vsndevts_path);
            return;
        }

        String content = base_vsndevts;
        content = content.replace("$$PATH$$", r_path.replaceAll("\\\\", "/"));
        try {
            Files.write(vsndevts_path, content.getBytes(charset));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
