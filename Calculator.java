import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Calculator extends JFrame {
    private JTextField textField;
    private double number1 = 0, number2 = 0, result = 0;
    private String operator = "";
    private boolean isNewInput = true;

    public Calculator() {
        super("Java 計算機");

        // 粉紅主介面
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(new Color(255, 182, 193));
        setContentPane(mainPanel);

        // 顯示欄
        textField = new JTextField();
        textField.setEditable(false);
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
        textField.setFont(new Font("Arial", Font.BOLD, 28));
        textField.setBackground(Color.WHITE);
        textField.setPreferredSize(new Dimension(0, 60));
        mainPanel.add(textField, BorderLayout.NORTH);

        // 按鈕區域
        JPanel buttonPanel = new JPanel(new GridLayout(5, 4, 4, 4));
        buttonPanel.setBackground(new Color(255, 182, 193));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String[] buttons = {
            "AC", "+/-", "%", "/",
            "7", "8", "9", "*",
            "4", "5", "6", "-",
            "1", "2", "3", "+",
            "0", "del", ".", "="
        };

        for (String label : buttons) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            button.setFocusPainted(false);
            button.addActionListener(new ButtonHandler());
            buttonPanel.add(button);
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // 視窗設定
        setSize(280, 420);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();

            if (cmd.matches("[0-9]")) {
                if (isNewInput) {
                    textField.setText(cmd);
                    isNewInput = false;
                } else {
                    textField.setText(textField.getText() + cmd);
                }
            } else if (cmd.equals(".")) {
                if (!textField.getText().contains(".")) {
                    textField.setText(textField.getText() + ".");
                    isNewInput = false;
                }
            } else if (cmd.equals("AC")) {
                number1 = number2 = result = 0;
                operator = "";
                textField.setText("");
                isNewInput = true;
            } else if (cmd.equals("+/-")) {
                String text = textField.getText();
                if (!text.isEmpty()) {
                    if (text.startsWith("-")) {
                        textField.setText(text.substring(1));
                    } else {
                        textField.setText("-" + text);
                    }
                }
            } else if (cmd.equals("%")) {
                try {
                    double value = Double.parseDouble(textField.getText());
                    textField.setText(String.valueOf(value / 100));
                    isNewInput = true;
                } catch (NumberFormatException ex) {
                    textField.setText("Error");
                }
            } else if (cmd.equals("del")) {
                String text = textField.getText();
                if (!text.isEmpty()) {
                    textField.setText(text.substring(0, text.length() - 1));
                }
            } else if (cmd.equals("=")) {
                try {
                    number2 = Double.parseDouble(textField.getText());
                    switch (operator) {
                        case "+": result = number1 + number2; break;
                        case "-": result = number1 - number2; break;
                        case "*": result = number1 * number2; break;
                        case "/": result = number2 == 0 ? 0 : number1 / number2; break;
                    }
                    textField.setText(String.valueOf(result));
                    isNewInput = true;
                } catch (NumberFormatException ex) {
                    textField.setText("Error");
                }
            } else if (cmd.matches("[+\\-*/]")) {
                try {
                    number1 = Double.parseDouble(textField.getText());
                    operator = cmd;
                    isNewInput = true;
                } catch (NumberFormatException ex) {
                    textField.setText("Error");
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Calculator());
    }
}
