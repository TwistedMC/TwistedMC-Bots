package net.twistedmc.shield.SHIELD;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;

public class SHIELD extends ListenerAdapter{

    private String token;
    private JDA jda;

    public SHIELD(String token) {
        this.token = token;
    }

    public void start(){
        try {
            this.jda = JDABuilder.createDefault(token).build();
            jda.addEventListener(this);
            jda.getPresence().setPresence(Activity.watching("for cheaters"), false);
            System.out.println("[SHIELD] Starting SHIELD bot..");
        } catch (LoginException err) {
            System.out.println("[SHIELD] Failed to start SHIELD Bot!");
        }
    }

    public void stop(){
        this.jda.shutdown();
    }

}