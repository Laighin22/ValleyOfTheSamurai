import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.*;

import util.AudioPlayer;
import util.CircularLinkedList;
import util.Node;
import util.UnitTests;

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


public class MainWindow {
    private static JFrame frame = new JFrame("Game");   // Change to the name of your game
    private static Model gameworld = new Model();
    private static Viewer canvas = new Viewer(gameworld, frame);
    private KeyListener Controller = new Controller();
    private MouseListener MouseController = new Controller();
    private static int TargetFPS = 100;
    private static boolean startGame = false;
    private JLabel BackgroundImageForStartMenu;
    private AudioPlayer audioPlayer = new AudioPlayer();
    private static JButton resumeButton;
    private static JButton exitButton;
    private static JButton retryButton;
    private static JButton changeCharacterButton;
    private static JLabel characterIconLabel;
    private static JLabel characterDesc;
    private static JTextField characterName;
    private static JLabel gameOverText;
    private static JLabel gameCompleteText;
    private static JComboBox<Integer> levelSelector;
    private static JLabel levelSelectorLabel;
    private CircularLinkedList characters = new CircularLinkedList();
    private String selectedCharacter;
    public static boolean devMode=false;

    private static GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
    public MainWindow() {
        String desc1="<html>Gender: Female<br>Style: Mid-range + Close-range<br>Special Ability: Deflect + Double Jump<br>Passive ability: Natural Healing<br>Cons: More difficult to learn</html>";
        String desc2="<html>Gender: Male<br>Style: Close-range<br>Special Ability:Random Teleport (Middle Mouse)<br>Passive ability: 1.5x faster movement speed<br>Cons: Extremely vulnerable in combat</html>";
        characters.insert(new Node("Samurai", "res/SamuraiIcon.png", desc1));
        characters.insert(new Node("Fighter", "res/fighterIcon.png", desc2));
        selectedCharacter=characters.getFirst().getCharacter();
        JLayeredPane layeredPane = new JLayeredPane();
        frame.setLayeredPane(layeredPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        graphicsDevice.setFullScreenWindow(frame);
        frame.setLayout(null); // Using null layout
        frame.add(canvas);
        canvas.setBounds(0, 0, 1920, 1080);
        canvas.setBackground(new Color(255, 255, 255)); //white background  replaced by Space background but if you remove the background method this will draw a white screen
        canvas.setVisible(false);   // this will become visible after you press the key.

        //loading background image
        File BackgroundToLoad = new File("res/backgrounds/Ocean_4.png");  //should work okay on OSX and Linux but check if you have issues depending your eclipse install or if your running this without an IDE
        try {

            BufferedImage myPicture = ImageIO.read(BackgroundToLoad);
            BackgroundImageForStartMenu = new JLabel(new ImageIcon(myPicture));
            BackgroundImageForStartMenu.setBounds(0, 0, 1280, 720);
            layeredPane.add(BackgroundImageForStartMenu, JLayeredPane.DEFAULT_LAYER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JLabel startMenuTitle = new JLabel();
        startMenuTitle.setText("Valley of the Samurai");
        startMenuTitle.setFont(new Font("Skia", Font.ITALIC, 48));
        startMenuTitle.setBounds(400, 100, 600, 50);
        startMenuTitle.setForeground(Color.RED);

        exitButton = new JButton("Exit to Desktop");  // exit button
        exitButton.setFont(new Font("Arial", Font.ITALIC, 11));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        exitButton.setBounds(550, 500, 200, 40);

        JButton startMenuButton = new JButton("Start Game");  // start button
        startMenuButton.setFont(new Font("Arial", Font.ITALIC, 11));
        startMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layeredPane.add(canvas);
                startMenuButton.setVisible(false);
                exitButton.setVisible(false);
                startMenuTitle.setVisible(false);
                changeCharacterButton.setVisible(false);
                characterIconLabel.setVisible(false);
                characterDesc.setVisible(false);
                characterName.setVisible(false);
                levelSelector.setVisible(false);
                levelSelectorLabel.setVisible(false);
                //titleLabel.setVisible(false);
                BackgroundImageForStartMenu.setVisible(false);
                canvas.setVisible(true);
                canvas.addKeyListener(Controller);    //adding the controller to the Canvas
                canvas.addMouseListener(MouseController);
                canvas.requestFocusInWindow();   // making sure that the Canvas is in focus so keyboard input will be taking in .
                audioPlayer.stopAudio();
                gameworld.playLevelMusic(gameworld.getLevel());
                gameworld.getPlayer().setCharacter(selectedCharacter);
                gameworld.setPlayerName(characterName.getText());
                if (devMode) {
                    int level = -1;
                    if (levelSelector.getSelectedItem() != null && levelSelector.getSelectedItem() instanceof Integer) {
                        level = (int) levelSelector.getSelectedItem();
                    }
                    if (level>-1) {
                        gameworld.setCutScenePlaying(false);
                    }
                    gameworld.setLevel(level);
                    gameworld.setEnemyWave(level);
                    gameworld.resetModel();
                }
                startGame = true;
            }
        });
        startMenuButton.setBounds(550, 350, 200, 40);

        resumeButton = new JButton("Resume Game");  // resume button
        resumeButton.setFont(new Font("Arial", Font.ITALIC, 11));
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!Objects.equals(selectedCharacter, gameworld.getPlayer().getCharacter())) {
                    gameworld.getPlayer().setCharacter(selectedCharacter);
                }
                gameworld.setLevelPaused(false);
                resumeButton.setVisible(false);
                exitButton.setVisible(false);
                gameworld.getMusicAudio().resumeAudio();
                canvas.requestFocusInWindow();
            }
        });
        resumeButton.setBounds(550, 350, 200, 40);

        gameOverText = new JLabel();
        gameOverText.setText("GAME OVER");
        gameOverText.setFont(new Font("Skia", Font.BOLD, 48));
        gameOverText.setBounds(500, 200, 600, 50);
        gameOverText.setForeground(Color.RED);
        gameOverText.setVisible(false);

        gameCompleteText = new JLabel("Thank you for playing my game");
        gameCompleteText.setVerticalAlignment(SwingConstants.CENTER);
        gameCompleteText.setHorizontalAlignment(SwingConstants.CENTER);
        gameCompleteText.setFont(new Font("Skia", Font.BOLD, 30));
        gameCompleteText.setBounds(250, 200, 800, 200);
        gameCompleteText.setForeground(Color.magenta);
        gameCompleteText.setVisible(false);

        retryButton = new JButton("Retry");  // retry button
        retryButton.setFont(new Font("Arial", Font.ITALIC, 11));
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                retryButton.setVisible(false);
                exitButton.setVisible(false);
                gameOverText.setVisible(false);
                gameworld.resetModel();
                canvas.requestFocusInWindow();
            }
        });
        retryButton.setBounds(550, 350, 200, 40);

        changeCharacterButton = new JButton("Change Character");  // change character button
        changeCharacterButton.setFont(new Font("Arial", Font.ITALIC, 11));
        changeCharacterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeCharacter();
            }
        });
        changeCharacterButton.setBounds(900, 350, 200, 20);

        characterIconLabel = new JLabel();
        characterIconLabel.setBounds(900, 200, 900, 500);
        ImageIcon characterIcon = new ImageIcon(characters.getCurr().getIcon());
        characterIconLabel.setIcon(characterIcon);

        characterDesc = new JLabel();
        characterDesc.setBounds(900, 500, 300, 100);
        characterDesc.setText(characters.getCurr().getDesc());
        characterDesc.setForeground(Color.WHITE);

        characterName = new JTextField();
        characterName.setBounds(900, 600, 200, 20);
        characterName.setText("Samurai");

        int[] choices = {-1, 0, 1, 2, 3};
        levelSelector = new JComboBox<>();
        for (int choice : choices) {
            levelSelector.addItem(choice);
        }
        levelSelector.setBounds(100, 350, 200, 20);

        levelSelector.setVisible(devMode);

        levelSelectorLabel = new JLabel();
        levelSelectorLabel.setText("Select a level:");
        levelSelectorLabel.setForeground(Color.WHITE);
        levelSelectorLabel.setBounds(100, 320, 200, 20);

        levelSelectorLabel.setVisible(devMode);

        audioPlayer.playMusic("res/Music/xDeviruchi_TitleTheme.wav");
        audioPlayer.setVolume(-10.0f);

        layeredPane.add(startMenuButton, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(exitButton, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(resumeButton, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(retryButton, JLayeredPane.PALETTE_LAYER);

        layeredPane.add(startMenuTitle, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(gameOverText, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(gameCompleteText, JLayeredPane.PALETTE_LAYER);

        layeredPane.add(changeCharacterButton, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(characterIconLabel, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(characterDesc, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(characterName, JLayeredPane.PALETTE_LAYER);

        layeredPane.add(levelSelector, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(levelSelectorLabel, JLayeredPane.PALETTE_LAYER);

        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }


    public static void main(String[] args) {
        MainWindow hello = new MainWindow();  //sets up environment
        while (true)   //not nice but remember we do just want to keep looping till the end.  // this could be replaced by a thread but again we want to keep things simple
        {
            //swing has timer class to help us time this but I'm writing my own, you can of course use the timer, but I want to set FPS and display it

            int TimeBetweenFrames = 1000 / TargetFPS;
            long FrameCheck = System.currentTimeMillis() + (long) TimeBetweenFrames;

            //wait till next time step
            while (FrameCheck > System.currentTimeMillis()) {
            }


            if (startGame) {
                gameloop();
            }

            //UNIT test to see if framerate matches
            UnitTests.CheckFrameRate(System.currentTimeMillis(), FrameCheck, TargetFPS);

        }


    }

    //Basic Model-View-Controller pattern
    private static void gameloop() {
        // GAMELOOP

        // controller input  will happen on its own thread
        // So no need to call it explicitly

        // model update
        gameworld.gamelogic();
        // view update

        canvas.updateview();

        if (gameworld.gameOver && gameworld.isLevelPaused()) {
            setGameOverUI(true);
            gameworld.getMusicAudio().pauseAudio();
        } else if (gameworld.isLevelPaused() && !gameworld.gameOver) {
            setGamePauseUI(true);
            if (devMode) {
                setCharacterSelectUI(true);
            }
            gameworld.getMusicAudio().pauseAudio();
        } else if (gameworld.gameComplete) {
            setGameCompleteUI(true);
        } else {
            hideUIs();
        }

        // Both these calls could be setup as  a thread but we want to simplify the game logic for you.
        //score update
        frame.setTitle("Score =  " + gameworld.getScore());

    }

    public static void setGameOverUI(boolean state) {
        retryButton.setVisible(state);
        exitButton.setVisible(state);
        gameOverText.setVisible(state);
    }

    public static void setGamePauseUI(boolean state) {
        resumeButton.setVisible(state);
        exitButton.setVisible(state);
    }

    public static void setGameCompleteUI(boolean state) {
        gameCompleteText.setVisible(state);
        exitButton.setVisible(state);
    }

    public static void setCharacterSelectUI(boolean state) {
        changeCharacterButton.setVisible(state);
        characterIconLabel.setVisible(state);
        characterDesc.setVisible(state);
    }

    public static void hideUIs() {
        setGamePauseUI(false);
        setGameOverUI(false);
        setGameCompleteUI(false);
        setCharacterSelectUI(false);
    }

    public void changeCharacter() {
        selectedCharacter=characters.next().getCharacter();
        ImageIcon newIcon = new ImageIcon(characters.getCurr().getIcon());
        characterIconLabel.setIcon(newIcon);
        characterDesc.setText(characters.getCurr().getDesc());
    }

}

/*
 *
 *

Hand shake agreement
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,=+++
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,,,,:::::,=+++????
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,,,,,,,,,,,,:++++????+??
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,:,:,,:,:,,,,,,,,,,,,,,,,,,,,++++++?+++++????
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,=++?+++++++++++??????
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,~+++?+++?++?++++++++++?????
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,:::,,,,,,,,,,,,,,,,,,,,,,,,,,,~+++++++++++++++????+++++++???????
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,:,,,,,,,,,,,,,,,,,,,,,,:===+=++++++++++++++++++++?+++????????????????
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,,,,,,,,,,,,,,,,~=~~~======++++++++++++++++++++++++++????????????????
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,::::,,,,,,=~.,,,,,,,+===~~~~~~====++++++++++++++++++++++++++++???????????????
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,:,,,,,~~.~??++~.,~~~~~======~=======++++++++++++++++++++++++++????????????????II
:::::::::::::::::::::::::::::::::::::::::::::::::::::::,:,,,,:=+++??=====~~~~~~====================+++++++++++++++++++++?????????????????III
:::::::::::::::::::::::::::::::::::::::::::::::::::,:,,,++~~~=+=~~~~~~==~~~::::~~==+++++++==++++++++++++++++++++++++++?????????????????IIIII
::::::::::::::::::::::::::::::::::::::::::::::::,:,,,:++++==+??+=======~~~~=~::~~===++=+??++++++++++++++++++++++++?????????????????I?IIIIIII
::::::::::::::::::::::::::::::::::::::::::::::::,,:+????+==??+++++?++====~~~~~:~~~++??+=+++++++++?++++++++++??+???????????????I?IIIIIIII7I77
::::::::::::::::::::::::::::::::::::::::::::,,,,+???????++?+?+++???7?++======~~+=====??+???++++++??+?+++???????????????????IIIIIIIIIIIIIII77
:::::::::::::::::::::::::::::::::::::::,,,,,,=??????IIII7???+?+II$Z77??+++?+=+++++=~==?++?+?++?????????????III?II?IIIIIIIIIIIIIIIIIIIIIIIIII
::::::::::::::::::::::::::::::,,,,,,~=======++++???III7$???+++++Z77ZDZI?????I?777I+~~+=7+?II??????????????IIIIIIIIIIIIIIIIIIIIII??=:,,,,,,,,
::::::::,:,:,,,,,,,:::~==+=++++++++++++=+=+++++++???I7$7I?+~~~I$I??++??I78DDDO$7?++==~I+7I7IIIIIIIIIIIIIIIIII777I?=:,,,,,,,,,,,,,,,,,,,,,,,,
++=++=++++++++++++++?+????+??????????+===+++++????I7$$ZZ$I+=~$7I???++++++===~~==7??++==7II?~,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
+++++++++++++?+++?++????????????IIIII?I+??I???????I7$ZOOZ7+=~7II?+++?II?I?+++=+=~~~7?++:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
+?+++++????????????????I?I??I??IIIIIIII???II7II??I77$ZO8ZZ?~~7I?+==++?O7II??+??+=====.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
?????????????III?II?????I?????IIIII???????II777IIII7$ZOO7?+~+7I?+=~~+???7NNN7II?+=+=++,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
????????????IIIIIIIIII?IIIIIIIIIIII????II?III7I7777$ZZOO7++=$77I???==+++????7ZDN87I??=~,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
IIII?II??IIIIIIIIIIIIIIIIIIIIIIIIIII???+??II7777II7$$OZZI?+$$$$77IIII?????????++=+.,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII?+++?IIIII7777$$$$$$7$$$$7IIII7I$IIIIII???I+=,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII???????IIIIII77I7777$7$$$II????I??I7Z87IIII?=,,,,,,,,,,,:,,::,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
777777777777777777777I7I777777777~,,,,,,,+77IIIIIIIIIII7II7$$$Z$?I????III???II?,,,,,,,,,,::,::::::::,,:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
777777777777$77777777777+::::::::::::::,,,,,,,=7IIIII78ZI?II78$7++D7?7O777II??:,,,:,,,::::::::::::::,:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
$$$$$$$$$$$$$77=:,:::::::::::::::::::::::::::,,7II$,,8ZZI++$8ZZ?+=ZI==IIII,+7:,,,,:::::::::::::::::,:::,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
$$$I~::::::::::::::::::::::::::::::::::::::::::II+,,,OOO7?$DOZII$I$I7=77?,,,,,,:::::::::::::::::::::,,,:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
::::::::::::::::::::::::::::::::::::::::::::::::::::::+ZZ?,$ZZ$77ZZ$?,,,,,::::::::::::::::::::::::::,::::,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::I$:::::::::::::::::::::::::::::::::::::::::::,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,:,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,,,,,,,,,,,,,,,,,,,,,,,,,
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,,,,,,,,,,,,,,,,,,,,,,,,,
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,,,,,,,,,,,,,,,,,,,,,,,
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,,,,,,,,,,,,,,,,,,,,
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::,,,,,,,,,,,,,,,,,,,,,,
                                                                                                                             GlassGiant.com
 *
 *
 */
