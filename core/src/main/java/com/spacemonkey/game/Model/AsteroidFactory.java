package com.spacemonkey.game.Model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class AsteroidFactory {
    private Map<String, Integer> typeChances;
    private Random random;
    public AsteroidFactory()
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
    public Asteroid CreateAstroid(double timePass, float widthW, float heightW) {

        typeChances.clear();


        if(timePass > 20.0) {
            typeChances.put("Fire", 50);
            typeChances.put("Frize", 30);
            typeChances.put("Normal", 20);
        } else if(timePass > 10.0) {
            typeChances.put("Fire", 10);
            typeChances.put("Frize", 60);
            typeChances.put("Normal", 30);
        } else {
            typeChances.put("Fire", 5);
            typeChances.put("Frize", 10);
            typeChances.put("Normal", 85);
        }

        String type = getRandomType(typeChances);
        if (type == null) {

            type = "Normal";
        }

        Asteroid asteroid;
        float size;

        if (type.equalsIgnoreCase("Normal")) {
            asteroid = new Asteroid("textures/asteroid.png",300.0f);
            size = 30.0f;
        } else if (type.equalsIgnoreCase("Frize")) {
            asteroid = new Asteroid("textures/asteroid_frize.png", 600.0f);
            size = 100.0f;
        } else if (type.equalsIgnoreCase("Fire")) {
            asteroid = new Asteroid("textures/asteroid_fire.png", 800.0f);
            size = 120.0f;
        } else {

            asteroid = new Asteroid("textures/asteroid.png", 300.0f);
            size = 30.0f;
        }


        Vector2 position = new Vector2();


        int spawnSide = MathUtils.random(0, 3);
        switch (spawnSide) {
            case 0:
                position.set(MathUtils.random(0, widthW), -size);
                break;
            case 1:
                position.set(widthW + size, MathUtils.random(0, heightW));
                break;
            case 2:
                position.set(MathUtils.random(0, widthW), heightW + size);
                break;
            case 3:
                position.set(-size, MathUtils.random(0, heightW));
                break;
        }

        Vector2 centerPoint = new Vector2(
            widthW * (0.3f + MathUtils.random() * 0.4f),
            heightW * (0.3f + MathUtils.random() * 0.4f)
        );

        Vector2 direction = new Vector2(centerPoint).sub(position).nor();
        float rotationSpeed = MathUtils.random(-2.0f, 2.0f);


        asteroid.size = size;
        asteroid.setPosition(position);
        asteroid.setVelocity(direction);
        asteroid.setRotationSpeed(rotationSpeed);

        return asteroid;
    }
}
