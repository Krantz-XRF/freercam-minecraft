package net.minecraft.client.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;

import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.Session;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Main {
    public static void main(String[] args) {
        OptionParser optionparser = new OptionParser();
        optionparser.allowsUnrecognizedOptions();
        optionparser.accepts("demo");
        optionparser.accepts("fullscreen");
        optionparser.accepts("checkGlErrors");

        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec1 = optionparser.accepts("server").withRequiredArg();

        ArgumentAcceptingOptionSpec<Integer> argumentAcceptingOptionSpec2 = optionparser.accepts("port").withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(25565);

        ArgumentAcceptingOptionSpec<File> argumentAcceptingOptionSpec3 = optionparser.accepts("gameDir").withRequiredArg()
                .ofType(File.class)
                .defaultsTo(new File("."));

        ArgumentAcceptingOptionSpec<File> argumentAcceptingOptionSpec4 = optionparser.accepts("assetsDir").withRequiredArg()
                .ofType(File.class);

        ArgumentAcceptingOptionSpec<File> argumentAcceptingOptionSpec5 = optionparser.accepts("resourcePackDir").withRequiredArg()
                .ofType(File.class);

        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec6 = optionparser.accepts("proxyHost").withRequiredArg();

        ArgumentAcceptingOptionSpec<Integer> argumentAcceptingOptionSpec7 = optionparser.accepts("proxyPort").withRequiredArg()
                .defaultsTo("8080", new String[0])
                .ofType(Integer.class);

        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec8 = optionparser.accepts("proxyUser").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec9 = optionparser.accepts("proxyPass").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec10 = optionparser.accepts("username").withRequiredArg()
                .defaultsTo("dodon");


        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec11 = optionparser.accepts("uuid").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec12 = optionparser.accepts("accessToken").withRequiredArg().required();
        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec13 = optionparser.accepts("version").withRequiredArg().required();

        ArgumentAcceptingOptionSpec<Integer> argumentAcceptingOptionSpec14 = optionparser.accepts("width").withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(854);

        ArgumentAcceptingOptionSpec<Integer> argumentAcceptingOptionSpec15 = optionparser.accepts("height").withRequiredArg()
                .ofType(Integer.class)
                .defaultsTo(480);
        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec16 = optionparser.accepts("userProperties").withRequiredArg()
                .defaultsTo("{}");

        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec17 = optionparser.accepts("profileProperties").withRequiredArg()
                .defaultsTo("{}");

        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec18 = optionparser.accepts("assetIndex").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec19 = optionparser.accepts("userType").withRequiredArg()
                .defaultsTo("legacy");

        ArgumentAcceptingOptionSpec<String> argumentAcceptingOptionSpec20 = optionparser.accepts("versionType").withRequiredArg()
                .defaultsTo("release");

        NonOptionArgumentSpec<String> nonOptionArgumentSpec = optionparser.nonOptions();
        OptionSet optionset = optionparser.parse(args);
        List<String> list = optionset.valuesOf(nonOptionArgumentSpec);

        if (!list.isEmpty()) {
            System.out.println("Completely ignored arguments: " + list);
        }

        String s = optionset.valueOf(argumentAcceptingOptionSpec6);
        Proxy proxy = Proxy.NO_PROXY;

        if (s != null) {

            try {
                proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(s, optionset.valueOf(argumentAcceptingOptionSpec7)));
            } catch (Exception ignored) {
            }
        }


        final String s1 = optionset.valueOf(argumentAcceptingOptionSpec8);
        final String s2 = optionset.valueOf(argumentAcceptingOptionSpec9);

        if (!proxy.equals(Proxy.NO_PROXY) && StringUtils.isNullOrEmpty(s1) && StringUtils.isNullOrEmpty(s2)) {
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(s1, s2.toCharArray());
                }
            });
        }

        int i = optionset.valueOf(argumentAcceptingOptionSpec14);
        int j = optionset.valueOf(argumentAcceptingOptionSpec15);
        boolean flag = optionset.has("fullscreen");
        boolean flag1 = optionset.has("checkGlErrors");
        boolean flag2 = optionset.has("demo");
        String s3 = optionset.valueOf(argumentAcceptingOptionSpec13);
        Gson gson = (new GsonBuilder()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create();
        PropertyMap propertymap = JsonUtils.gsonDeserialize(gson, optionset
                .valueOf(argumentAcceptingOptionSpec16), PropertyMap.class);
        PropertyMap propertymap1 = JsonUtils.gsonDeserialize(gson, optionset
                .valueOf(argumentAcceptingOptionSpec17), PropertyMap.class);
        String s4 = optionset.valueOf(argumentAcceptingOptionSpec20);
        File file1 = optionset.valueOf(argumentAcceptingOptionSpec3);
        File file2 = optionset.has(argumentAcceptingOptionSpec4) ? optionset.valueOf(argumentAcceptingOptionSpec4) : new File(file1, "assets/");
        File file3 = optionset.has(argumentAcceptingOptionSpec5) ? optionset.valueOf(argumentAcceptingOptionSpec5) : new File(file1, "resourcepacks/");


        String s5 = optionset.has(argumentAcceptingOptionSpec11) ? argumentAcceptingOptionSpec11.value(optionset) : argumentAcceptingOptionSpec10.value(optionset);
        String s6 = optionset.has(argumentAcceptingOptionSpec18) ? argumentAcceptingOptionSpec18.value(optionset) : null;
        String s7 = optionset.valueOf(argumentAcceptingOptionSpec1);
        Integer integer = optionset.valueOf(argumentAcceptingOptionSpec2);

        Session session = new Session(
                argumentAcceptingOptionSpec10.value(optionset),
                s5,
                argumentAcceptingOptionSpec12.value(optionset),
                argumentAcceptingOptionSpec19.value(optionset));


        //noinspection ConstantConditions
        GameConfiguration gameconfiguration = new GameConfiguration(
                new GameConfiguration.UserInformation(session, propertymap, propertymap1, proxy),
                new GameConfiguration.DisplayInformation(i, j, flag, flag1),
                new GameConfiguration.FolderInformation(file1, file3, file2, s6),
                new GameConfiguration.GameInformation(flag2, s3, s4),
                new GameConfiguration.ServerInformation(s7, integer));

        Runtime.getRuntime().addShutdownHook(new Thread("Client Shutdown Thread") {
            public void run() {
                Minecraft.stopIntegratedServer();
            }
        });

        Thread.currentThread().setName("Client thread");

        (new Minecraft(gameconfiguration)).run();
    }
}
