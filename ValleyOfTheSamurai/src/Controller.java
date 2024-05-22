import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

//Singeton pattern
public class Controller implements MouseListener, KeyListener {

    private static boolean KeyAPressed = false;
    private static boolean KeyDPressed = false;
    private static boolean KeyWPressed = false;
    private static boolean KeySpacePressed = false;
    private static boolean KeyShiftPressed = false;
    private static boolean LeftClickPressed = false;
    private static boolean RightClickPressed = false;
    private static boolean MiddleClickPressed = false;
    private static boolean KeyEscPressed = false;

    private static final Controller instance = new Controller();

    public Controller() {
    }

    public static Controller getInstance() {
        return instance;
    }

    @Override
    // Key pressed , will keep triggering
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_SHIFT) {
            setKeyShiftPressed(true);
        }
        if (e.getKeyCode()==27) {
            setKeyEscPressed(true);
        }
        switch (e.getKeyChar()) {
            case 'a':
            case 'A':
                setKeyAPressed(true);
                break;
            case 'w':
                setKeyWPressed(true);
                break;
            case 'd' :
            case 'D' :
                //System.out.println("D PRESSED");
                setKeyDPressed(true);
                break;
            case ' ':
                setKeySpacePressed(true);
                break;
            default:
                //System.out.println("Controller test:  Unknown key pressed");
                break;
        }

        // You can implement to keep moving while pressing the key here .

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_SHIFT) {
            setKeyShiftPressed(false);
        }
        if (e.getKeyCode()==27) {
            setKeyEscPressed(false);
        }
        switch (e.getKeyChar()) {
            case 'a':
            case 'A':
                setKeyAPressed(false);
                break;
            case 'w':
                setKeyWPressed(false);
                break;
            case 'd':
            case 'D':
                setKeyDPressed(false);
                break;
            case ' ':
                setKeySpacePressed(false);
                break;
            default:
                //System.out.println("Controller test:  Unknown key pressed");
                break;
        }
        //upper case

    }



    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 && !isRightClickPressed()) {
            setLeftClickPressed(true);
        }
        if (e.getButton() == MouseEvent.BUTTON3 && !isLeftClickPressed()) {
            setRightClickPressed(true);
        }
        if (e.getButton() == MouseEvent.BUTTON2 && !isMiddleClickPressed()) {
            setMiddleClickPressed(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            setLeftClickPressed(false);
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            setRightClickPressed(false);
        }
        if (e.getButton() == MouseEvent.BUTTON2) {
            setMiddleClickPressed(false);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }


    public boolean isKeyAPressed() {
        return KeyAPressed;
    }


    public void setKeyAPressed(boolean keyAPressed) {
        KeyAPressed = keyAPressed;
    }
    public boolean isKeyDPressed() {
        return KeyDPressed;
    }
    public boolean isLeftClickPressed() {
        return LeftClickPressed;
    }

    public void setLeftClickPressed(boolean leftClickPressed) {
        LeftClickPressed = leftClickPressed;
    }

    public boolean isRightClickPressed() {
        return RightClickPressed;
    }

    public void setRightClickPressed(boolean rightClickPressed) {
        RightClickPressed = rightClickPressed;
    }
    public boolean isMiddleClickPressed() {
        return MiddleClickPressed;
    }
    public void setMiddleClickPressed(boolean middleClickPressed) {
        MiddleClickPressed = middleClickPressed;
    }


    public void setKeyDPressed(boolean keyDPressed) {
        KeyDPressed = keyDPressed;
    }


    public boolean isKeyWPressed() {
        return KeyWPressed;
    }


    public void setKeyWPressed(boolean keyWPressed) {
        KeyWPressed = keyWPressed;
    }


    public boolean isKeySpacePressed() {
        return KeySpacePressed;
    }


    public void setKeySpacePressed(boolean keySpacePressed) {
        KeySpacePressed = keySpacePressed;
    }

    public boolean isKeyShiftPressed() {
        return KeyShiftPressed;
    }

    public void setKeyShiftPressed(boolean keyShiftPressed) {
        KeyShiftPressed = keyShiftPressed;
    }

    public boolean isKeyEscPressed() {
        return KeyEscPressed;
    }

    public void setKeyEscPressed(boolean keyEscPressed) {
        KeyEscPressed = keyEscPressed;
    }


}

/*
 * 
 * KEYBOARD :-) . can you add a mouse or a gamepad 

 *@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ @@@@@@@@@@@@@@@

  @@@     @@@@    @@@@    @@@@    @@@@     @@@     @@@     @@@     @@@     @@@  

  @@@     @@@     @@@     @@@@     @@@     @@@     @@@     @@@     @@@     @@@  

  @@@     @@@     @@@     @@@@    @@@@     @@@     @@@     @@@     @@@     @@@  

@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

@     @@@     @@@     @@@      @@      @@@     @@@     @@@     @@@     @@@     @

@     @@@   W   @@@     @@@      @@      @@@     @@@     @@@     @@@     @@@     @

@@    @@@@     @@@@    @@@@    @@@@    @@@@     @@@     @@@     @@@     @@@     @

@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@N@@@@@@@@@@@@@@@@@@@@@@@@@@@

@@@     @@@      @@      @@      @@      @@@     @@@     @@@     @@@     @@@    

@@@   A   @@@  S     @@  D     @@      @@@     @@@     @@@     @@@     @@@     @@@    

@@@@ @  @@@@@@@@@@@@ @@@@@@@    @@@@@@@@@@@@    @@@@@@@@@@@@     @@@@   @@@@@   

    @@@     @@@@    @@@@    @@@@    $@@@     @@@     @@@     @@@     @@@     @@@

    @@@ $   @@@      @@      @@ /Q   @@ ]M   @@@     @@@     @@@     @@@     @@@

    @@@     @@@      @@      @@      @@      @@@     @@@     @@@     @@@     @@@

@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

@       @@@                                                @@@       @@@       @

@       @@@              SPACE KEY       @@@        @@ PQ     

@       @@@                                                @@@        @@        

@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
 * 
 * 
 * 
 * 
 * 
 */
