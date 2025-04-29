import javax.swing.*;

public class SwingGUI extends JFrame{
    JFrame frame = new JFrame("Router Details");
    SwingGUI(){
        JLabel l = new JLabel("Router 1");
        l.setBounds(130,100,100, 40);
        frame.add(l);
        frame.setSize(700, 400);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public static void main(String[] args){
       new SwingGUI();
    }


}
