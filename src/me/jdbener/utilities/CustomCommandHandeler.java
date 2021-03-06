package me.jdbener.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.ho.yaml.Yaml;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import me.jdbener.Bennerbot;
import me.jdbener.moderataion.FilterManager;

@SuppressWarnings("unused")
public class CustomCommandHandeler extends ListenerAdapter<PircBotX>{
	public CustomCommandHandeler(){
		setupReplacementTable();
		update();
	}
	
	public void update(){
		try {
			@SuppressWarnings("unchecked")
			Map<String, String> temp = (Map<String, String>) Yaml.load(new FileInputStream(new File("config/commands.yml")));
			
			for (Entry<String, String> entry : temp.entrySet()){
				if(entry.getKey().toString().startsWith("~")){
					Bennerbot.variableMap.put("<"+entry.getKey().replace("~", "")+">", entry.getValue());
				} else {
					Bennerbot.commandMap.put(entry.getKey(), entry.getValue());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void setupReplacementTable(){
		Bennerbot.variableMap.put("<botname>", Bennerbot.name);
		Bennerbot.variableMap.put("<botversion>", Bennerbot.version);
		Bennerbot.variableMap.put("<twitchhost>", Bennerbot.conf.get("twitchChannel").toString().toLowerCase().trim());
		Bennerbot.variableMap.put("<hitboxhost>", Bennerbot.conf.get("hitboxChannel").toString().toLowerCase().trim());
		Bennerbot.variableMap.put("<user>", "The username of the person who sent the message");
		Bennerbot.variableMap.put("<User>", "The username of the person who sent the message, capitalized");
	}

	public void onMessage(MessageEvent<PircBotX> e) throws Exception {
		update();
		Bennerbot.variableMap.put("<username>", e.getUser().getNick());
		Bennerbot.variableMap.put("<Username>", Bennerbot.capitalize(e.getUser().getNick()));
		Bennerbot.variableMap.put("<user>", e.getUser().getNick());
		Bennerbot.variableMap.put("<User>", Bennerbot.capitalize(e.getUser().getNick()));
		for (Entry<String, String> entry : Bennerbot.commandMap.entrySet()){
			if(e.getMessage().startsWith("!"+entry.getKey())){
				Bennerbot.sendMessage(replaceVariables(entry.getValue()));
			}
		}
		if(e.getMessage().equalsIgnoreCase("!commands")){
			Runnable run = new Runnable(){
				@Override
				public void run() {
					String out = "";
					int i = 0;
					for(Entry<String, String> entry: Bennerbot.commandMap.entrySet()){
						if(i != 0){
							out+=(i+") !"+entry.getKey()+"   ");
						}
						i++;
					}
					Bennerbot.sendMessage(out);
				}
			};
			
			Executors.newScheduledThreadPool(2).execute(run);
		}
	}
	/**
	 * This function will replace any variables in the string that is passed to it, with there appropriate values.
	 */
	public static String replaceVariables(String in){
		//irate through all of the variables
		for (Map.Entry<String, String> entry : Bennerbot.variableMap.entrySet()) {
	        //replace any occurrences of the variable
			in = in.replaceAll(entry.getKey(), entry.getValue());
	    }
		return in;
	}
}
