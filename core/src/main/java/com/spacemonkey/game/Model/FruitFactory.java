package com.spacemonkey.game.Model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;


public class FruitFactory {
    private Map<String, Integer> typeChances;
    private Random random;
    public FruitFactory()
    {
        typeChances = new LinkedHashMap<>();
        random = new Random();
    }



    public String getRandomType(Map<String, Integer> chances) {
        int total = 0;
        for (int chance : chances.values()) {
            total += chance;
        }

        int randomVal = random.nextInt(total);
        int cumulative = 0;

        for (Map.Entry<String, Integer> entry : chances.entrySet()) {
            cumulative += entry.getValue();
            if (randomVal < cumulative) {
                return entry.getKey();
            }
        }

        return null;
    }
    public Fruit CreateFruit(double timePass, float widthW, float heightW, Ship ship) {
        typeChances.clear();


        if(timePass > 20.0) {
            typeChances.put("banana", 50);
            typeChances.put("grape", 50);
        } else if(timePass > 10.0) {
            typeChances.put("banana", 30);
            typeChances.put("grape", 70);
        } else {
            typeChances.put("banana", 10);
            typeChances.put("grape", 90);
        }

        String type = getRandomType(typeChances);
        if (type == null) {

            type = "grape";
        }

        Fruit fruit;

        if (type.equalsIgnoreCase("grape")) {
            fruit = new Fruit("textures/fruit2.png", 1);
        } else if (type.equalsIgnoreCase("banana")) {
            fruit = new Fruit("textures/fruit1.png", 2);
        } else {

            fruit = new Fruit("textures/fruit2.png", 1);
        }


        Vector2 position = new Vector2();
        boolean validPosition = false;
        int attempts = 0;
        float padding = 100.0f;
        float minPlayerDistance = 200.0f;

        while (!validPosition && attempts < 20) {
            position.set(
                MathUtils.random(padding, widthW - padding),
                MathUtils.random(padding, heightW - padding)
            );

            if (position.dst(ship.getPosition()) >= minPlayerDistance) {
                validPosition = true;
            }
            attempts++;
        }


        if (!validPosition) {

            Vector2 shipDirection = new Vector2(1, 0).rotate(MathUtils.random(360));
            position.set(ship.getPosition()).add(shipDirection.scl(minPlayerDistance + 50));


            position.x = MathUtils.clamp(position.x, padding, widthW - padding);
            position.y = MathUtils.clamp(position.y, padding, heightW - padding);
        }

        fruit.setPosition(position);
        return fruit;
    }
}
