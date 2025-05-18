import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class Calculator {
    private JTextField displayField;
    private JLabel historyLabel;
    private StringBuilder inputBuffer = new StringBuilder();
    private double result = 0;
    private String lastOperator = "";
    private boolean startNewInput = true;

    private final double MAX_VALUE = 9.999999999E99;
    private final double MIN_VALUE = -9.999999999E99;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Calculator calc = new Calculator();
            calc.createAndShowGUI();
        });
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 400);
        
        // 設定自訂圖示
        try {
            ImageIcon icon = new ImageIcon("calculator.png"); // 替換成您的圖示路徑
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("無法載入自訂圖示: " + e.getMessage());
        }

        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.PINK);  // 改為粉紅色背景

        // 歷史顯示
        historyLabel = new JLabel(" ");
        historyLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        historyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        historyLabel.setOpaque(true);
        historyLabel.setBackground(Color.PINK);

        // 顯示區
        displayField = new JTextField("0");
        displayField.setFont(new Font("Arial", Font.BOLD, 26));
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        displayField.setEditable(false);
        displayField.setBackground(Color.WHITE);

        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.setBackground(Color.PINK);
        displayPanel.add(historyLabel, BorderLayout.NORTH);
        displayPanel.add(displayField, BorderLayout.CENTER);

        mainPanel.add(displayPanel, BorderLayout.NORTH);

        // 按鈕面板
        JPanel buttonPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        buttonPanel.setBackground(Color.PINK);
        String[] buttonLabels = {
                "AC", "+/-", "%", "/",
                "7", "8", "9", "*",
                "4", "5", "6", "-",
                "1", "2", "3", "+",
                "0", "del", ".", "="
        };

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.PLAIN, 18));
            button.setBackground(Color.WHITE);
            button.addActionListener(e -> handleCommand(label));
            buttonPanel.add(button);
        }

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        frame.setContentPane(mainPanel);
        setupKeyBindings(mainPanel);
        frame.setVisible(true);
    }

    private void setupKeyBindings(JPanel panel) {
        for (int i = 0; i <= 9; i++) {
            addKeyBinding(panel, KeyEvent.getExtendedKeyCodeForChar('0' + i), 0, String.valueOf(i));
            addKeyBinding(panel, KeyEvent.VK_NUMPAD0 + i, 0, String.valueOf(i));
        }

        addKeyBinding(panel, KeyEvent.VK_PLUS, 0, "+");
        addKeyBinding(panel, KeyEvent.VK_ADD, 0, "+");
        addKeyBinding(panel, KeyEvent.VK_MINUS, 0, "-");
        addKeyBinding(panel, KeyEvent.VK_SUBTRACT, 0, "-");
        addKeyBinding(panel, KeyEvent.VK_MULTIPLY, 0, "*");
        addKeyBinding(panel, KeyEvent.VK_DIVIDE, 0, "/");
        addKeyBinding(panel, KeyEvent.VK_PERIOD, 0, ".");
        addKeyBinding(panel, KeyEvent.VK_DECIMAL, 0, ".");
        addKeyBinding(panel, KeyEvent.VK_ENTER, 0, "=");
        addKeyBinding(panel, KeyEvent.VK_EQUALS, 0, "=");
        addKeyBinding(panel, KeyEvent.VK_ESCAPE, 0, "AC");
        addKeyBinding(panel, KeyEvent.VK_BACK_SPACE, 0, "del");
        addKeyBinding(panel, KeyEvent.VK_DELETE, 0, "del");
    }

    private void addKeyBinding(JComponent component, int keyCode, int modifiers, String actionCommand) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
        inputMap.put(keyStroke, actionCommand);
        actionMap.put(actionCommand, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCommand(actionCommand);
            }
        });
    }

    private void handleCommand(String command) {
        if (command.matches("[0-9]")) {
            if (startNewInput) {
                inputBuffer.setLength(0);
                startNewInput = false;
            }
            inputBuffer.append(command);
            displayField.setText(inputBuffer.toString());
        } else if (command.equals(".")) {
            if (startNewInput) {
                inputBuffer.setLength(0);
                inputBuffer.append("0.");
                startNewInput = false;
            } else if (!inputBuffer.toString().contains(".")) {
                inputBuffer.append(".");
            }
            displayField.setText(inputBuffer.toString());
        } else if (command.equals("AC")) {
            inputBuffer.setLength(0);
            displayField.setText("0");
            historyLabel.setText(" ");
            result = 0;
            lastOperator = "";
            startNewInput = true;
        } else if (command.equals("del")) {
            if (inputBuffer.length() > 0) {
                inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                displayField.setText(inputBuffer.length() == 0 ? "0" : inputBuffer.toString());
            }
        } else if (command.equals("+/-")) {
            if (inputBuffer.length() > 0) {
                if (inputBuffer.charAt(0) == '-') {
                    inputBuffer.deleteCharAt(0);
                } else {
                    inputBuffer.insert(0, '-');
                }
                displayField.setText(inputBuffer.toString());
            }
        } else if (command.equals("%")) {
            if (inputBuffer.length() > 0) {
                double value = Double.parseDouble(inputBuffer.toString()) / 100;
                if (checkOverflow(value)) return;
                inputBuffer = new StringBuilder(String.valueOf(value));
                displayField.setText(inputBuffer.toString());
                startNewInput = true;
            }
        } else if ("+-*/".contains(command)) {
            if (inputBuffer.length() > 0) {
                if (!startNewInput) {
                    calculate();
                    if (checkOverflow(result)) return;
                }
                lastOperator = command;
                historyLabel.setText(displayField.getText() + " " + command);
                startNewInput = true;
            } else if (result != 0) {
                lastOperator = command;
                historyLabel.setText(formatNumber(result) + " " + command);
                startNewInput = true;
            }
        } else if (command.equals("=")) {
            if (inputBuffer.length() > 0 && !startNewInput) {
                calculate();
                if (checkOverflow(result)) return;
                lastOperator = "";
                historyLabel.setText(" ");
                startNewInput = true;
            }
        }
    }

    private void calculate() {
        if (inputBuffer.length() > 0) {
            double currentValue = Double.parseDouble(inputBuffer.toString());

            switch (lastOperator) {
                case "+":
                    result += currentValue;
                    break;
                case "-":
                    result -= currentValue;
                    break;
                case "*":
                    result *= currentValue;
                    break;
                case "/":
                    if (currentValue != 0) {
                        result /= currentValue;
                    } else {
                        displayField.setText("Error");
                        historyLabel.setText(" ");
                        result = 0;
                        lastOperator = "";
                        startNewInput = true;
                        return;
                    }
                    break;
                default:
                    result = currentValue;
                    break;
            }

            String resultStr = formatNumber(result);
            displayField.setText(resultStr);
            inputBuffer = new StringBuilder(resultStr);

            if (!lastOperator.isEmpty()) {
                historyLabel.setText(resultStr + " " + lastOperator);
            }
        }
    }

    private String formatNumber(double num) {
        return (num == (long) num) ? String.valueOf((long) num) : String.valueOf(num);
    }

    private boolean checkOverflow(double value) {
        if (value > MAX_VALUE || value < MIN_VALUE || Double.isInfinite(value) || Double.isNaN(value)) {
            displayField.setText("Overflow");
            historyLabel.setText(" ");
            result = 0;
            inputBuffer.setLength(0);
            lastOperator = "";
            startNewInput = true;
            return true;
        }
        return false;
    }
}