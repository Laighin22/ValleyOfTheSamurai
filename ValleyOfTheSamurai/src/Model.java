import java.awt.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import util.*;

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
public class Model {

    private GameObject Player;
    private Controller controller = Controller.getInstance();
    private CopyOnWriteArrayList<GameObject> YureiList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<GameObject> BulletList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<GameObject> OnreList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<GameObject> NpcList = new CopyOnWriteArrayList<>();
    private int Score = 0;
    private boolean npcSpeaking = false;
    private boolean attacking = false;
    private boolean hurting=false;
    private long attackStartTime;
    private long damageStartTime;
    private boolean jumping = false;
    private boolean falling = false;
    private boolean teleporting = false;
    boolean gameOver = false;
    boolean gameComplete = false;
    float playerSpeed = 0.f;
    private AudioPlayer musicAudio = new AudioPlayer();
    private AudioPlayer playerAudio = new AudioPlayer();
    private AudioPlayer enemyAudio = new AudioPlayer();
    private int level = -1;
    private int enemyWave = -1;
    private boolean levelComplete = false;
    private boolean levelPaused = false;
    private int yureiBlastTimer=200;
    private int healingTimer=200;
    private boolean cutScenePlaying=false;
    private int cutScene = 0;
    private int cutSceneStage = 0;
    private float jumpPos=0;
    private int jumpCounter=0;
    private String playerName="Samurai";

    public Model() {
        //setup game world
        //Player
        Player = new GameObject("Player", "res/Samurai/Idle.png", 200, 200, new Point3f(0, 400, 0), 3.0, false);
        Player.setStatus("IDLE");
        //Enemies  starting with four

        spawnEnemies(this.enemyWave);
        spawnNPCS(this.level);
        cutScenePlaying=true;
    }

    // This is the heart of the game , where the model takes in all the inputs ,decides the outcomes and then changes the model accordingly.
    public void gamelogic() {
        if (cutScenePlaying) {
            cutSceneLogic(cutScene);
        } else if (!levelPaused) {
            // Player Logic first
            playerLogic();
            // Onre Logic next
            onreLogic();
            // Enemy Logic next
            yureiLogic();
            // Bullets move next
            bulletLogic();
            //npc logic next
            npcLogic();
            // interactions between objects
            gameLogic();
        }
    }

    private void gameLogic() {


        // this is a way to increment across the array list data structure
        for (GameObject temp : YureiList) {
            if ((getPlayer().getStatus().equals("ATTACKING")) && (System.currentTimeMillis() - attackStartTime > 100)) {
                if (Math.abs((temp.getCentre().getX()-20) - Player.getCentre().getX()) <= Player.getAttackDistance()) {
                    if (facingEnemy(temp)) {
                        defeatEnemy(temp);
                    }
                }
            }
        }

        for (GameObject temp : OnreList) {
            if ((getPlayer().getStatus().equals("ATTACKING")) && (System.currentTimeMillis() - attackStartTime > 350)) {
                if (Math.abs(temp.getCentre().getX() - Player.getCentre().getX()) <= Player.getAttackDistance()) {
                    if (facingEnemy(temp)) {
                        defeatEnemy(temp);
                    } //else System.out.println("WRONG DIRECTION");
                }
            }
        }

        //The final level has more criteria in order for the level to be complete
        if (this.level==3) {
            if (enemyWave==6) {
                setLevelComplete(true);
            } else if (getEnemies().isEmpty() && getOnres().isEmpty()) {
                enemyWave++;
                spawnEnemies(this.enemyWave);
            }
        } else if (getEnemies().isEmpty() && getOnres().isEmpty()) {
            setLevelComplete(true);
        }

        //The player ideally shouldn't do this, but I wanted to leave it as an option to allow more freedom and consuequences
        for (GameObject temp : NpcList) {
            if ((getPlayer().getStatus().equals("ATTACKING")) && (System.currentTimeMillis() - attackStartTime > 350)) {
                if (Math.abs(temp.getCentre().getX() - Player.getCentre().getX()) <= getPlayer().getAttackDistance()) {
                    if (facingEnemy(temp)) {
                        NpcList.remove(temp);
                    } //else System.out.println("WRONG DIRECTION");
                }
            }
        }

        NpcList.removeIf(npc -> npc.getHealth() == 0);

        if (NpcList.isEmpty()) {
            if (this.level==3) {
                levelPaused=true;
                gameOver=true;
            }
        }
    }

    //The onre are easier to defeat but have more complex behaviour so their logic is a lot longer
    //I tried to keep the two types of enemies balanced between their difficulty, their damage and their behaviour
    public void onreLogic() {

        int minDistance;
        //On the last level they should immediately be targeting the NPCs so the min distance will guarantee they see them
        if (level==3) {
            minDistance=10000;
        } else minDistance=400;

        for (GameObject temp: OnreList) {
            int direction = 0;
            //Start of their routine
            if (!Objects.equals(temp.getStatus(), "DEAD")) {
                if (!Objects.equals(temp.getStatus(), "WALKING")) {
                    //Their patrol is triggered by a target getting too close
                    if (Math.abs(temp.getCentre().getX() - getClosestFriendly(temp).getCentre().getX()) < minDistance) {
                        faceObject(temp, getClosestFriendly(temp));
                        temp.setStatus("WALKING");
                        temp.setTexture("res/Onre/Walk.png");
                    }
                } else if (Objects.equals(temp.getStatus(), "WALKING")) {
                    //This is logic for when they are on patrol
                    //Check to see if they've reached the edges of the screen and change direction accordingly
                    if (temp.getCentre().getX() + direction == 0) {
                        temp.setDirection('R');
                    } else if (temp.getCentre().getX() + direction == 1140) {
                        temp.setDirection('L');
                    }
                    //Otherwise keep walking, they should move +1 or -1 depending on what direction they are facing
                    if (Objects.equals(temp.getDirection(), "RIGHT")) {
                        direction = 1;
                    } else direction = -1;
                    temp.getCentre().ApplyVector(new Vector3f(direction, 0, 0));
                }
            }


            //Check if they have walked into a friendly
            if (Math.abs(temp.getCentre().getX()-getClosestFriendly(temp).getCentre().getX())==0) {
                if (Math.abs(temp.getCentre().getY() - getClosestFriendly(temp).getCentre().getY())<50) {
                    //More checks required if they are attacking player
                    if (canHit(temp)) {
                        //System.out.println("Attacking player when they are: " + Player.getStatus());
                        damage(getClosestFriendly(temp), direction, 1.0);
                    }
                }
            }
        }
    }

    //The yurei has very simple behaviour and doesn't do much damage, but their long ranged attacks can
    //catch the player off guard if they're not careful and require a bit more work to counter
    private void yureiLogic() {
        // TODO Auto-generated method stub

        //same logic as onres
        int minDistance;
        if (level == 3) {
            minDistance = 10000;
        } else minDistance = 600;

        for (GameObject temp : YureiList) {
            // Shoot at player
            if (Math.abs(temp.getCentre().getX() - getClosestFriendly(temp).getCentre().getX()) < minDistance) {
                faceObject(temp, getClosestFriendly(temp));
                if (getBullets().isEmpty() && yureiBlastTimer == 400) {
                    CreateBullet(temp);
                    yureiBlastTimer = 0;
                } else {
                    yureiBlastTimer++;
                }
            }
        }
    }

    private void bulletLogic() {
        // TODO Auto-generated method stub
        // move bullets

        for (GameObject temp : BulletList) {
            //check to move them
            int direction;
            if (Objects.equals(temp.getDirection(), "LEFT")) {
                temp.getCentre().ApplyVector(new Vector3f(-2, 0, 0));
                direction = -1;
            } else {
                temp.getCentre().ApplyVector(new Vector3f(2, 0, 0));
                direction=1;
            }

            //bullet logic if its an enemy bullet
            if (temp.isEnemy()) {
                //did they hit anything
                if (checkCollision(temp, getClosestFriendly(temp))) {
                    GameObject target = getClosestFriendly(temp); //store it so we don't have to do multiple calculations here every time we want to reference the closest character
                    if (Objects.equals(target.getName(), "Player")) {
                        //if the player blocked then nothing should happen and bullet should disappear
                        if (Objects.equals(getPlayer().getStatus(), "BLOCKING") && facingEnemy(temp)) {
                            BulletList.remove(temp);
                        } else if (deflected()) {
                            //Change the bullet to a friendly bullet
                            temp.setEnemy(false);
                            //Change the direction
                            if (Objects.equals(temp.getDirection(), "LEFT")) {
                                //System.out.println("Changed direction");
                                temp.setDirection('R');
                            } else temp.setDirection('L');
                        } else {
                            //Otherwise just damage the player
                            //getPlayer().setHealth(getPlayer().getHealth() - 0.5);
                            damage(Player, direction, 0.5);
                            BulletList.remove(temp);
                        }
                    } else  {
                        //If its not a player and is an npc, just lower their health
                        target.setHealth(target.getHealth() - 0.5);
                        BulletList.remove(temp);
                    }
                }
            } else {
                if (checkCollision(temp, getClosestEnemy(temp))) {
                    //System.out.println("Bullet collided with enemy");
                    GameObject target = getClosestEnemy(temp); //store it so we don't have to do multiple calculations here every time we want to reference the closest character
                    BulletList.remove(temp);
                    defeatEnemy(target);
                }
            }

            //see if they get to the edge of the screen
            if (temp.getCentre().getX()<=0 || temp.getCentre().getX()>=1140) {
                BulletList.remove(temp);
            }
        }
    }

    public void npcLogic() {
        //The NPCs should speak if the player gets close to them
        if (!getNpcs().isEmpty()) {
            GameObject temp = NpcList.get(0);
            if (levelComplete) {
                temp.setSpeech("You saved us " + playerName + ", thank you!");
                temp.setSpeaking(true);
            } else if (Math.abs(temp.getCentre().getX() - Player.getCentre().getX()) < 100) {
                temp.setSpeech("Please help us," + playerName + "!");
                temp.setSpeaking(true);
            } else {
                temp.setSpeaking(false);
                temp.setSpeech("");
            }
        }
    }

    private void playerLogic() {
        //Check what they're doing, the idle should be the default status to reset to but attacking and jumping/falling should take priority
        if (attacking) {
            //System.out.println("**DEBUG: " + (System.currentTimeMillis() - attackStartTime));
            if (System.currentTimeMillis() - attackStartTime > 550) {
                attacking=false;
                Player.setStatus("IDLE");
            }
        } else if (hurting) {
            if (System.currentTimeMillis() - damageStartTime > 1500) {
                //System.out.println("HURT ANIMATION DONE");
                hurting = false;
                Player.setStatus("IDLE");
            }
        } else if (!jumping && !falling) {
            Player.setStatus("IDLE");
        }

        // smoother animation is possible if we make a target position  // done but may try to change things for students

        //check for movement and if you used your attack

        if (Controller.getInstance().isKeyAPressed()) {
            getPlayer().setDirection('L');
            if (!Controller.getInstance().isRightClickPressed()) {
                if (Controller.getInstance().isKeyShiftPressed()) {
                    //make sure not to overwrite animations of higher priority
                    if (!jumping && !falling) {
                        Player.setStatus("RUNNING");
                    }
                    playerSpeed = -2;
                } else {
                    playerSpeed = -1;
                    if (!jumping && !falling) {
                        Player.setStatus("WALKING");
                    }
                }
                playerSpeed = playerSpeed * Player.getSpeedModifier();
                Player.getCentre().ApplyVector(new Vector3f(playerSpeed, 0, 0));
            }
        }

        if (Controller.getInstance().isKeyDPressed()) {
            //System.out.println("WALKING");
            getPlayer().setDirection('R');
            if (!Controller.getInstance().isRightClickPressed()) {
                if (Controller.getInstance().isKeyShiftPressed()) {
                    playerSpeed = 2;
                    if (!jumping && !falling) {
                        Player.setStatus("RUNNING");
                    }
                } else {
                    playerSpeed = 1;
                    if (!jumping && !falling) {
                        Player.setStatus("WALKING");
                    }
                }
                playerSpeed = playerSpeed * Player.getSpeedModifier();
                Player.getCentre().ApplyVector(new Vector3f(playerSpeed, 0, 0));
            }
        }

        if (Controller.getInstance().isLeftClickPressed()) {
            if (!jumping && !falling) {
                Point p = MouseInfo.getPointerInfo().getLocation();
                //Check if the user clicked in front of or behind the player
                if (p.x>getPlayer().getCentre().getX()) {
                    getPlayer().setDirection('R');
                } else if (p.x<getPlayer().getCentre().getX()) {
                    getPlayer().setDirection('L');
                }
                Player.setStatus("ATTACKING");
                startAttack();
                Player.setTexture("BaseGameTemplate/res/" + getPlayer().getCharacter() + "/Attack.png");
            }
        }

        if (Controller.getInstance().isRightClickPressed()) {
            if (!jumping && !falling) {
                Point p = MouseInfo.getPointerInfo().getLocation();
                //Check if the user clicked in front of or behind the player
                if (p.x>getPlayer().getCentre().getX()) {
                    getPlayer().setDirection('R');
                } else if (p.x<getPlayer().getCentre().getX()) {
                    getPlayer().setDirection('L');
                }
                Player.setStatus("BLOCKING");
            }
        }

        if (Controller.getInstance().isKeyWPressed()) {
            if (jumping || falling) {
                //System.out.println("Jump Counter: " + jumpCounter);
                if (Player.canDoubleJump()) {
                    if (jumpCounter<2) {
                        jumpPos = Player.getCentre().getY();
                        Player.setStatus("JUMPING");
                        jumping = true;
                        jumpCounter++;
                    }
                }
            } else {
                jumpPos=Player.getCentre().getY();
                Player.setStatus("JUMPING");
                jumping=true;
                jumpCounter++;
            }
            Controller.getInstance().setKeyWPressed(false);
        }

        if (Controller.getInstance().isKeySpacePressed()) {
            if (jumping || falling) {
                //System.out.println("Jump Counter: " + jumpCounter);
                if (Player.canDoubleJump()) {
                    if (jumpCounter<2) {
                        jumpPos = Player.getCentre().getY();
                        Player.setStatus("JUMPING");
                        jumping = true;
                        jumpCounter++;
                    }
                }
            } else {
                jumpPos=Player.getCentre().getY();
                Player.setStatus("JUMPING");
                jumping=true;
                jumpCounter++;
            }
            Controller.getInstance().setKeySpacePressed(false);
        }

        if (Controller.getInstance().isKeyEscPressed()) {
            levelPaused=true;
        }

        if (Controller.getInstance().isMiddleClickPressed()) {
            if (Player.canTeleport()) {
                if (!teleporting) {
                    teleporting = true;
                    playerAudio.playAudio("res/Audio/Teleport_Sound_Effect_No_Copyright.wav");
                    int xcoord = (int) ((Math.random() * (1140 - 1)) + 1);
                    Player.getCentre().setX(xcoord);
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            teleporting = false;
                        }
                    }, 1000);
                }
            }
            Controller.getInstance().setMiddleClickPressed(false);
        }

        //System.out.println(isLevelComplete());
        if (Player.getCentre().getX()>=1140 && isLevelComplete()) {
            nextLevel();
        }

        if (Player.getHealth()<=0.0) {
            levelPaused=true;
            gameOver=true;
        }

        if (Player.canNaturallyHeal()) {
            if (Player.getHealth()<3.0) {
                if (healingTimer==800) {
                    Player.setHealth(Player.getHealth() + 0.5);
                    healingTimer = 0;
                } else healingTimer++;
            }
        }
    }

    public void cutSceneLogic(int cutScene) {
        if (cutScene==0) {
            if (cutSceneStage==0) {
                if (NpcList.get(0).getCentre().getX() > 400) {
                    NpcList.get(0).setTexture("res/Shinobi/Run.png");
                    NpcList.get(0).getCentre().ApplyVector(new Vector3f(-2, 0, 0));
                } else {
                    NpcList.get(0).setTexture("res/Shinobi/Idle.png");
                    NpcList.get(0).setSpeech(playerName + ", the monsters are attacking my home!");
                    NpcList.get(0).setSpeaking(true);
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            NpcList.get(0).setSpeech("");
                            NpcList.get(0).setSpeaking(false);
                            cutSceneStage=1;
                        }
                    }, 2000);
                }
            } else if (cutSceneStage==1) {
                if (Player.getCentre().getX() < 1140) {
                    Player.setStatus("RUNNING");
                    Player.getCentre().ApplyVector(new Vector3f(2, 0, 0));
                } else {
                    Player.setStatus("IDLE");
                    cutScenePlaying=false;
                    cutSceneStage=0;
                }
            }
        } else if (cutScene==1) {
            if (Player.getCentre().getX() < 400) {
                Player.setStatus("WALKING");
                Player.getCentre().ApplyVector(new Vector3f(1, 0, 0));
            } else {
                Player.setStatus("IDLE");
                Player.setSpeech("Finally... it's over");
                Player.setSpeaking(true);
            }
        }
    }

    private void CreateBullet(GameObject temp) {
        enemyAudio.playAudio(temp.getAttackSound());
        GameObject bullet = new GameObject("Bullet", "res/Yurei/Charge_3.png", 80, 80, new Point3f(temp.getCentre().getX(), temp.getCentre().getY()+100, 0.0f), 0.5, true);
        bullet.setDirection(temp.getDirection().charAt(0));
        BulletList.add(bullet);
    }

    public void setJumping(boolean Jumping) {
        jumping = Jumping;
    }

    public boolean isJumping() {
        return jumping;
    }

    public void setFalling(boolean falling) {
        this.falling=falling;
    }
    public boolean isFalling() {
        return falling;
    }

    public GameObject getPlayer() {
        return Player;
    }

    public GameObject getClosestFriendly(GameObject temp) {
        GameObject closest = getPlayer();
        float distance = Math.abs(temp.getCentre().getX() - closest.getCentre().getX());

        for (GameObject npc : NpcList) {
            if (Math.abs(temp.getCentre().getX() - npc.getCentre().getX()) < distance) {
                closest = npc;
                distance = temp.getCentre().getX() - npc.getCentre().getX();
            } //else System.out.println(npc.getName() + " is too far: " + Math.abs(temp.getCentre().getX() - npc.getCentre().getX()) + " > " + distance);
        }
        return closest;
    }

    public GameObject getClosestEnemy(GameObject temp) {
        GameObject closest = null;
        float distance = 1000000.0f;

        //Check both types of enemies
        for (GameObject enemy : YureiList) {
            if (Math.abs(temp.getCentre().getX() - enemy.getCentre().getX()) < distance) {
                closest = enemy;
                distance = Math.abs(temp.getCentre().getX() - enemy.getCentre().getX());
            }
        }

        for (GameObject enemy : OnreList) {
            if (Math.abs(temp.getCentre().getX() - enemy.getCentre().getX()) < distance) {
                closest = enemy;
                distance = Math.abs(temp.getCentre().getX() - enemy.getCentre().getX());
            }
        }

        return closest;
    }

    public CopyOnWriteArrayList<GameObject> getEnemies() {
        return YureiList;
    }

    public CopyOnWriteArrayList<GameObject> getBullets() {
        return BulletList;
    }

    public int getScore() {
        return Score;
    }

    public boolean isCutScenePlaying() {
        return this.cutScenePlaying;
    }

    public void setCutScenePlaying(boolean state) {
        this.cutScenePlaying=state;
    }

    public int getJumpCounter() {
        return this.jumpCounter;
    }

    public void setJumpCounter(int count) {
        this.jumpCounter=count;
    }
    public float getJumpPos() {
        return this.jumpPos;
    }

    public void setJumpPos(float value) {
        this.jumpPos=value;
    }


    /*
    Play sound effect and initialise the attack start time
    The attack start time is used as a reference for when the player should go back to being idle
     */
    public void startAttack() {
        //Controller class suffers from registering multiple presses so to this avoids audio getting distorted
        if (!playerAudio.isPlaying()) {
            playerAudio.playAudio(getPlayer().getAttackSound());
        }
        attacking = true;
        attackStartTime = System.currentTimeMillis();
    }


    //remove them from the game and play a death animation and increase the score
    //double points for more difficult enemy
    public void defeatEnemy(GameObject enemy) {
        int delay;
        int scoreGained;
        if (Objects.equals(enemy.getName(), "Yurei")) {
            delay = 40;
            scoreGained=2;
        } else {
            delay=800;
            scoreGained=1;
        }
        enemy.setStatus("DEAD");
        if (!enemyAudio.isPlaying()) {
            enemyAudio.playAudio(enemy.getHurtSound());
        }
        enemy.setTexture("res/"+enemy.getName()+"/Dead.png");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (Objects.equals(enemy.getName(), "Yurei")) {
                    YureiList.remove(enemy);
                } else OnreList.remove(enemy);
            }
        }, delay);
        Score+=scoreGained;
    }

    //Check if two game objects are colliding
    public boolean checkCollision(GameObject temp, GameObject target) {

        return temp.getRectangle().intersects(target.getRectangle());
    }

    //Simple function to get the enemies to turn and face the player when the player gets too close
    public void faceObject(GameObject temp, GameObject target) {
        if (temp.getCentre().getX()>target.getCentre().getX()) {
            temp.setDirection('L');
        } else if (temp.getCentre().getX()<target.getCentre().getX()) {
            temp.setDirection('R');
        }
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level=level;
        System.out.println("Level updated to: " + this.level);
    }

    public int getEnemyWave() {
        return this.enemyWave;
    }

    public void setEnemyWave(int wave) {
        this.enemyWave=wave;
    }

    public boolean isLevelComplete() {
        return levelComplete;
    }

    public void setLevelComplete(boolean status) {
        this.levelComplete = status;
    }

    public void nextLevel() {
        System.out.println("LOADING NEXT LEVEL...");
        //Increment level counter
        setLevel(getLevel()+1);
        System.out.println("Level number: " + getLevel());
        setEnemyWave(getEnemyWave()+1);
        if (getLevel()==4) {
            gameComplete=true;
            cutScenePlaying=true;
            cutScene=1;
        }
        //Reload what the player sees
        resetModel();
        //The new level shouldn't be complete yet
        setLevelComplete(false);
        System.out.println("Done");
    }

    public void resetModel() {
        //Reset player's health to make it easier for them
        Player.setHealth(3.0);
        //Move them to start of level to create impression that they walked to the next level
        Player.getCentre().setX(0);
        //Y reset should never be needed, but I felt it's important to include in case the game breaks at all
        Player.getCentre().setY(400);
        Player.setDirection('R');
        System.out.println("Player position reset to: " + Player.getCentre().getX() + ", " + Player.getCentre().getY());
        //Remove all enemies in case its loading the same level again and the enemies aren't dead
        YureiList.clear();
        OnreList.clear();
        NpcList.clear();
        BulletList.clear();
        yureiBlastTimer=200;
        //Spawn whatever enemies this level requires
        //Since waves only come into the game in the final mission, I use this.level to decide what enemies to spawn
        //That way if they fail the final mission and retry, they have to fight all the waves again and not the last wave they were on
        //This means the enemy wave and level counter need to be synced again
        this.enemyWave=this.level;
        spawnEnemies(this.level);
        spawnNPCS(this.level);
        playLevelMusic(this.level);
        gameOver=false;
        levelPaused=false;
    }

    public void spawnEnemies(int wave) {
        System.out.println("Spawning enemies for wave: " + level);
        //Check what level it is and choose what enemies to spawn based on that
        if (wave==0) {
            OnreList.add(new GameObject("Onre", "res/Onre/Idle.png", 200, 200, new Point3f(800, 400, 0), 0.5, true));
            OnreList.get(0).setStatus("IDLE");
        } else if (wave==1) {
            YureiList.add(new GameObject("Yurei", "res/Yurei/Yurei_Idle.png", 200, 200, new Point3f(800, 400, 0), 0.5, true));
        } else if (wave==2) {
            OnreList.add(new GameObject("Onre", "res/Onre/Idle.png", 200, 200, new Point3f(500, 400, 0), 0.5, true));
            OnreList.get(0).setStatus("IDLE");
            YureiList.add(new GameObject("Yurei", "res/Yurei/Yurei_Idle.png", 200, 200, new Point3f(800, 400, 0), 0.5, true));
        } else if (wave==3) {
            OnreList.add(new GameObject("Onre", "res/Onre/Idle.png", 200, 200, new Point3f(1140, 400, 0), 0.5, true));
            OnreList.get(0).setStatus("IDLE");
            OnreList.add(new GameObject("Onre", "res/Onre/Idle.png", 200, 200, new Point3f(900, 400, 0), 0.5, true));
            OnreList.get(1).setStatus("IDLE");
            YureiList.add(new GameObject("Yurei", "res/Yurei/Yurei_Idle.png", 200, 200, new Point3f(800, 400, 0), 0.5, true));
        } else if (wave==4) {
            OnreList.add(new GameObject("Onre", "res/Onre/Idle.png", 200, 200, new Point3f(1400, 400, 0), 0.5, true));
            OnreList.get(0).setStatus("IDLE");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    GameObject temp = new GameObject("Onre", "res/Onre/Idle.png", 200, 200, new Point3f(1400, 400, 0), 0.5, true);
                    OnreList.add(temp);
                    temp.setStatus("IDLE");
                }
            }, 800);
        } else if (wave==5) {
            OnreList.add(new GameObject("Onre", "res/Onre/Idle.png", 200, 200, new Point3f(1200, 400, 0), 0.5, true));
            OnreList.get(0).setStatus("IDLE");
            YureiList.add(new GameObject("Yurei", "res/Yurei/Yurei_Idle.png", 200, 200, new Point3f(1000, 400, 0), 0.5, true));
        }
    }

    public void spawnNPCS(int level) {
        if (level==-1) {
            NpcList.add(new GameObject("villager", "res/Shinobi/Idle.png", 200, 200, new Point3f(1600, 400, 0), 1.0, false));
            NpcList.get(0).setDirection('L');
        }
        if (level==3) {
            NpcList.add(new GameObject("villager", "res/Kunoichi/Idle.png",225, 225, new Point3f(100, 375, 0), 2.0, false));
            NpcList.add(new GameObject("villager", "res/Kunoichi/Idle.png",225, 225, new Point3f(200, 375, 0), 2.0, false));
            NpcList.add(new GameObject("villager", "res/Kunoichi/Idle.png",225, 225, new Point3f(300, 375, 0), 2.0, false));
        }
    }

    public CopyOnWriteArrayList<GameObject> getOnres() {
        return OnreList;
    }

    public CopyOnWriteArrayList<GameObject> getNpcs() {
        return NpcList;
    }

    //FUnction used to verify correctly done attacks/blocks
    public boolean facingEnemy(GameObject temp) {
        //Check if the player is facing the enemy for attacks or blocks
        if (temp.getCentre().getX() > getPlayer().getCentre().getX() && Objects.equals(getPlayer().getDirection(), "RIGHT")) {
            //System.out.println("FACING RIGHT DIRECTION");
            return true;
        } else if (temp.getCentre().getX() > getPlayer().getCentre().getX() && Objects.equals(getPlayer().getDirection(), "LEFT")) {
            //System.out.println("FACING WRONG DIRECTION");
            return false;
        } else return temp.getCentre().getX() < getPlayer().getCentre().getX() && Objects.equals(getPlayer().getDirection(), "LEFT");
    }

    public void damage(GameObject target, int direction, double damage) {
        //System.out.println("Damaged: " + target.getName() + " who's health was: " + target.getHealth());
        target.setHealth(target.getHealth() - damage);
        target.setStatus("HURT");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                target.setStatus("IDLE");
            }
        }, 1000);
        if (Objects.equals(target.getName(), "Player")) {
            playerAudio.playAudio(getPlayer().getHurtSound());
            Player.setTexture("BaseGameTemplate/res/" + getPlayer().getCharacter() + "/Hurt.png");
            Player.setStatus("HURT");
            hurting = true;
            damageStartTime = System.currentTimeMillis();
            int directionX;
            if (direction > 0) {
                //Push them to the right and face the enemy
                directionX = 50;
                Player.setDirection('L');
            } else {
                //Push them to the left and face the enemy
                directionX = -50;
                Player.setDirection('R');
            }
            Player.getCentre().ApplyVector(new Vector3f(directionX, 0, 0));
        }
    }

    public boolean isLevelPaused() {
        return this.levelPaused;
    }

    public void setLevelPaused(boolean state) {
        this.levelPaused=state;
    }

    public boolean isNpcSpeaking() {
        return npcSpeaking;
    }

    public void playLevelMusic(int level) {
        if (musicAudio.isPlaying()) {
            musicAudio.stopAudio();
        }
        if (level < 1) {
            musicAudio.playMusic("res/Music/xDeviruchi_And_The_Journey_Begins .wav");
            musicAudio.setVolume(-15.0f);
        } else if (level==1) {
            musicAudio.playMusic("res/Music/xDeviruchi_Decisive_Battle.wav");
            musicAudio.setVolume(-15.0f);
        } else if (level==2) {
            musicAudio.playMusic("res/Music/xDeviruchi_Exploring_The_Unknown.wav");
            musicAudio.setVolume(-15.0f);
        } else if (level==3) {
            musicAudio.playMusic("res/Music/xDeviruchi_Prepare_for_Battle.wav");
            musicAudio.setVolume(-15.0f);
        } else if (level==4) {
            musicAudio.playMusic("res/Music/xDeviruchi_The_Final_of_The_Fantasy.wav");
            musicAudio.setVolume(-15.0f);
        }
        musicAudio.resumeAudio();
        if (!musicAudio.isPlaying()) {
            System.out.println("Music is not playing this time");
        }
    }

    public AudioPlayer getMusicAudio() {
        return musicAudio;
    }

    /*
    Test mechanic to deflect a bullet back towards enemies
    It's very powerful so should only be achieved under very specific conditions
     */
    public boolean deflected() {
        if (Player.canDeflect()) {
            if (Objects.equals(Player.getStatus(), "ATTACKING")) {
                return Math.abs(System.currentTimeMillis() - attackStartTime) <= 200;
            } else return false;
        } else return false;
    }

    public boolean canHit(GameObject source) {
        boolean answer = true;
        String targetStatus = getClosestFriendly(source).getStatus();
        //System.out.println("Target is: " + targetStatus);
        //System.out.println("Source is : " + source.getStatus());
        if (Objects.equals(targetStatus, "ATTACKING")) {
            answer = false;
        } else if (Objects.equals(targetStatus, "HURT")) {
            answer = false;
        } else if (Objects.equals(source.getStatus(), "DEAD")) {
            answer = false;
        }
        return answer;
    }

    public void setPlayerName(String name) {
        if ((name != null) && (!name.isEmpty())) {
            playerName=name;
        }
    }

}


/* MODEL OF your GAME world 
 * MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWWNNNXXXKKK000000000000KKKXXXNNNWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWNXXK0OOkkxddddooooooolllllllloooooooddddxkkOO0KXXNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWNXK0OkxddooolllllllllllllllllllllllllllllllllllllllloooddxkO0KXNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXK0OkdooollllllllooddddxxxkkkOOOOOOOOOOOOOOOkkxxdddooolllllllllllooddxO0KXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNK0kxdoollllllloddxkO0KKXNNNNWWWWWWMMMMMMMMMMMMMWWWWNNNXXK00Okkxdoollllllllloodxk0KNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXKOxdooolllllodxkO0KXNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWNXK0OkxdolllllolloodxOKXWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOxdoolllllodxO0KNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXKOkdolllllllloodxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0kdolllllooxk0KNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNK0kdolllllllllodk0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xdolllllodk0XNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWWMMMMMMMMMMMWN0kdolllllllllodx0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xoollllodxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWWMMMMMMMMMMWNXKOkkkk0WMMMMMMMMMMMMWNKkdolllloololodx0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWN0kdolllllox0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXK0kxk0KNWWWWNX0OkdoolllooONMMMMMMMMMMMMMMMWXOxolllllllollodk0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdollllllllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWN0xooollloodkOOkdoollllllllloxXWMMMMMMMMMMMMMMMWXkolllllllllllllodOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWN0koolllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxolllllllllllllllllllllllllllox0XWWMMMMMMMMMWNKOdoloooollllllllllllok0NWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xoolllllllllllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxllolllllllllllllllllloollllllolodxO0KXNNNXK0kdoooxO0K0Odolllollllllllox0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdolllllllllllllllllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkolllllllllloolllllllllllllllllllolllloddddoolloxOKNWMMMWNKOxdolollllllllodOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMWXOdolllolllllllllllllloxKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxlllolllllloxkxolllllllllllllllllolllllllllllllxKWMWWWNNXXXKKOxoollllllllllodOXWMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMWXOdollllllllllllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOollllllllllxKNKOxooollolllllllllllllllllllolod0XX0OkxdddoooodoollollllllllllodOXWMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMN0xollllllllllllllllllllllld0NMMMMMMMMMMMMMMMMMMMMMMMWWNKKNMMMMMMMMMMMW0dlllllllllokXWMWNKkoloolllllllllllllllllllolokkxoolllllllllllllollllllllllllllox0NMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMWKxolllllllllllllllllllllllllloONMMMMMMMMMMMMMMMMMMMWNKOxdookNMMMMMMMMMWXkollllllodx0NWMMWWXkolooollllllllllllllllllllooollllllllllllllolllllllllllloooolloxKWMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMWXOdllllllllllllllooollllllllollld0WMMMMMMMMMMMMMMMMWXOxollllloOWMMMMMMMWNkollloodxk0KKXXK0OkdoollllllllllllllllllllllllllllllollllllllloollllllollllllllllllldOXWMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMN0xolllllllllllolllllllllllloodddddONMMMMMMMMMMMMMMMNOdolllllllokNMMMMMMWNkolllloddddddoooolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllox0NMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMWXkolllllllllllllllllllodxxkkO0KXNNXXXWMMMMMMMMMMMMMMNkolllllllllod0NMMMMMNOollllloollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllolllllllllllllokXWMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMWKxollllllllllllllllllox0NWWWWWMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllllookKNWWNOolollloolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloxKWMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMN0dlllllllllllllllllllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkoloolllollllolloxO0Odllllllllllllllllllllllllllllllllllllllllllllollllllllllllllllllllllllllllllllllllllllllllllld0NWMMMMMMMMMMMMM
MMMMMMMMMMMMMXkolllllllllllllllllolllxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXOO0KKOdollllllllllooolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloONWMMMMMMMMMMMM
MMMMMMMMMMMWXkollllllllllllllllllllllxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWMMMMWNKOxoollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllokXWMMMMMMMMMMM
MMMMMMMMMMWKxollllllllllllllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWKxollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloxKWMMMMMMMMMM
MMMMMMMMMWKxollllllllllllodxkkkkkkkO0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMNKOkO0KK0OkdolllllloolllllllllllloooollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloxKWMMMMMMMMM
MMMMMMMMWKxllllllllllolodOXWWWWWWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxolloooollllllllllllllllloollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxKWMMMMMMMM
MMMMMMMWKxlllllllllollokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxololllllllooolloollllloolloooolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxKWMMMMMMM
MMMMMMWXxllllllllooodkKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMKdloollllllllllololodxxddddk0KK0kxxxdollolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXWMMMMMM
MMMMMMXkolllllodk0KXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMKdllollllllllllllodOXWWNXXNWMMMMWWWNX0xolollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllokNMMMMMM
MMMMMNOollllodONWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dooollllllllllllodOXNWWWWWWMMMMMMMMMWXOddxxddolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloONMMMMM
MMMMW0dllllodKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKKK0kdlllllllllllloodxxxxkkOOKNWMMMMMMWNNNNNXKOkdooooollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllld0WMMMM
MMMWKxllllloOWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkolllllollllllllllllllllodOKXWMMMMMMMMMMMMWNXKK0OOkkkxdooolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxKWMMM
MMMNkollllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWXOdlllllolllllllllllloloolllooxKWMMMMMMMMMMMMMMMMMMMMWWWNXKOxoollllllllllllllllllllllllllllllllllllllllllllllllllllllllllolokNMMM
MMW0ollllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOOkxdollllllllllllllllllllllllllllox0NWMMMMWWNNXXKKXNWMMMMMMMMMWNKOxolllolllllllllllllllllllllllllllllllllllllllllllllllllllllllo0WMM
MMXxllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXkolllllllllllllllllllllllllllllllllllooxO000OkxdddoooodkKWMMMMMMMMMMWXxllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXWM
MWOollllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXkollllllllllllllllllllllllllllllllllllllllllllllllllllllld0WMMMMMMMMMWKdlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloOWM
MXxllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXkollllllllllllllllllllllllllllllllllooollllllllllllllllllold0WMMMMMMWN0dolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXM
W0ollllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNKkdolllllllllllllllllllllllllllllllllllllllllllllllllolllllllllokKXNWWNKkollllllllloxdollllllllolllllllllllllllllllllllllolllllllllllllolo0W
NkllllloxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllodxkkdoolollllllllxKOolllllllllllllllllllllllollooollllllloolllllloolllllkN
KxllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0doolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllkX0dlllllllllllllllllllloollloOKKOkxdddoollllllllllllllxK
Oolllllo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXXkollllooolllllllllllllllloONMMMWNNNXX0xolllllllllolloO
kolllllo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllxXWXkollollllllllllllllllllodKMMMMMMMMMMWKxollllolollolok
kllllllo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloolllllllllxXWWXkolllllllllllllllolllloONMMMMMMMMMMMW0dllllllllllllk
xollolld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxllllolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloollllllolloONMMN0xoolllllllolllllllloxXWMMMMMMMMMMMMXxollllllloollx
dollllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloollld0WMMWWXOdollollollllllloxXWMMMMMMMMMMMMMNOollllllokkold
olllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNxlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllldONMMMMWXxollllolllllox0NWMMMMMMMMMMMMMMNOollllllxXOolo
llllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXxllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloONMMMMMXxddxxxxkkO0XWMMMMMMMMMMMMMMMMMNOolllllxKW0olo
llllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdlllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllldONWMMMWNXNNWWWMMMMMMMMMMMMMMMMMMMMMMMW0dllollOWW0oll
llllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloxO0KXXXXKKKXNWMMMMMMMMMMMMMMMMMMMMMMMNOdolllkNWOolo
ollllllo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllooooddooloodkKWMMMMMMMMMMMMMMMMMMMMMMWXOolldKNOooo
dollllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllloollllo0WMMMMMMMMMMMMMMMMMMMMMMMMXkold0Nkold
xollllloxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllollokNMMMMMMMMMMMMMMMMMMMMMMMMMWOookXXxolx
xolllllloONWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMN00XW0dlox
kollllllloxOKXXNNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOxollllllllllllllllllllllllllllllolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllolllllolo0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWOollk
OolllllllllloodddkKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOkkxddooooollllllllllooodxxdollolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkoloO
KdllllllllllllllllxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWNXXXK0OOkkkkkkkkOKXXXNNX0xolllllllllllllllllllllllllllllllllllllllllllllllllllllllloollllllllloox0NMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMKdlldK
NkllllollloolllllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWWWWMMMMMMMMMWNOdlllllllllllllllllllllllllllllllllllllllllllllllllllllllollllllllodOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWOolokN
WOolllllllllllolllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllod0NWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXxolo0W
WXxllllllllllllllllox0NWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxollllllllllllllllllllllllllllllllllllllllllllllllllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOollxXM
MWOollllllllllllllooloxKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdllllllllllllllllllllllllllllllllllllllllllllllllllllloolld0NMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdlloOWM
MWXxllolllllllllllllllldOXWWNNK00KXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllllllllllllllllllllllllllllllllllllllllllllllllllllllod0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxollxXWM
MMWOollllllllloollllllolodxkxdollodk0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOollllllllllllllllllllllllllllllllllllllllllllllllllodxOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWN0dlllo0WMM
MMMXxllolllllllllllllllllllllllllllloox0NMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMN0dooollllllllllllllllllllllllllllllllllllllllllllodOXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKOkxxolllokNMMM
MMMW0dlllllllllllllllllllolllllllollollokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdoolllllllllllllllllllllllllllllllllllllllllllxKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNOoollllllldKWMMM
MMMMNOollllllllllllllllllllllllllllllllloOWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXKOdolllllllllllllllllllllllllllllllllllllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOolllllllloOWMMMM
MMMMMXkollllllllllllllllllllllllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllllllllllllllllllllllllllllllllld0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllolllllokNMMMMM
MMMMMWXxlllllllllllllllllllllllllllllllloxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0ollllllllllllllllllllllllllllllllllllllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWOollllllllxXWMMMMM
MMMMMMWKdlllllllllllllllllllllllllllllllokNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxolllllllllllllllllllllllllllllllllllllloONWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOolllllllxKWMMMMMM
MMMMMMMW0dlllllllllllllllllllllllllllllloOWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllloollllllllllllllllllllllllllllllllloxkOKKXXKKXNMMMMMMMMMMMMMMMMMMMMMMMMNOolllllldKWMMMMMMM
MMMMMMMMW0dllllllllllllllllllllllllllllldKMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdlllllllllllllllllllllllllllllllllllllllllllloooood0WMMMMMMMMMMMMMMMMMMMMMMMNOollolldKWMMMMMMMM
MMMMMMMMMW0dlllllllllllllllllllllllllllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllllllllllllllllllllllllllllllolllllllllllllllllld0WMMMMMMMMMMMMMMMMMMMMMMWKxllllldKWMMMMMMMMM
MMMMMMMMMMW0dlllllllllllllllllllllllllloxXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkolllllllllllllllllllllllllllllllllllllllllllllllllxXMMMMMMMMMMMMMMMMMMMMMWXOdolllldKWMMMMMMMMMM
MMMMMMMMMMMWKxollllllllllllllllllllllllloOWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllllllllllllllllllllllllloolllllllolllollllkNMMMMMMMMMMMMMMMMMMMWXOdolllloxKWMMMMMMMMMMM
MMMMMMMMMMMMWKxollllllllllllllllllllllllod0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkoloollllllllllllllllllllllllllllloddollllllllllllld0WMMMMMMMMMMMMMMMWWNKOdolllllokXWMMMMMMMMMMMM
MMMMMMMMMMMMMWXkollllllllllllllllllllllllldKMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllollllllllllllllllllllllllllllld0XOollllllllllllkNMMMMMMMMMMMMWNK0OkxollllllloONWMMMMMMMMMMMMM
MMMMMMMMMMMMMMMNOdlllllllllllllllllllllllokXMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMN0dlllllllllllllllllllllllllolllld0NWN0dlllllloodxkKWMMMMMMMMMMMMNOollllllllllld0NMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMWKxolollllllllllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNOolllllllllllllllllllllllllllldONMMMWKkdoooxOXNNWMMMMMMMMMMMMMNOollllllllllokXWMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMWXOdlllllllllllllllllloONWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdllllllllllllllllllllllllllld0NMMMMMWWXXXXNWMMMMMMMMMMMMMMMMW0dlllllllllod0NMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMWKxollolllllllllllloONMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMXxllllllllllllllllllllllllloxKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dlllllllllokXWMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMWNOdollllllloolllldKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkolllloollllooolllllllllodONWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNkllllllolox0NMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMWXkollllllolllllox0NMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKdlllllollllllllllllllodkKWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMW0dllllllodOXWMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMWKxoolllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMN0dollllllllllooddxxk0KNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdollllldOXWMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMN0xolllllllllllokXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKxolllllodk0KXNNWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKkdollolodkXWMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMNKxoolllllllllodOKNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWXOdolldOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xoollllodkXWMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNKkolllollllllloxOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOx0WMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOdolllllldOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWKOdollllllllllodx0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOdoollllloxOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0xollollollollodxOXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0kdooollllodk0NWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKkdooolllllllllooxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOxdollllllloxOXWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWN0kdllllllllollllodkOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWXKOkdoolllllloodOKNMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0kdolllllllllllllodxO0XNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNX0OxdollloolllloxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWX0kdoolllllllllllllooxkO0XNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWNX0OkxoololllllllooxOKNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNK0kdoolllllllllllloooodxkO0KXNWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWNXK0Okxdoolllllollllloxk0XWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNKOkdoollllllllloolllllloodxkkO00KXXNNWWWWWWMMMMMMMMMWWWWWWWNNXXK00Okxxdoolllllllllllloooxk0KNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNK0kxdoollllllllllllllllllllloodddxxxkkOOOOOOOOOOOkkkxxxdddoollllllllllllllllloodxO0XNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXK0OxdooollllllllllllooolllllllllllllllllllllllllllllllllllllllllllooodkO0KNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNXK0OkxdooollllllllllllllllllllllllllllllllllllllllllloooddxkO0KXNWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWNNXK0OOkkxdddoooooollllllllllllllllooooooddxxkOO0KKXNWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMWWWNNXXXKK00OOOOOOOOOOOOOOOO00KKXXXNNWWWMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
 */

