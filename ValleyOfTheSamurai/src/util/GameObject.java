package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

/*
Submission by Andrew Roberts (Student Number: 20450942)
 */

/*
 * Created by Abraham Campbell on 15/01/2020.
 *   Copyright (c) 2020  Abraham Campbell

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
   
   (MIT LICENSE ) e.g do what you want with this :-) 
 */
public class GameObject {
    public enum Status {
        IDLE,
        WALKING,
        ATTACKING,
        BLOCKING,
        RUNNING,
        JUMPING,
        FALLING,
        HURT,
        DEAD
    }

    /*
    Every character's animation will depend on what their currently doing.
    When the viewer gets the texture to use when drawing them, it will look here based on what they're doing
    The enemies will use this a lot less than the player as the enemies are fairly simple for this assignment
    A future iteration of the game could take full advantage of this
     */
    public Status status;
    private String name = "Jeff"; //Make sure they always have some kind of name before being initialised
    private Point3f centre = new Point3f(0, 0, 0);            // Centre of object, using 3D as objects may be scaled
    private int width = 128;
    private int height = 128;
    private boolean hasTextured = false;
    private String textureLocation;
    private String blanktexture = "res/blankSprite.png";
    private double health;
    private enum Direction {
        RIGHT,
        LEFT
    }
    private Direction direction;
    private int CurrentAnimationTime;
    private Image sprite;

    private boolean enemy;
    private boolean speaking=false;
    private String speech;
    private int attackDistance;
    private String character;
    private String attackSound;
    private String hurtSound;
    private boolean deflect = false;
    private boolean doubleJump = false;
    private boolean teleport = false;
    private boolean naturallyHeal = false;
    private float speedModifier;

    public GameObject() {

    }

    public GameObject(String name, String textureLocation, int width, int height, Point3f centre, double health, boolean enemy) {
        this.name=name;
        hasTextured = true;
        this.textureLocation = textureLocation;
        this.width = width;
        this.height = height;
        this.centre = centre;
        this.health=health;
        this.direction=Direction.RIGHT;
        CurrentAnimationTime=0;
        this.enemy=enemy;
        try {
            setSprite(textureLocation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        attackDistance=90;
        speedModifier=1;
        if (Objects.equals(getName(), "Yurei")) {
            setHurtSound("res/Audio/Battle_cats_death_sound_effect.wav");
            setAttackSound("res/Audio/Laser_Gun_Sound_Effect.wav");
        } else if (Objects.equals(getName(), "Onre")) {
            setHurtSound("res/Audio/Battle_cats_death_sound_effect.wav");
        } else if (Objects.equals(getName(), "Player")){
            attackSound = "res/Audio/SWORD_SLASH_SOUND_EFFECT_NO_COPYRIGHT.wav";
            hurtSound = "res/Audio/Woman_Female_Grunt_Hurt_Pain_Shout_Sound_Effects.wav";
        }
        setStatus("IDLE");
    }

    public int getAttackDistance() {
        return attackDistance;
    }
    public void setAttackDistance(int attackDistance) {
        this.attackDistance=attackDistance;
    }
    public String getAttackSound() {
        //System.out.println(attackSound);
        return attackSound;
    }
    public void setAttackSound(String sound) {
        this.attackSound=sound;
    }
    public String getHurtSound() {
        return hurtSound;
    }
    public void setHurtSound(String sound) {
        this.hurtSound=sound;
    }
    public void setCharacter(String character) {
        this.character=character;
        if (Objects.equals(this.character, "Samurai")) {
            setAttackDistance(100);
            setAttackSound("res/Audio/SWORD_SLASH_SOUND_EFFECT_NO_COPYRIGHT.wav");
            setHurtSound("res/Audio/Woman_Female_Grunt_Hurt_Pain_Shout_Sound_Effects.wav");
            setDeflect(true);
            setDoubleJump(true);
            setNaturallyHeal(true);
            setTeleport(false);
            speedModifier=1;
        } else if (Objects.equals(this.character, "Fighter")) {
            setAttackDistance(50);
            setAttackSound("res/Audio/Kick_sound_effect.wav");
            setHurtSound("res/Audio/Male_Hurt_Sound_Effect.wav");
            setDoubleJump(false);
            setNaturallyHeal(false);
            setDeflect(false);
            setTeleport(true);
            speedModifier=1.5f;
        }
    }

    public String getCharacter() {
        return this.character;
    }
    public void setDeflect(boolean state) {
        this.deflect=state;
    }
    public boolean canDeflect() {
        return this.deflect;
    }
    public void setDoubleJump(boolean state) {
        this.doubleJump=state;
    }
    public boolean canDoubleJump() {
        return doubleJump;
    }
    public void setTeleport(boolean state) {
        this.teleport=state;
    }
    public boolean canTeleport() {
        return this.teleport;
    }
    public float getSpeedModifier() {
        return this.speedModifier;
    }
    public void setNaturallyHeal(boolean state) {
        this.naturallyHeal =state;
    }
    public boolean canNaturallyHeal() {
        return this.naturallyHeal;
    }

    public Point3f getCentre() {
        return centre;
    }

    public void setCentre(Point3f centre) {
        this.centre = centre;

        //make sure to put boundaries on the gameObject

    }

    public Rectangle getRectangle() {
        int width;
        if (Objects.equals(name, "Bullet")) {
            width=20;
        } else if (Objects.equals(name, "Yurei")) {
            width=20;
        } else width=getWidth()-80;
        return new Rectangle((int) getCentre().getX(), (int) getCentre().getY(), width, getHeight());
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTexture() {
        if (hasTextured) {
            return textureLocation;
        }

        return blanktexture;
    }

    public void setTexture(String TextureLocation) {
        if (TextureLocation!=null) {
            textureLocation = TextureLocation;
        }
    }

    public int getSpriteWidth() {
        int textureWidth;
        System.out.println("Texture Location: " + getTexture());
        File textureToLoad = new File(getTexture());
        Image myImage;
        try {
            myImage = ImageIO.read(textureToLoad);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        textureWidth = myImage.getWidth(null);
        return textureWidth;
    }

    public int getSpriteHeight() {
        int textureHeight;
        File textureToLoad = new File(getTexture());
        Image myImage;
        try {
            myImage = ImageIO.read(textureToLoad);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        textureHeight = myImage.getHeight(null);
        return textureHeight;
    }

    public double getHealth() {
        return this.health;
    }

    public void setHealth(double health) {
        if (health>=0) {
            this.health = health;
        }
    }

    public void setDirection(char dir) {
        if (dir=='L') {
            this.direction=Direction.LEFT;
        } else this.direction=Direction.RIGHT;
    }

    public String getDirection() {
        return this.direction.toString();
    }

    public int getCurrentAnimationTime() {
        return CurrentAnimationTime;
    }
    public void setCurrentAnimationTime(int time) {
        this.CurrentAnimationTime=time;
    }

    public void incrementAnimationTime() {
        this.CurrentAnimationTime++;
    }

    public String getStatus() {
        return status.toString();
    }

    public void setStatus(String status) {
        switch (status) {
            case "WALKING":
                this.status=Status.WALKING;
                break;
            case "ATTACKING":
                this.status=Status.ATTACKING;
                break;
            case "BLOCKING":
                this.status=Status.BLOCKING;
                break;
            case "RUNNING":
                this.status=Status.RUNNING;
                break;
            case "JUMPING":
                this.status=Status.JUMPING;
                break;
            case "FALLING":
                this.status=Status.FALLING;
                break;
            case "HURT":
                this.status=Status.HURT;
                break;
            case "DEAD":
                this.status=Status.DEAD;
                break;
            default:
                this.status=Status.IDLE;
                break;
        }
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name=name;
    }
    public void setSprite(String texture) throws IOException {
        File textureToLoad = new File(texture);
        this.sprite=ImageIO.read(textureToLoad);
    }
    public Image getSprite() {
        return this.sprite;
    }

    public void setEnemy(boolean status) {
        this.enemy=status;
    }

    public boolean isEnemy() {
        return this.enemy;
    }
    public boolean isSpeaking() {
        return this.speaking;
    }

    public void setSpeaking(boolean status) {
        this.speaking=status;
    }

    public String getSpeech() {
        return this.speech;
    }

    public void setSpeech(String speech) {
        this.speech=speech;
    }


}

/*
 *  Game Object 
 * ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::c:::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::::::::::::::clc::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::::::::::::::lol:;::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::;;cool:;::::::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::::::::::codk0Oxolc:::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::coxk0XNWMMWWWNK0kxdolc::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::loxO0XWMMMMMMMWWWMMMMMMWWNK0Oxdlc::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::cldkOKNWMMMMMMMMMMMMMWWMMMMMMMMMMMMMWNX0Okdolc:::::::::::::::::::::::::::::::::
::::::::::::::::codk0KNWMMMMMMMMMMMMMMMMMMWMMMMMMMMMMMMMMMMMMMMWWXKOkxo:::::::::::::::::::::::::::::
::::::::::::;cdOXNWMMMMMMMMMMMMMMMMMMMMMMWWWMMMMMMMMMMMMMMMMMMMMMMMNKOdc::::::::::::::::::::::::::::
:::::::::::::cxKXXNWWMMMMMMMMMMMMMMMMMMMMWWWMMMMMMMMMMMMMMMMMMMWX0kdolc:::::::::::::::::::::::::::::
::::::::::::::d0000KKXNNWMMMMMMMMMMMMMMMMWWMMMMMMMMMMMMMMMMWNKOxolllllc:::::::::::::::::::::::::::::
::::::::::::::oO00000000KXXNWWMMMMMMMMMMMMWMMMMMMMMMMMMMWX0kdollllllllc:::::::::::::::::::::::::::::
::::::::::::::lk00000O000000KKXNWWMMMMMMMWWWMMMMMMMMWNKOxollllllllllll::::::::::::::::::::::::::::::
::::::::::::::cx0000000000O000000KXXNWMMMWWWMMMMWXK0kdlllllllllllllllc::::::::::::::::::::::::::::::
:::::::::::::::dO00000000000000000000KKXNNNWWNKOxolllllllllllllllllllc::::::::::::::::::::::::::::::
:::::::::::::::lO000000000000000OOOO0000000Kkdlllllllllllllllllllllllc::::::::::::::::::::::::::::::
:::::::::::::::ck00000000000000000OOOOOOOOkxollllllllllllllllllllllll:::::::::::::::::::::::::::::::
:::::::::::::;;cx00000000000000000000OOOOOOxocllllllllllllllllllllllc:;;;;;;;;;;::::::::::::::::::::
;;;;;;;;;;;;;;;:oO00000000000000000OOOO0000kdllllcclllllllllllllllllc:;;;;;;;;;;;;;;;;;;::::::::::::
;;;;;;;;;;;;;;;:lO00000000000000OOO00000000Oolllllllllllllllllllllllc:;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;ck0000000000OOO000000000000kolllllllllllllllllllllll:;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;:dOO0000OOO0000000000000000kolllllllllllllllllllllllc:;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;:lxkOOOO0000000000000000000kolllllllllllllllllllllooool::;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;::;;::cokOkkO00000000000000000000kolllllllllllllllllllllccccllcc::::;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;:;;:;::ccllodxkO00000000000000000000kolllllllllllllllllllcc:;;;;:::::;;:;;;;;;;;;;;;;;;;;;;;;
;;;;;;;::::::::::;;:ldkO0000000000000000000kolllllllllllllllllc::;;::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;::;;:::;;;;;:;::ldkO0000000000000000kollllllllllllllcc::;;;;:;;:;;;;;:;;;:;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;:;;;;:cldkO0000000000000kollllllllllllc:::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;::ldkO0000000000kolllllllllcc::;;::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;::;:;;::ldkO0000000kolllllllc::::;;;;:;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;:;::ldkO0000kolllcc:::;;;;;;;;::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;:;;;;;:ldkO0kolcc::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;:;;;::lodl:::::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;:;::;:::::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;:;;::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;::;;;;;;;;;;;:::;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
*/