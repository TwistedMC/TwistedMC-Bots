package net.twistedmc.shield.bedwars;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class BedWars extends ListenerAdapter{

    private String token;
    private JDA jda;

    public BedWars(String token) {
        this.token = token;
    }

    public void start(){
        try {
            this.jda = JDABuilder.createDefault(token).build();
            jda.addEventListener(this);
            //jda.getPresence().setPresence(Activity.watching("for cheaters"), false);
            System.out.println("[BED WARS] Starting BED WARS bot..");
        } catch (LoginException err) {
            System.out.println("[BED WARS] Failed to start BED WARS Bot!");
        }
    }

    public void stop(){
        this.jda.shutdown();
    }

}