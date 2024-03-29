package io.github.dantetam.world.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import io.github.dantetam.world.entity.City;
import io.github.dantetam.world.entity.CityState;
import io.github.dantetam.world.entity.Clan;
import io.github.dantetam.xml.ClanXmlParser;
import io.github.dantetam.opstrykontest.LessonSevenActivity;
import io.github.dantetam.opstrykontest.R;
import io.github.dantetam.utilmath.Vector4f;

/**
 * Created by Dante on 7/16/2016.
 */
public class ClanFactory {

    private static LessonSevenActivity mActivity;

    public static HashMap<Clan.ClanType, List<Vector4f>> clanTypeColorSchemes;
    public static HashMap<Clan.ClanFaction, List<Vector4f>> clanFactionColorSchemes;

    public static ClanXmlParser parser;

    public static HashSet<String> usedClans, usedCityStates;

    //public static HashMap<String, Clan> civilizationAi;

    public static void init(LessonSevenActivity context) {
        mActivity = context;

        clanTypeColorSchemes = new HashMap<>();

        List<Vector4f> list = new ArrayList<>();
        list.add(new Vector4f(1f,0,0,1f));
        list.add(new Vector4f(0.5f,0,0,1f));
        list.add(new Vector4f(0.5f,0,0.5f,1f));
        list.add(new Vector4f(0, 0, 0, 1f));
        list.add(new Vector4f(0.25f, 0, 0.1f, 1f));
        clanTypeColorSchemes.put(Clan.ClanType.CLAN_AGGRESSIVE, list);

        list = new ArrayList<>();
        list.add(new Vector4f(1f,0.85f,0,1f));
        list.add(new Vector4f(0.5f,0.3f,0,1f));
        list.add(new Vector4f(0.75f,0.25f,0,1f));
        list.add(new Vector4f(0.5f, 0.5f, 0.5f, 1f));
        list.add(new Vector4f(0.75f, 0.75f, 0.75f, 1f));
        clanTypeColorSchemes.put(Clan.ClanType.CLAN_INDUSTRIOUS, list);

        list = new ArrayList<>();
        list.add(new Vector4f(0,1f,0,1f));
        list.add(new Vector4f(0,0.5f,0.5f,1f));
        list.add(new Vector4f(0, 0.5f, 0, 1f));
        list.add(new Vector4f(0.6f, 0.1f, 0.2f, 1f));
        clanTypeColorSchemes.put(Clan.ClanType.CLAN_SETTLER, list);

        list = new ArrayList<>();
        list.add(new Vector4f(0,0,1f,1f));
        list.add(new Vector4f(0.5f,0f,0.5f,1f));
        list.add(new Vector4f(0, 0.25f, 0.25f, 1f));
        list.add(new Vector4f(0f, 0f, 0.25f, 1f));
        clanTypeColorSchemes.put(Clan.ClanType.CLAN_TRADITIONAL, list);

        clanFactionColorSchemes = new HashMap<>();

        list = new ArrayList<>();
        list.add(new Vector4f(0,0,0,1f));
        list.add(new Vector4f(0.2f,0.2f,0.2f,1f));
        list.add(new Vector4f(0.5f, 0.5f, 0.5f, 1f));
        list.add(new Vector4f(0.5f, 0.25f, 0.25f, 1f));
        list.add(new Vector4f(0.25f, 0, 0, 1f));
        clanFactionColorSchemes.put(Clan.ClanFaction.FACTION_BARBARIAN, list);

        list = new ArrayList<>();
        list.add(new Vector4f(0.5f,0,0,1f));
        list.add(new Vector4f(0.75f,0.75f,0.75f,1f));
        list.add(new Vector4f(0.8f, 0.8f, 1f, 1f));
        list.add(new Vector4f(0.4f, 0.5f, 0.6f, 1f));
        clanFactionColorSchemes.put(Clan.ClanFaction.FACTION_CIVILIZED, list);

        list = new ArrayList<>();
        list.add(new Vector4f(0,1f,0,1f));
        list.add(new Vector4f(0, 0.8f, 0f, 1f));
        list.add(new Vector4f(0, 0.6f, 0.2f, 1f));
        clanFactionColorSchemes.put(Clan.ClanFaction.FACTION_FOREIGNER, list);

        list = new ArrayList<>();
        list.add(new Vector4f(1f, 0, 0, 1f));
        list.add(new Vector4f(1f,0.5f,0.5f,1f));
        list.add(new Vector4f(0.2f, 0.3f, 0.2f, 1f));
        clanFactionColorSchemes.put(Clan.ClanFaction.FACTION_SAVAGE, list);

        parser = new ClanXmlParser();
        parser.parseAllClans(mActivity, R.raw.clan_flavors);

        usedClans = new HashSet<>();
        usedCityStates = new HashSet<>();
    }

    public static Clan getClanByName(String name) {
        Clan ai = parser.clans.get(name);
        return newClan(ai);
    }

    public static Clan randomAvailableClan() {
        Clan clan = null;
        while (clan == null || usedClans.contains(clan.name)) {
            String key = parser.clanKeys[(int)(Math.random()*parser.clanKeys.length)];
            Clan randomAi = parser.clans.get(key);

            clan = newClan(randomAi);
            if (usedClans.size() >= parser.clanKeys.length) {
                return clan;
            }
        }
        usedClans.add(clan.name);
        return clan;
    }

    public static Clan newClan(Clan aiType) {
        Clan.ClanType clanType = Clan.ClanType.random();
        Clan.ClanFaction clanFaction  = Clan.ClanFaction.random();
        List<Vector4f> colors = clanTypeColorSchemes.get(clanType);
        List<Vector4f> secondaryColors = clanFactionColorSchemes.get(clanFaction);
        Vector4f primaryColor, secondaryColor;
        if (colors.size() == 0 || secondaryColors.size() == 0) {
            return null;
        }
        else {
            primaryColor = colors.get((int)(Math.random()*colors.size())).scaled(255f);
            secondaryColor = secondaryColors.get((int)(Math.random()*secondaryColors.size())).scaled(255f);
        }
        /*switch (type) {
            case 0:
                clanType.
                break;
            default:
                System.err.println("Invalid clan type: " + type);
                return null;
        }*/
        aiType.color = primaryColor;
        aiType.reducedColor = primaryColor.scaled(0.7f);
        aiType.secondaryColor = secondaryColor;
        aiType.reducedSecondaryColor = secondaryColor.scaled(0.7f);
        return aiType;
    }

    public static CityState randomAvailableCityState() {
        CityState clan = null;
        while (clan == null || usedCityStates.contains(clan.name)) {
            System.out.println(parser.cityStateKeys.length);

            String key = parser.cityStateKeys[(int)(Math.random()*parser.cityStateKeys.length)];
            CityState randomAi = parser.cityStates.get(key);

            clan = newCityState(randomAi);
            if (usedCityStates.size() >= parser.cityStateKeys.length) {
                return clan;
            }
        }
        usedCityStates.add(clan.name);
        return clan;
    }

    public static CityState newCityState(CityState aiType) {
        if (aiType == null) {
            return null;
        }

        Clan.ClanType clanType = Clan.ClanType.random();
        Clan.ClanFaction clanFaction  = Clan.ClanFaction.random();
        List<Vector4f> colors = clanTypeColorSchemes.get(clanType);
        List<Vector4f> secondaryColors = clanFactionColorSchemes.get(clanFaction);
        Vector4f primaryColor, secondaryColor;
        if (colors.size() == 0 || secondaryColors.size() == 0) {
            return null;
        }
        else {
            primaryColor = colors.get((int)(Math.random()*colors.size())).scaled(255f);
            secondaryColor = secondaryColors.get((int)(Math.random()*secondaryColors.size())).scaled(255f);
        }

        aiType.clanType = clanType;
        aiType.clanFaction = clanFaction;
        /*switch (type) {
            case 0:
                clanType.
                break;
            default:
                System.err.println("Invalid clan type: " + type);
                return null;
        }*/
        aiType.color = primaryColor;
        aiType.reducedColor = primaryColor.scaled(0f);
        aiType.secondaryColor = secondaryColor;
        aiType.reducedSecondaryColor = secondaryColor.scaled(0f);
        return aiType;
    }

    /*public static Clan newClan(Clan.ClanType clanType, Clan.ClanFaction clanFaction, Clan aiType) {
        *//*Clan.ClanType clanType;
        Clan.ClanFaction clanFaction;*//*
        List<Vector4f> colors = clanTypeColorSchemes.get(clanType);
        List<Vector4f> secondaryColors = clanFactionColorSchemes.get(clanFaction);
        Vector4f primaryColor, secondaryColor;
        if (colors.size() == 0 || secondaryColors.size() == 0) {
            return null;
        }
        else {
            primaryColor = colors.get((int)(Math.random()*colors.size())).scaled(255f);
            secondaryColor = secondaryColors.get((int)(Math.random()*secondaryColors.size())).scaled(255f);
        }
        *//*switch (type) {
            case 0:
                clanType.
                break;
            default:
                System.err.println("Invalid clan type: " + type);
                return null;
        }*//*
        Clan clan = new Clan(aiType.name);
        clan.techTree = aiType.techTree;
        clan.ai = aiType.ai;
        clan.color = primaryColor;
        clan.reducedColor = primaryColor.scaled(0.7f);
        clan.secondaryColor = secondaryColor;
        clan.reducedSecondaryColor = secondaryColor.scaled(0.7f);
        clan.clanType = clanType;
        clan.clanFaction = clanFaction;
        clan.cityNames = aiType.cityNames;
        return clan;
    }*/

}
