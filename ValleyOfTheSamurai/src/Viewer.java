import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.*;

import util.GameObject;
import util.Vector3f;

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
 
 * Credits: Kelly Charles (2020)
 */
public class Viewer extends JPanel {
    private long CurrentAnimationTime = 0;
    private String prevStatus = "";

    private int GROUND_LEVEL = 400;

    Model gameworld = new Model();

    JFrame frame = new JFrame();

    public Viewer(Model World, JFrame frame) {
        this.gameworld = World;
        this.frame = frame;
        // TODO Auto-generated constructor stub
    }

    public Viewer(LayoutManager layout) {
        super(layout);
        // TODO Auto-generated constructor stub
    }

    public Viewer(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public Viewer(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        // TODO Auto-generated constructor stub
    }

    public void updateview() {

        this.repaint();
        // TODO Auto-generated method stub

    }


    public void paintComponent(Graphics g) {

        if (!gameworld.isLevelPaused()) {
            CurrentAnimationTime++; // runs animation time step
            gameworld.getPlayer().incrementAnimationTime();
        }
        super.paintComponent(g);


        //Draw player Game Object
        int x = (int) gameworld.getPlayer().getCentre().getX();
        int y = (int) gameworld.getPlayer().getCentre().getY();
        int width = (int) gameworld.getPlayer().getWidth();
        int height = (int) gameworld.getPlayer().getHeight();
        String texture = gameworld.getPlayer().getTexture();

        //Draw background
        drawBackground(g);


        //Draw player
        drawPlayer(x, y, width, height, g);

        //Draw Bullets
        // change back
        gameworld.getBullets().forEach((temp) ->
        {
            drawBullet(temp.getDirection(), (int) temp.getCentre().getX(), (int) temp.getCentre().getY(), (int) temp.getWidth(), (int) temp.getHeight(), temp.getTexture(), g);
        });

        //Draw Enemies
        gameworld.getEnemies().forEach((temp) ->
        {
            drawYurei(temp.getDirection(), (int) temp.getCentre().getX(), (int) temp.getCentre().getY(), (int) temp.getWidth(), (int) temp.getHeight(), temp.getTexture(), g);

        });

        gameworld.getOnres().forEach((temp) ->
        {
            drawOnres(temp.getDirection(), (int) temp.getCentre().getX(), (int) temp.getCentre().getY(), (int) temp.getWidth(), (int) temp.getHeight(), temp.getTexture(), g);
        });

        gameworld.getNpcs().forEach((temp) ->
        {
            drawNpcs(temp, (int) temp.getCentre().getX(), (int) temp.getCentre().getY(), (int) temp.getWidth(), (int) temp.getHeight(), temp.getTexture(), g);
            if (temp.isSpeaking()) {
                drawSpeech(temp, g);
            }
        });

        drawForeGround(g);

        drawHealth(g);

        if (gameworld.getLevel()==3) {
            drawNPCHealth(g);
        } else if (gameworld.getLevel()==4) {
            if (gameworld.getPlayer().isSpeaking()) {
                drawSpeech(gameworld.getPlayer(), g);
            }
        }

        if (gameworld.isCutScenePlaying()) {
            drawCutSceneMode(g);
        }
    }

    private void drawCutSceneMode(Graphics g) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, (int) width, 100);
        g.fillRect(0, 650, (int) width, 100);
    }

    private void drawYurei(String direction, int x, int y, int width, int height, String texture, Graphics g) {
        File TextureToLoad = new File(texture);  //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        try {
            BufferedImage myImage = ImageIO.read(TextureToLoad);
            if (Objects.equals(direction, "LEFT")) {
                AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
                transform.translate(-myImage.getWidth(null), 0);
                AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                myImage = transformOp.filter(myImage, null);
            }
            //The spirte is 32x32 pixel wide and 4 of them are placed together so we need to grab a different one each time
            //remember your training :-) computer science everything starts at 0 so 32 pixels gets us to 31
            int currentPositionInAnimation = ((int) ((CurrentAnimationTime % 20)/5) * 128); //slows down animation so every 10 frames we get another frame so every 100ms
            g.drawImage(myImage, x, y, x + width, y + height, currentPositionInAnimation, 0, currentPositionInAnimation + 128-1, 128-1, null);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void drawOnres(String direction, int x, int y, int width, int height, String texture, Graphics g) {
        File TextureToLoad = new File(texture);
        try {
            BufferedImage myImage = ImageIO.read(TextureToLoad);
            if (Objects.equals(direction, "LEFT")) {
                AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
                transform.translate(-myImage.getWidth(null), 0);
                AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                myImage = transformOp.filter(myImage, null);
            }
            //The spirte is 32x32 pixel wide and 4 of them are placed together so we need to grab a different one each time
            //remember your training :-) computer science everything starts at 0 so 32 pixels gets us to 31
            int currentPositionInAnimation = ((int) ((CurrentAnimationTime % 20)/5) * 128); //slows down animation so every 10 frames we get another frame so every 100ms
            g.drawImage(myImage, x, y, x + width, y + height, currentPositionInAnimation, 0, currentPositionInAnimation + 128-1, 128-1, null);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void drawNpcs(GameObject npc, int x, int y, int width, int height, String texture, Graphics g) {
        //The spirte is 32x32 pixel wide and 4 of them are placed together so we need to grab a different one each time
        //remember your training :-) computer science everything starts at 0 so 32 pixels gets us to 31
        //int currentPositionInAnimation = ((int) ((CurrentAnimationTime % 20)/5) * 128); //slows down animation so every 10 frames we get another frame so every 100ms
        //g.drawImage(npc.getSprite(), x, y, x + width, y + height, currentPositionInAnimation, 0, currentPositionInAnimation + 128-1, 128-1, null);
        File TextureToLoad = new File(texture);
        try {
            BufferedImage myImage = ImageIO.read(TextureToLoad);
            if (Objects.equals(npc.getDirection(), "LEFT")) {
                AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
                transform.translate(-myImage.getWidth(null), 0);
                AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                myImage = transformOp.filter(myImage, null);
            }
            //The spirte is 32x32 pixel wide and 4 of them are placed together so we need to grab a different one each time
            //remember your training :-) computer science everything starts at 0 so 32 pixels gets us to 31
            int currentPositionInAnimation = ((int) ((CurrentAnimationTime % 20)/5) * 128); //slows down animation so every 10 frames we get another frame so every 100ms
            g.drawImage(myImage, x, y, x + width, y + height, currentPositionInAnimation, 0, currentPositionInAnimation + 128-1, 128-1, null);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void drawSpeech(GameObject npc, Graphics g) {
        g.setColor(Color.BLACK);
        int x = (int) npc.getCentre().getX();
        int y = (int) npc.getCentre().getY();
        //int width = (int) npc.getWidth();
        int width = npc.getSpeech().length()*8;
        g.fillRoundRect(x - 12, y + 40, width + 2, 22, 20, 20);
        g.setColor(Color.WHITE);
        g.fillRoundRect(x - 10, y + 40, width, 20, 20, 20);
        g.setColor(Color.BLACK);
        g.drawString(npc.getSpeech(), x, y + 50);
    }

    private void drawBackground(Graphics g) {
        //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        File TextureToLoad = switch (gameworld.getLevel()) {
            case 0 -> new File("res/backgrounds/summer5.png");
            case 1 -> new File("res/backgrounds/summer3.png");
            case 2 -> new File("res/backgrounds/summer8.png");
            case 3 -> new File("res/backgrounds/summer1.png");
            case 4 -> new File("res/backgrounds/5.png");
            default -> new File("res/backgrounds/Ocean_4.png");
        };
        try {
            Image myImage = ImageIO.read(TextureToLoad);
            g.drawImage(myImage, 0, 0, 1280, 720, null);
            g.drawString("Coordinates: " + (int) gameworld.getPlayer().getCentre().getX() + ", " + (int) gameworld.getPlayer().getCentre().getY(), 50, 50);
            String controls="";
            int x=0;
            int y=0;
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(Color.YELLOW);
            if (gameworld.getLevel()==1) {
                controls = "Block: Right Click";
                FontMetrics fm = g.getFontMetrics();
                x = frame.getX() + (frame.getWidth() + fm.stringWidth(controls)) / 2;
                y = frame.getY() + ((frame.getHeight() - fm.getHeight()) / 2);
                g.drawString(controls, x, y);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void drawForeGround(Graphics g) {
        File TextureToLoad = null;
        if (gameworld.getLevel()==0) {
            TextureToLoad = new File("res/backgrounds/4.png");
        }
        if (TextureToLoad!=null) {
            try {
                Image myImage = ImageIO.read(TextureToLoad);
                g.drawImage(myImage, 0, 0, 1280, 720, null);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void drawHealth(Graphics g) {
        File TextureToLoad = new File("res/health/heartsh.png");
        int playerHealth = (int) gameworld.getPlayer().getHealth();
        int j=50;
        try {
            Image heart =  ImageIO.read(TextureToLoad);
            //For every number of health the player has, draw another heart and make sure it's drawn next to the previous one (j)
            for (int i=0;i<playerHealth;i++) {
                g.drawImage(heart, j, 60, 30, 30, null);
                j+=30;
            }
            //If the player had a non-whole number (i.e. 2.5 health) check for this and draw half a heart
            //Check by seeing if the converted int version of the number is equal to the actual player health
            if (gameworld.getPlayer().getHealth()!=playerHealth) {
                TextureToLoad = new File("res/health/heart half.png");
                heart = ImageIO.read(TextureToLoad);
                g.drawImage(heart, j, 60, 30, 30, null);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawNPCHealth(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(400, 100, 500, 15);
        g.setColor(Color.RED);
        double max_health = 6.0;
        double total_health=0.0;
        for (GameObject temp : gameworld.getNpcs()) {
            total_health += temp.getHealth();
        }
        int percentage = (int) ((total_health/max_health)*500);
        g.fillRect(400, 100, percentage, 15);
        g.drawString("Villagers' Health:", 600, 80);
    }

    private void drawBullet(String direction, int x, int y, int width, int height, String texture, Graphics g) {
        File TextureToLoad = new File(texture);  //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        try {
            BufferedImage myImage = ImageIO.read(TextureToLoad);
            if (Objects.equals(direction, "RIGHT")) {
                AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
                transform.translate(-myImage.getWidth(null), 0);
                AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                myImage = transformOp.filter(myImage, null);
            }
            //64 by 128
            int currentPositionInAnimation = ((int) ((CurrentAnimationTime % 20) / 5) * 24);
            g.drawImage(myImage, x, y, x + width, y + height, currentPositionInAnimation, 0, currentPositionInAnimation + 24 - 1, 24 - 1, null);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void drawPlayer(int x, int y, int width, int height, Graphics g) {

        String character = gameworld.getPlayer().getCharacter();

        //Check current status of the player to draw them right with correct number of frames
        int frames = 0;
        File TextureToLoad;
        frames = switch (gameworld.getPlayer().getStatus()) {
            case ("WALKING") -> {
                TextureToLoad = new File("res/"+character+"/Walk.png");
                yield 18;
            }
            case ("ATTACKING") -> {
                TextureToLoad = new File("res/"+character+"/Attack.png");
                yield 12;
            }
            case ("BLOCKING") -> {
                TextureToLoad = new File("res/"+character+"/Shield.png");
                yield 6;
            }
            case ("RUNNING") -> {
                TextureToLoad = new File("res/"+character+"/Run.png");
                yield 24;
            }
            case ("JUMPING") -> {
                TextureToLoad = new File("res/"+character+"/Jump.png");
                yield 12;
            }
            case ("FALLING") -> {
                TextureToLoad = new File("res/"+character+"/Fall.png");
                yield 12;
            }
            case ("HURT") -> {
                TextureToLoad = new File("res/"+character+"/Hurt.png");
                yield 6;
            }
            default -> {
                TextureToLoad = new File("res/"+character+"/Idle.png");
                yield 18;
            }
        };
        //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        try {
            BufferedImage myImage = ImageIO.read(TextureToLoad);
            if (!Objects.equals(gameworld.getPlayer().getStatus(), prevStatus)) {
                gameworld.getPlayer().setCurrentAnimationTime(0);
                prevStatus = gameworld.getPlayer().getStatus();
            }
            if (Objects.equals(gameworld.getPlayer().getDirection(), "LEFT")) {
                AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
                transform.translate(-myImage.getWidth(null), 0);
                AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                myImage = transformOp.filter(myImage, null);
                int currentPositionInAnimation = myImage.getWidth(null) - ((((gameworld.getPlayer().getCurrentAnimationTime() % frames) / 3)) * 128) - 128; //slows down animation so every 3 frames we get another frame so every 30ms
                applyGravity();
                g.drawImage(myImage, x, y, x + width, y + height, currentPositionInAnimation, 0, currentPositionInAnimation + 128 - 1, 128 - 1, null);
            } else {
                //The spirte is 32x32 pixel wide and 4 of them are placed together so we need to grab a different one each time
                //remember your training :-) computer science everything starts at 0 so 32 pixels gets us to 31
                int currentPositionInAnimation = (((gameworld.getPlayer().getCurrentAnimationTime() % frames) / 3)) * 128; //slows down animation so every 3 frames we get another frame so every 30ms
                applyGravity();
                g.drawImage(myImage, x, y, x + width, y + height, currentPositionInAnimation, 0, currentPositionInAnimation + 128 - 1, 128 - 1, null);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer));
        //Lighnting Png from https://opengameart.org/content/animated-spaceships  its 32x32 thats why I know to increament by 32 each time
        // Bullets from https://opengameart.org/forumtopic/tatermands-art
        // background image from https://www.needpix.com/photo/download/677346/space-stars-nebula-background-galaxy-universe-free-pictures-free-photos-free-images

    }

    public void applyGravity() {
        //System.out.println(gameworld.getPlayer().getCentre().getY());
        if (gameworld.isJumping()) {
            gameworld.getPlayer().getCentre().ApplyVector(new Vector3f(0, 10, 0));
            if (gameworld.getPlayer().getCentre().getY()<=gameworld.getJumpPos()-120) {
                gameworld.setJumping(false);
                gameworld.setFalling(true);
                gameworld.getPlayer().setStatus("FALLING");
            }
        } else {
            if (gameworld.getPlayer().getCentre().getY() < GROUND_LEVEL) {
                gameworld.getPlayer().getCentre().ApplyVector(new Vector3f(0, -10, 0));
            } else {
                gameworld.setFalling(false);
                gameworld.setJumpCounter(0);
            }
        }
    }

//    public void drawText(String s, int width, int x, int y, Graphics g) {
//        int length = (int) g.getFontMetrics().getFont().getStringBounds().getWidth();
//        int start = width/2 - length/2;
//        g.drawString(s, start + x, y);
//    }


}


/*
 * 
 * 
 *              VIEWER HMD into the world                                                             
                                                                                
                                      .                                         
                                         .                                      
                                             .  ..                              
                               .........~++++.. .  .                            
                 .   . ....,++??+++?+??+++?++?7ZZ7..   .                        
         .   . . .+?+???++++???D7I????Z8Z8N8MD7I?=+O$..                         
      .. ........ZOZZ$7ZZNZZDNODDOMMMMND8$$77I??I?+?+=O .     .                 
      .. ...7$OZZ?788DDNDDDDD8ZZ7$$$7I7III7??I?????+++=+~.                      
       ...8OZII?III7II77777I$I7II???7I??+?I?I?+?+IDNN8??++=...                  
     ....OOIIIII????II?I??II?I????I?????=?+Z88O77ZZO8888OO?++,......            
      ..OZI7III??II??I??I?7ODM8NN8O8OZO8DDDDDDDDD8DDDDDDDDNNNOZ= ......   ..    
     ..OZI?II7I?????+????+IIO8O8DDDDD8DNMMNNNNNDDNNDDDNDDNNNNNNDD$,.........    
      ,ZII77II?III??????DO8DDD8DNNNNNDDMDDDDDNNDDDNNNDNNNNDNNNNDDNDD+.......   .
      7Z??II7??II??I??IOMDDNMNNNNNDDDDDMDDDDNDDNNNNNDNNNNDNNDMNNNNNDDD,......   
 .  ..IZ??IIIII777?I?8NNNNNNNNNDDDDDDDDNDDDDDNNMMMDNDMMNNDNNDMNNNNNNDDDD.....   
      .$???I7IIIIIIINNNNNNNNNNNDDNDDDDDD8DDDDNM888888888DNNNNNNDNNNNNNDDO.....  
       $+??IIII?II?NNNNNMMMMMDN8DNNNDDDDZDDNN?D88I==INNDDDNNDNMNNMNNNNND8:..... 
   ....$+??III??I+NNNNNMMM88D88D88888DDDZDDMND88==+=NNNNMDDNNNNNNMMNNNNND8......
.......8=+????III8NNNNMMMDD8I=~+ONN8D8NDODNMN8DNDNNNNNNNM8DNNNNNNMNNNNDDD8..... 
. ......O=??IIIIIMNNNMMMDDD?+=?ONNNN888NMDDM88MNNNNNNNNNMDDNNNMNNNMMNDNND8......
........,+++???IINNNNNMMDDMDNMNDNMNNM8ONMDDM88NNNNNN+==ND8NNNDMNMNNNNNDDD8......
......,,,:++??I?ONNNNNMDDDMNNNNNNNNMM88NMDDNN88MNDN==~MD8DNNNNNMNMNNNDND8O......
....,,,,:::+??IIONNNNNNNDDMNNNNNO+?MN88DN8DDD888DNMMM888DNDNNNNMMMNNDDDD8,.... .
...,,,,::::~+?+?NNNNNNNMD8DNNN++++MNO8D88NNMODD8O88888DDDDDDNNMMMNNNDDD8........
..,,,,:::~~~=+??MNNNNNNNND88MNMMMD888NNNNNNNMODDDDDDDDND8DDDNNNNNNDDD8,.........
..,,,,:::~~~=++?NMNNNNNNND8888888O8DNNNNNNMMMNDDDDDDNMMNDDDOO+~~::,,,.......... 
..,,,:::~~~~==+?NNNDDNDNDDNDDDDDDDDNNND88OOZZ$8DDMNDZNZDZ7I?++~::,,,............
..,,,::::~~~~==7DDNNDDD8DDDDDDDD8DD888OOOZZ$$$7777OOZZZ$7I?++=~~:,,,.........   
..,,,,::::~~~~=+8NNNNNDDDMMMNNNNNDOOOOZZZ$$$77777777777II?++==~::,,,......  . ..
...,,,,::::~~~~=I8DNNN8DDNZOM$ZDOOZZZZ$$$7777IIIIIIIII???++==~~::,,........  .  
....,,,,:::::~~~~+=++?I$$ZZOZZZZZ$$$$$777IIII?????????+++==~~:::,,,...... ..    
.....,,,,:::::~~~~~==+?II777$$$$77777IIII????+++++++=====~~~:::,,,........      
......,,,,,:::::~~~~==++??IIIIIIIII?????++++=======~~~~~~:::,,,,,,.......       
.......,,,,,,,::::~~~~==+++???????+++++=====~~~~~~::::::::,,,,,..........       
.........,,,,,,,,::::~~~======+======~~~~~~:::::::::,,,,,,,,............        
  .........,.,,,,,,,,::::~~~~~~~~~~:::::::::,,,,,,,,,,,...............          
   ..........,..,,,,,,,,,,::::::::::,,,,,,,,,.,....................             
     .................,,,,,,,,,,,,,,,,.......................                   
       .................................................                        
           ....................................                                 
               ....................   .                                         
                                                                                
                                                                                
                                                                 GlassGiant.com
                                                                 */
